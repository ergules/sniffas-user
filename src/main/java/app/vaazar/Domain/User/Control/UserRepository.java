package app.vaazar.Domain.User.Control;


import app.vaazar.Domain.User.Entity.User;
import app.vaazar.Endpoint.Dto.BasicUser;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static app.vaazar.Domain.User.Entity.User.BASIC_USERS_IN_LIST;

public interface UserRepository extends CrudRepository<User, Long> {
    User findByUsername(String username);

    Optional<User> findByFirebaseUid(String firebaseUid);

    boolean existsByUsername(String username);

    @Query(name = BASIC_USERS_IN_LIST, nativeQuery = true)
    List<BasicUser> findBasicUsersInList(Collection<Long> ids);
}

