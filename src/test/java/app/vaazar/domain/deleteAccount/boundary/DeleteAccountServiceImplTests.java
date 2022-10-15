package app.vaazar.domain.deleteAccount.boundary;

import app.vaazar.domain.deleteAccount.boundary.impl.DeleteAccountServiceImpl;
import app.vaazar.domain.deleteAccount.control.DeleteRequestRepository;
import app.vaazar.domain.deleteAccount.entity.DeleteAccountRequest;
import app.vaazar.domain.deleteAccount.entity.DeleteRequestStatus;
import app.vaazar.domain.user.control.UserRepository;
import app.vaazar.domain.user.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static app.vaazar.domain.deleteAccount.entity.DeleteRequestStatus.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
public class DeleteAccountServiceImplTests {

    @Mock
    DeleteRequestRepository deleteRequestRepo;
    @Mock
    UserRepository userRepo;
    @Mock
    ApplicationEventPublisher eventPublisher;
    @Mock
    Logger log;
    @InjectMocks
    DeleteAccountServiceImpl service;

    @Test
    public void requestToDeleteAccount_happy() {
        long userId = 10;

        User requester = new User();
        requester.setId(userId);

        when(deleteRequestRepo.findByRequester(any())).thenReturn(Optional.empty());
        when(deleteRequestRepo.save(any())).thenAnswer(i -> i.getArguments()[0]);

        DeleteAccountRequest saved = service.requestToDeleteAccount(requester);
        assertEquals(saved.getRequester().getId(), userId);
        assertEquals(saved.getStatus(), DeleteRequestStatus.NEW);
    }

    @Test
    public void requestToDeleteAccount_whenAnotherExistsThenReturnExisting() {
        long pastId = 5;
        DeleteAccountRequest pastRequest = new DeleteAccountRequest();
        pastRequest.setId(pastId);
        when(deleteRequestRepo.findByRequester(any())).thenReturn(Optional.of(pastRequest));

        assertEquals(service.requestToDeleteAccount(new User()).getId(), pastId);
    }

    @Test
    public void listDeleteRequests_filterByStatus() {
        service.listDeleteRequests(List.of(DeleteRequestStatus.NEW), Pageable.unpaged());
        verify(deleteRequestRepo).findByStatusIn(List.of(DeleteRequestStatus.NEW), Pageable.unpaged());
    }

    @Test
    public void listDeleteRequests_all() {
        service.listDeleteRequests(Collections.emptyList(), Pageable.unpaged());
        verify(deleteRequestRepo).findAll(Pageable.unpaged());
    }

    @Test
    public void scheduleDelete_whenStatusWrong_thenThrowIllegalStateException() {
        long requestId = 15;

        DeleteAccountRequest original = new DeleteAccountRequest();
        original.setId(requestId);
        original.setStatus(AUTO_DELETE_COMPLETED);

        DeleteAccountRequest adminResp = new DeleteAccountRequest();
        adminResp.setId(requestId);
        adminResp.setAutoDeleteDate(Instant.now());

        when(deleteRequestRepo.getById(requestId)).thenReturn(original);

        assertThrows(IllegalStateException.class, () ->
                service.scheduleDelete(adminResp));
    }

    @Test
    public void scheduleDelete_whenRequestHasMissingInfo_thenThrowIllegalArgumentException() {
        long requestId = 15;

        DeleteAccountRequest original = new DeleteAccountRequest();
        original.setId(requestId);
        original.setStatus(NEW);

        DeleteAccountRequest adminResp = new DeleteAccountRequest();
        adminResp.setId(requestId);
        adminResp.setAutoDeleteDate(null);

        when(deleteRequestRepo.getById(requestId)).thenReturn(original);

        assertThrows(IllegalArgumentException.class, () ->
                service.scheduleDelete(adminResp));
    }

    @Test
    public void scheduleDelete_happy() {
        long requestId = 15;
        Instant _now = Instant.now();

        DeleteAccountRequest original = new DeleteAccountRequest();
        original.setId(requestId);
        original.setStatus(NEW);

        DeleteAccountRequest adminResp = new DeleteAccountRequest();
        adminResp.setId(requestId);
        adminResp.setAutoDeleteDate(_now);

        when(deleteRequestRepo.getById(requestId)).thenReturn(original);
        when(deleteRequestRepo.save(any())).thenAnswer(i -> i.getArguments()[0]);

        DeleteAccountRequest saved = service.scheduleDelete(adminResp);
        assertEquals(saved.getStatus(), AUTO_DELETE_ASSIGNED);
        assertEquals(saved.getAutoDeleteDate(), _now);
    }

    @Test
    public void discardRequest() {
        long requestId = 15;

        DeleteAccountRequest original = new DeleteAccountRequest();
        original.setId(requestId);

        when(deleteRequestRepo.getById(requestId)).thenReturn(original);

        service.discardRequest(requestId);
        verify(deleteRequestRepo).delete(original);
    }

    @Test
    public void handleDueDeletes() {
        DeleteAccountRequest timely1 = new DeleteAccountRequest();
        timely1.setRequester(new User());

        DeleteAccountRequest timely2 = new DeleteAccountRequest();
        timely2.setRequester(new User());

        when(deleteRequestRepo.findDueRequests(any()))
                .thenReturn(List.of(timely1, timely2));

        service.handleDueDeletes();
        verify(userRepo, times(2)).save(any());
        verify(deleteRequestRepo, times(2)).save(any());

        assertTrue(timely1.getRequester().getDeleted());
        assertTrue(timely2.getRequester().getDeleted());
        assertSame(timely1.getStatus(), AUTO_DELETE_COMPLETED);
        assertSame(timely2.getStatus(), AUTO_DELETE_COMPLETED);
    }

}
