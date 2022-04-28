package app.vaazar.Domain.DeleteAccount.Control;

import app.vaazar.Domain.DeleteAccount.Entity.DeleteAccountRequest;
import app.vaazar.Domain.DeleteAccount.Entity.DeleteRequestStatus;
import app.vaazar.Domain.User.Entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface DeleteRequestRepository extends JpaRepository<DeleteAccountRequest, Long> {

    Optional<DeleteAccountRequest> findByRequester(User requester);

    Page<DeleteAccountRequest> findByStatusIn(List<DeleteRequestStatus> statuses, Pageable pageable);

    @Query("select d from DeleteAccountRequest d join fetch d.requester " +
            "where d.autoDeleteDate < ?1 and d.status = 'AUTO_DELETE_ASSIGNED'")
    List<DeleteAccountRequest> findDueRequests(Instant time);
}
