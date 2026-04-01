package app.vaazar.domain.deleteAccount.boundary;

import app.vaazar.domain.deleteAccount.boundary.impl.DeleteAccountServiceImpl;
import app.vaazar.domain.deleteAccount.control.DeleteRequestRepository;
import app.vaazar.domain.deleteAccount.entity.DeleteAccountRequest;
import app.vaazar.domain.refreshtoken.boundary.RefreshTokenService;
import app.vaazar.domain.user.control.UserRepository;
import app.vaazar.domain.user.entity.User;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static app.vaazar.TestDataHelper.getAllSellers;
import static app.vaazar.TestDataHelper.getPrivateSellers;
import static app.vaazar.domain.deleteAccount.entity.DeleteRequestStatus.*;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
public class DeleteAccountServiceImplIntegrationTests {

    private final TestEntityManager entityManager;
    private final DeleteAccountServiceImpl service;

    @Test
    public void requestToDeleteAccount() {
        User seller = getPrivateSellers(entityManager).get(0);
        DeleteAccountRequest persisted = service.requestToDeleteAccount(seller);

        assertNotNull(persisted.getId());
        assertNull(persisted.getAutoDeleteDate());
        assertEquals(NEW, persisted.getStatus());
        assertEquals(seller.getId(), persisted.getRequester().getId());
    }

    @Test
    public void listDeleteRequests() {
        List<User> sellers = getAllSellers(entityManager);
        List<DeleteAccountRequest> manuallyPersisted = new ArrayList<>(sellers.size());
        for (User seller : sellers) {
            DeleteAccountRequest request = new DeleteAccountRequest();
            request.setRequester(seller);
            request.setStatus(NEW);
            manuallyPersisted.add(entityManager.persist(request));
        }

        assertEquals(manuallyPersisted.size(),
                service.listDeleteRequests(List.of(NEW), Pageable.unpaged()).getContent().size());

        assertEquals(manuallyPersisted.size(),
                service.listDeleteRequests(List.of(NEW, AUTO_DELETE_ASSIGNED, AUTO_DELETE_COMPLETED), Pageable.unpaged())
                        .getContent().size());

        assertEquals(0,
                service.listDeleteRequests(List.of(AUTO_DELETE_ASSIGNED), Pageable.unpaged()).getContent().size());

        manuallyPersisted.get(0).setStatus(AUTO_DELETE_ASSIGNED);
        assertEquals(1,
                service.listDeleteRequests(List.of(AUTO_DELETE_ASSIGNED), Pageable.unpaged()).getContent().size());
    }

    @Test
    public void scheduleDelete() {
        User seller = getPrivateSellers(entityManager).get(0);

        DeleteAccountRequest request = new DeleteAccountRequest();
        request.setRequester(seller);
        request.setStatus(NEW);
        entityManager.persistAndFlush(request);
        entityManager.clear();

        DeleteAccountRequest adminResponse = new DeleteAccountRequest();
        adminResponse.setId(request.getId());
        adminResponse.setAutoDeleteDate(Instant.now().plus(1, ChronoUnit.DAYS));

        DeleteAccountRequest persisted = service.scheduleDelete(adminResponse);
        assertEquals(request.getId(), persisted.getId());
        assertEquals(AUTO_DELETE_ASSIGNED, persisted.getStatus());
    }

    @Test
    public void discardRequest() {
        User seller = getPrivateSellers(entityManager).get(0);

        DeleteAccountRequest request = new DeleteAccountRequest();
        request.setRequester(seller);
        request.setStatus(NEW);
        entityManager.persistAndFlush(request);
        entityManager.clear();

        service.discardRequest(request.getId());
        assertNull(entityManager.find(DeleteAccountRequest.class, request.getId()));
    }

    @Test
    public void handleDueDeletes() {
        List<User> sellers = getPrivateSellers(entityManager);
        List<DeleteAccountRequest> manuallyPersisted = new ArrayList<>(sellers.size());
        Instant deleteDate = Instant.now();
        for (User seller : sellers) {
            DeleteAccountRequest request = new DeleteAccountRequest();
            request.setRequester(seller);
            request.setStatus(AUTO_DELETE_ASSIGNED);
            request.setAutoDeleteDate(deleteDate);
            manuallyPersisted.add(entityManager.persist(request));
        }
        entityManager.clear();

        service.handleDueDeletes();
        for (User seller : getPrivateSellers(entityManager)) {
            assertTrue(seller.getDeleted());
        }
        for (DeleteAccountRequest request : manuallyPersisted) {
            assertEquals(AUTO_DELETE_COMPLETED,
                    entityManager.find(DeleteAccountRequest.class, request.getId()).getStatus());
        }
    }


    @Autowired
    public DeleteAccountServiceImplIntegrationTests(TestEntityManager entityManager,
                                                    DeleteRequestRepository deleteRequestRepo,
                                                    UserRepository userRepo) {

        Logger logger = LoggerFactory.getLogger(DeleteAccountServiceImpl.class);
        this.entityManager = entityManager;
        this.service = new DeleteAccountServiceImpl(deleteRequestRepo, userRepo, Mockito.mock(RefreshTokenService.class), Mockito.mock(ApplicationEventPublisher.class), logger);
    }
}
