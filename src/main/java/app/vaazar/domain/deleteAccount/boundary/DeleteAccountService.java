package app.vaazar.domain.deleteAccount.boundary;

import app.vaazar.domain.deleteAccount.entity.DeleteAccountRequest;
import app.vaazar.domain.deleteAccount.entity.DeleteRequestStatus;
import app.vaazar.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DeleteAccountService {
    DeleteAccountRequest requestToDeleteAccount(User user);

    Page<DeleteAccountRequest> listDeleteRequests(List<DeleteRequestStatus> statuses, Pageable pageable);

    DeleteAccountRequest scheduleDelete(DeleteAccountRequest deleteRequest);

    void discardRequest(Long requestId);

    void handleDueDeletes();
}
