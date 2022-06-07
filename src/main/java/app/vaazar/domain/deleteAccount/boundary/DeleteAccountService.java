package app.vaazar.domain.deleteAccount.boundary;

import app.vaazar.domain.deleteAccount.control.DeleteRequestRepository;
import app.vaazar.domain.deleteAccount.entity.DeleteAccountRequest;
import app.vaazar.domain.deleteAccount.entity.DeleteRequestStatus;
import app.vaazar.domain.user.control.UserRepository;
import app.vaazar.domain.user.entity.User;
import org.slf4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static app.vaazar.domain.deleteAccount.entity.DeleteRequestStatus.*;


@Service
@Transactional
public class DeleteAccountService {

    private final DeleteRequestRepository deleteRequestRepo;
    private final UserRepository userRepo;
    private final Logger log;

    public DeleteAccountRequest requestToDeleteAccount(User user) {
        log.info("User#{} requested to delete account", user.getId());
        Optional<DeleteAccountRequest> requestOptional = deleteRequestRepo.findByRequester(user);
        if (requestOptional.isPresent())
            return requestOptional.get();
        DeleteAccountRequest deleteRequest = new DeleteAccountRequest();
        deleteRequest.setRequester(user);
        deleteRequest.setStatus(DeleteRequestStatus.NEW);
        return deleteRequestRepo.save(deleteRequest);
    }

    public Page<DeleteAccountRequest> listDeleteRequests(List<DeleteRequestStatus> statuses, Pageable pageable) {
        return statuses.size() > 0
                ? deleteRequestRepo.findByStatusIn(statuses, pageable)
                : deleteRequestRepo.findAll(pageable);
    }

    public DeleteAccountRequest scheduleDelete(DeleteAccountRequest deleteRequest) {
        DeleteAccountRequest persisted = deleteRequestRepo.getById(deleteRequest.getId());
        if (persisted.getStatus() != NEW && persisted.getStatus() != AUTO_DELETE_ASSIGNED) {
            throw new IllegalStateException("Request must be NEW or ASSIGNED");
        }
        if (deleteRequest.getAutoDeleteDate() == null)
            throw new IllegalArgumentException("Provide date for auto deletion");

        log.info("Accepting/modifying delete request#{}, auto delete after {}",
                deleteRequest.getId(), deleteRequest.getAutoDeleteDate());
        persisted.setStatus(AUTO_DELETE_ASSIGNED);
        persisted.setAutoDeleteDate(deleteRequest.getAutoDeleteDate());
        return deleteRequestRepo.save(persisted);
    }

    public void discardRequest(Long requestId) {
        DeleteAccountRequest persisted = deleteRequestRepo.getById(requestId);
        log.info("Deleting delete request {}", persisted);
        deleteRequestRepo.delete(persisted);
    }

    public void handleDueDeletes() {
        List<DeleteAccountRequest> requests = deleteRequestRepo.findDueRequests(Instant.now());
        log.info("Found due requests size: {}", requests.size());
        for (DeleteAccountRequest request : requests) {
            request.setStatus(AUTO_DELETE_COMPLETED);
            request.getRequester().setDeleted(true);
            userRepo.save(request.getRequester());
            deleteRequestRepo.save(request);
        }
    }

    public DeleteAccountService(DeleteRequestRepository deleteRequestRepo, UserRepository userRepo, Logger log) {
        this.deleteRequestRepo = deleteRequestRepo;
        this.userRepo = userRepo;
        this.log = log;
    }
}
