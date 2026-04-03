package app.vaazar.domain.refreshtoken.boundary;

import app.vaazar.config.exception.AuthorisationException;
import app.vaazar.domain.refreshtoken.boundary.impl.RedisRefreshTokenServiceImpl;
import app.vaazar.domain.refreshtoken.entity.RefreshToken;
import app.vaazar.domain.user.control.UserRepository;
import app.vaazar.domain.user.entity.Role;
import app.vaazar.domain.user.entity.User;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import redis.embedded.RedisServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@DataRedisTest
@ActiveProfiles("test")
public class RedisRefreshTokenServiceImplIntegrationTests {

    private static RedisServer redisServer;
    private static int redisPort;
    private static final String REDIS_PASSWORD = "itest-pw";

    private final StringRedisTemplate redisTemplate;
    private final UserRepository userRepo;
    private final RedisRefreshTokenServiceImpl service;

    @BeforeAll
    static void startRedis() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            redisPort = socket.getLocalPort();
        }
        redisServer = RedisServer.newRedisServer()
                .port(redisPort)
                .setting("requirepass " + REDIS_PASSWORD)
                .setting("maxmemory 32M")
                .build();
        redisServer.start();
    }

    @AfterAll
    static void stopRedis() throws IOException {
        if (redisServer != null) redisServer.stop();
    }

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.redis.host", () -> "localhost");
        registry.add("spring.redis.port", () -> redisPort);
        registry.add("spring.redis.password", () -> REDIS_PASSWORD);
    }

    @BeforeEach
    void cleanRedis() {
        redisTemplate.getRequiredConnectionFactory().getConnection().flushAll();
        Mockito.reset(userRepo);
    }

    @Test
    public void verifyRedisConnection() {
        assertDoesNotThrow(service::verifyRedisConnection);
    }

    @Test
    public void createRefreshToken_writesTokenAndUserIndex() {
        User user = new User(37L, "e@m.com", "un", Role.USER);

        RefreshToken token = service.createRefreshToken(user);

        assertNotNull(token.getToken());
        assertEquals("37", redisTemplate.opsForValue().get("refresh:token:" + token.getToken()));
        assertTrue(redisTemplate.opsForSet().isMember("refresh:user:37", token.getToken()));
        assertTrue(redisTemplate.getExpire("refresh:token:" + token.getToken()) > 0);
        assertTrue(redisTemplate.getExpire("refresh:user:37") > 0);
    }

    @Test
    public void validateRefreshToken_happy() throws AuthorisationException {
        User user = new User(37L, "e@m.com", "un", Role.USER);
        when(userRepo.findById(37L)).thenReturn(Optional.of(user));

        RefreshToken token = service.createRefreshToken(user);
        User result = service.validateRefreshToken(token.getToken());

        assertSame(user, result);
    }

    @Test
    public void validateRefreshToken_unknownToken() {
        assertThrows(AuthorisationException.class,
                () -> service.validateRefreshToken("does-not-exist"));
    }

    @Test
    public void validateRefreshToken_deletedUserRevokesAll() {
        User user = new User(37L, "e@m.com", "un", Role.USER);
        user.setDeleted(true);
        when(userRepo.findById(37L)).thenReturn(Optional.of(user));

        RefreshToken t1 = service.createRefreshToken(user);
        RefreshToken t2 = service.createRefreshToken(user);

        assertThrows(AuthorisationException.class,
                () -> service.validateRefreshToken(t1.getToken()));

        assertNull(redisTemplate.opsForValue().get("refresh:token:" + t1.getToken()));
        assertNull(redisTemplate.opsForValue().get("refresh:token:" + t2.getToken()));
        assertFalse(redisTemplate.hasKey("refresh:user:37"));
    }

    @Test
    public void deleteByUser_removesAllTokens() {
        User user = new User(37L, "e@m.com", "un", Role.USER);
        RefreshToken t1 = service.createRefreshToken(user);
        RefreshToken t2 = service.createRefreshToken(user);

        service.deleteByUser(user);

        assertNull(redisTemplate.opsForValue().get("refresh:token:" + t1.getToken()));
        assertNull(redisTemplate.opsForValue().get("refresh:token:" + t2.getToken()));
        assertFalse(redisTemplate.hasKey("refresh:user:37"));
    }


    @Autowired
    public RedisRefreshTokenServiceImplIntegrationTests(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.userRepo = Mockito.mock(UserRepository.class);
        this.service = new RedisRefreshTokenServiceImpl(redisTemplate, userRepo, 3600L,
                LoggerFactory.getLogger(RedisRefreshTokenServiceImpl.class));
    }
}
