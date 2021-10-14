package app.vaazar.Domain.User.Boundary;


import app.vaazar.Domain.User.Control.UserRepository;
import app.vaazar.Domain.User.Entity.Role;
import app.vaazar.Domain.User.Entity.User;
import com.google.firebase.auth.FirebaseToken;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;

@Service
@Transactional
public class UserService {

    private UserRepository userRepo;

    public User saveUser(User user) {
        return userRepo.save(user);
    }

    public User updateUserInfo(User user) {
        User toUpdate = userRepo.findById(user.getId()).orElseThrow();
        toUpdate.updateWithEntity(user);
        return userRepo.save(toUpdate);
    }

    public User registerUser(User user, FirebaseToken token) {
        if(token.getEmail() != null && !user.getEmail().equalsIgnoreCase(token.getEmail()))
            throw new IllegalStateException("emails do not match");
        user.setEmail(user.getEmail()); //ensure lowerCase
        user.setFirebaseUid(token.getUid());
        user.setRole(Role.USER);
        return saveUser(user);
    }

    public User findUserById(Long id) {
        return userRepo.findById(id).orElse(null);
    }

    public Optional<User> findByUid(String uid) {
        return userRepo.findByFirebaseUid(uid);
    }

    public UserService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }
}
