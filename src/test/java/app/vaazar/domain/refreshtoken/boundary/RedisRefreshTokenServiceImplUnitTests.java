package app.vaazar.domain.refreshtoken.boundary;

import app.vaazar.config.exception.AuthorisationException;
import app.vaazar.domain.refreshtoken.boundary.impl.RedisRefreshTokenServiceImpl;
import app.vaazar.domain.refreshtoken.entity.RefreshToken;
import app.vaazar.domain.user.control.UserRepository;
import app.vaazar.domain.user.entity.Role;
import app.vaazar.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RedisRefreshTokenServiceImplUnitTests {

    @Mock
    StringRedisTemplate redis;
    @Mock
    ValueOperations<String, String> valueOps;
    @Mock
    SetOperations<String, String> setOps;
    @Mock
    UserRepository userRepo;

    RedisRefreshTokenServiceImpl sut;

    final long ttl = 3600L;

    @BeforeEach
    void setUp() {
        lenient().when(redis.opsForValue()).thenReturn(valueOps);
        lenient().when(redis.opsForSet()).thenReturn(setOps);
        sut = new RedisRefreshTokenServiceImpl(redis, userRepo, ttl,
                LoggerFactory.getLogger(RedisRefreshTokenServiceImpl.class));
    }

    @Test
    void verifyRedisConnection_failsFastWhenUnreachable() {
        RedisConnectionFactory factory = mock(RedisConnectionFactory.class);
        when(redis.getRequiredConnectionFactory()).thenReturn(factory);
        when(factory.getConnection()).thenThrow(new RuntimeException("Connection refused"));

        assertThrows(IllegalStateException.class, () -> sut.verifyRedisConnection());
    }

    @Test
    void verifyRedisConnection_okWhenPingSucceeds() {
        RedisConnectionFactory factory = mock(RedisConnectionFactory.class);
        RedisConnection conn = mock(RedisConnection.class);
        when(redis.getRequiredConnectionFactory()).thenReturn(factory);
        when(factory.getConnection()).thenReturn(conn);
        when(conn.ping()).thenReturn("PONG");

        assertDoesNotThrow(() -> sut.verifyRedisConnection());
        verify(conn).close();
    }

    @Test
    void createRefreshToken_storesTokenAndUserIndexWithTtl() {
        User user = new User(37L, "e@m.com", "un", Role.USER);

        RefreshToken result = sut.createRefreshToken(user);

        assertNotNull(result.getToken());
        assertEquals(user, result.getUser());
        verify(valueOps).set(eq("refresh:token:" + result.getToken()), eq("37"), eq(Duration.ofSeconds(ttl)));
        verify(setOps).add("refresh:user:37", result.getToken());
        verify(redis).expire("refresh:user:37", Duration.ofSeconds(ttl));
    }

    @Test
    void validateRefreshToken_happy() throws AuthorisationException {
        User user = new User(37L, "e@m.com", "un", Role.USER);
        when(valueOps.get("refresh:token:abc")).thenReturn("37");
        when(userRepo.findById(37L)).thenReturn(Optional.of(user));

        User result = sut.validateRefreshToken("abc");

        assertSame(user, result);
    }

    @Test
    void validateRefreshToken_unknownOrExpired() {
        when(valueOps.get("refresh:token:abc")).thenReturn(null);

        assertThrows(AuthorisationException.class, () -> sut.validateRefreshToken("abc"));
        verifyNoInteractions(userRepo);
    }

    @Test
    void validateRefreshToken_deletedUserRevokesAll() {
        User user = new User(37L, "e@m.com", "un", Role.USER);
        user.setDeleted(true);
        when(valueOps.get("refresh:token:abc")).thenReturn("37");
        when(userRepo.findById(37L)).thenReturn(Optional.of(user));
        when(setOps.members("refresh:user:37")).thenReturn(Set.of("abc", "def"));

        assertThrows(AuthorisationException.class, () -> sut.validateRefreshToken("abc"));

        verify(redis).delete("refresh:token:abc");
        verify(redis).delete("refresh:token:def");
        verify(redis).delete("refresh:user:37");
    }

    @Test
    void deleteByUser_removesAllTokens() {
        User user = new User(37L, "e@m.com", "un", Role.USER);
        when(setOps.members("refresh:user:37")).thenReturn(Set.of("t1", "t2"));

        sut.deleteByUser(user);

        verify(redis).delete("refresh:token:t1");
        verify(redis).delete("refresh:token:t2");
        verify(redis).delete("refresh:user:37");
    }
}
