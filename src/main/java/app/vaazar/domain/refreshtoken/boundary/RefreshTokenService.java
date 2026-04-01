package app.vaazar.domain.refreshtoken.boundary;

import app.vaazar.config.exception.AuthorisationException;
import app.vaazar.domain.refreshtoken.entity.RefreshToken;
import app.vaazar.domain.user.entity.User;

public interface RefreshTokenService {

    RefreshToken createRefreshToken(User user);

    User validateRefreshToken(String token) throws AuthorisationException;

    void deleteByUser(User user);
}
