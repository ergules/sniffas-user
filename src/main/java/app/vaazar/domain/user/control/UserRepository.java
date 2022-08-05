package app.vaazar.domain.user.control;


import app.vaazar.domain.user.entity.User;
import app.vaazar.endpoint.dto.BasicUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static app.vaazar.domain.user.entity.User.BASIC_USERS_IN_LIST;

/**
 *  when used named queries sort params are neglected:
 *  XX is backed by a NamedQuery but contains a Pageable parameter! Sorting delivered via this Pageable will not be applied!
 */
public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);

    Optional<User> findByFirebaseUid(String firebaseUid);

    boolean existsByUsername(String username);

    @Query(name = BASIC_USERS_IN_LIST, nativeQuery = true)
    List<BasicUser> findBasicUsersInList(Collection<Long> ids);

    @Query("SELECT new User(u.id, u.username, u.firstname, u.lastname, u.profilePhoto, u.role) FROM User u " +
            "WHERE u.deleted <> true  AND (:sellerOnly = false OR u.role in ('SELLER', 'COMPANY')) ")
    Page<User> findBasicUsers(boolean sellerOnly, Pageable pageable);

    @Query("SELECT new User(u.id, u.username, u.firstname, u.lastname, u.profilePhoto, u.role) FROM User u " +
            "WHERE u.deleted <> true  AND " +
            "(lower(concat(u.firstname, ' ', u.lastname)) LIKE lower(concat('%', :qry, '%'))" +
            " or lower(u.username) like lower(concat('%',:qry,'%')))  " +
            "AND (:sellerOnly = false OR u.role in ('SELLER', 'COMPANY')) ")
    Page<User> findBasicUsersByName(boolean sellerOnly, String qry, Pageable pageable);

    @Query("SELECT new User(u.id, u.username, u.firstname, u.lastname, u.profilePhoto, u.role) FROM User u " +
            "WHERE u.deleted <> true AND (u.email LIKE lower(concat('%',:qry,'%'))) AND " +
            "(:sellerOnly = false OR u.role in ('SELLER', 'COMPANY')) ")
    Page<User> findBasicUsersByEmail(boolean sellerOnly, String qry, Pageable pageable);
}

