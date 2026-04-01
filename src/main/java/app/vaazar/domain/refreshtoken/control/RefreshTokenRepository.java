package app.vaazar.domain.refreshtoken.control;

import app.vaazar.domain.refreshtoken.entity.RefreshToken;
import app.vaazar.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByUser(User user);
}
