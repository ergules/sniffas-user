package app.vaazar.domain.refreshtoken.boundary.impl;

import app.vaazar.config.exception.AuthorisationException;
import app.vaazar.domain.refreshtoken.boundary.RefreshTokenService;
import app.vaazar.domain.refreshtoken.entity.RefreshToken;
import app.vaazar.domain.user.control.UserRepository;
import app.vaazar.domain.user.entity.User;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * Issues, validates and revokes refresh tokens using Redis as the backing store.
 *
 * On login an opaque random token is generated and written to Redis under
 * {@code refresh:token:{token}} with the owning user id as value and a TTL equal to
 * the configured refresh-token lifetime. The token is also added to the
 * {@code refresh:user:{userId}} set so all active tokens of a user can be located.
 *
 * On /refresh the token key is looked up; a missing key means the token is unknown
 * or has expired. Otherwise the owning user is loaded by id and returned so a new
 * access JWT can be issued.
 *
 * When a user is deleted every token in {@code refresh:user:{userId}} is removed,
 * invalidating all sessions of that user immediately.
 */
@Service
@ConditionalOnProperty(name = "app.redis.active", havingValue = "true")
public class RedisRefreshTokenServiceImpl implements RefreshTokenService {

    private static final String TOKEN_KEY = "refresh:token:";
    private static final String USER_KEY = "refresh:user:";

    private final StringRedisTemplate redis;
    private final UserRepository userRepo;
    private final long refreshTokenDurationSeconds;
    private final Logger log;

    public RedisRefreshTokenServiceImpl(
            StringRedisTemplate redis,
            UserRepository userRepo,
            @Value("${app.refresh-token-expiration-seconds:2592000}") long refreshTokenDurationSeconds,
            Logger log) {
        this.redis = redis;
        this.userRepo = userRepo;
        this.refreshTokenDurationSeconds = refreshTokenDurationSeconds;
        this.log = log;
    }

    /**
     * Lettuce connects lazily by default; force a PING on startup so a
     * misconfigured host/port/password fails the application immediately
     * instead of on the first /login or /refresh call.
     */
    @PostConstruct
    public void verifyRedisConnection() {
        try (RedisConnection conn = redis.getRequiredConnectionFactory().getConnection()) {
            String pong = conn.ping();
            log.info("Redis refresh-token store active (PING -> {}), token TTL {}s",
                    pong, refreshTokenDurationSeconds);
        } catch (Exception e) {
            log.error("Cannot connect to Redis for refresh-token store - {}", e.getMessage());
            throw new IllegalStateException("Redis connection check failed", e);
        }
    }

    @Override
    public RefreshToken createRefreshToken(User user) {
        String token = UUID.randomUUID().toString();
        Duration ttl = Duration.ofSeconds(refreshTokenDurationSeconds);

        redis.opsForValue().set(TOKEN_KEY + token, String.valueOf(user.getId()), ttl);
        String userKey = USER_KEY + user.getId();
        redis.opsForSet().add(userKey, token);
        redis.expire(userKey, ttl);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(token);
        refreshToken.setExpiryDate(Instant.now().plusSeconds(refreshTokenDurationSeconds));
        return refreshToken;
    }

    @Override
    public User validateRefreshToken(String token) throws AuthorisationException {
        String userIdStr = redis.opsForValue().get(TOKEN_KEY + token);
        if (userIdStr == null) {
            // covers both unknown and expired tokens (Redis TTL evicts expired keys)
            log.warn("Refresh token rejected: unknown or expired");
            throw new AuthorisationException("Invalid or expired refresh token. Please log in again.");
        }

        Long userId = Long.valueOf(userIdStr);
        User user = userRepo.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Refresh token rejected: user#{} no longer exists", userId);
                    redis.delete(TOKEN_KEY + token);
                    return new AuthorisationException("User account has been deleted");
                });

        if (user.getDeleted()) {
            log.warn("Refresh token rejected: user#{} is deleted, revoking all tokens", userId);
            deleteByUser(user);
            throw new AuthorisationException("User account has been deleted");
        }

        return user;
    }

    @Override
    public void deleteByUser(User user) {
        String userKey = USER_KEY + user.getId();
        Set<String> tokens = redis.opsForSet().members(userKey);
        if (tokens != null) {
            for (String t : tokens) {
                redis.delete(TOKEN_KEY + t);
            }
        }
        redis.delete(userKey);
        log.info("Revoked {} refresh token(s) for user#{}",
                tokens != null ? tokens.size() : 0, user.getId());
    }
}
