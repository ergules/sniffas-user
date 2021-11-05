package app.vaazar.Domain.User.Control;


import app.vaazar.Domain.User.Entity.User;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Long> {
    User findByUsername(String username);
    Optional<User> findByFirebaseUid(String firebaseUid);
    boolean existsByUsername(String username);
}

