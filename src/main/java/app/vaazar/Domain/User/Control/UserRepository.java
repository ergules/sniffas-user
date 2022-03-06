package app.vaazar.Domain.User.Control;


import app.vaazar.Domain.User.Entity.User;
import app.vaazar.Endpoint.Dto.BasicUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static app.vaazar.Domain.User.Entity.User.*;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);

    Optional<User> findByFirebaseUid(String firebaseUid);

    boolean existsByUsername(String username);

    @Query(name = BASIC_USERS_IN_LIST, nativeQuery = true)
    List<BasicUser> findBasicUsersInList(Collection<Long> ids);

    @Query(name = BASIC_USERS_ALL, nativeQuery = true)
    Page<BasicUser> findBasicUsers(Pageable pageable);

    @Query(name = BASIC_USERS_BY_NAME_QUERY, nativeQuery = true)
    Page<BasicUser> findBasicUsersByName(String qry, Pageable pageable);

    @Query(name = BASIC_USERS_BY_EMAIL_QUERY, nativeQuery = true)
    Page<BasicUser> findBasicUsersByEmail(String qry, Pageable pageable);

    @Query(name = BASIC_SELLERS_ALL, nativeQuery = true)
    Page<BasicUser> findBasicSellers(Pageable pageable);

    @Query(name = BASIC_SELLERS_BY_NAME_QUERY, nativeQuery = true)
    Page<BasicUser> findBasicSellersByName(String qry, Pageable pageable);

    @Query(name = BASIC_SELLERS_BY_EMAIL_QUERY, nativeQuery = true)
    Page<BasicUser> findBasicSellersByEmail(String qry, Pageable pageable);
}

