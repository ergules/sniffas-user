package app.vaazar.domain.user.boundary;


import app.vaazar.config.exception.AuthorisationException;
import app.vaazar.domain.deleteAccount.entity.DeleteAccountRequest;
import app.vaazar.domain.user.entity.User;
import app.vaazar.endpoint.dto.BasicUser;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface UserService {

    User findById(Long id);

    boolean isUsernameAvailable(String username);

    User saveUser(User user);

    List<BasicUser> findBasicUsers(List<Long> idList);

    User updateUserInfo(User user) throws AuthorisationException;

    User registerUser(User user, FirebaseToken token) throws AuthorisationException;

    DeleteAccountRequest deleteUser(Long userId);

    Optional<User> findByUid(String uid);

    Page<User> findUsers(Optional<String> query, boolean seller, Pageable page);
}
