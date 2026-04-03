package app.vaazar.domain.refreshtoken.boundary.impl;

import app.vaazar.config.exception.AuthorisationException;
import app.vaazar.domain.refreshtoken.boundary.RefreshTokenService;
import app.vaazar.domain.refreshtoken.control.RefreshTokenRepository;
import app.vaazar.domain.refreshtoken.entity.RefreshToken;
import app.vaazar.domain.user.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@Transactional
@ConditionalOnProperty(name = "app.redis.active", havingValue = "false", matchIfMissing = true)
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepo;
    private final long refreshTokenDurationSeconds;

    public RefreshTokenServiceImpl(
            RefreshTokenRepository refreshTokenRepo,
            @Value("${app.refresh-token-expiration-seconds:2592000}") long refreshTokenDurationSeconds) {
        this.refreshTokenRepo = refreshTokenRepo;
        this.refreshTokenDurationSeconds = refreshTokenDurationSeconds;
    }

    public RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(Instant.now().plusSeconds(refreshTokenDurationSeconds));
        return refreshTokenRepo.save(refreshToken);
    }

    public User validateRefreshToken(String token) throws AuthorisationException {
        RefreshToken refreshToken = refreshTokenRepo.findByToken(token)
                .orElseThrow(() -> new AuthorisationException("Invalid refresh token"));

        if (refreshToken.isExpired()) {
            refreshTokenRepo.delete(refreshToken);
            throw new AuthorisationException("Refresh token has expired. Please log in again.");
        }

        User user = refreshToken.getUser();
        if (user.getDeleted()) {
            refreshTokenRepo.deleteByUser(user);
            throw new AuthorisationException("User account has been deleted");
        }

        return user;
    }

    public void deleteByUser(User user) {
        refreshTokenRepo.deleteByUser(user);
    }
}
