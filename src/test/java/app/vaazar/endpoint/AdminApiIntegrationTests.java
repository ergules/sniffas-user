package app.vaazar.endpoint;

import app.vaazar.domain.approval.entity.ApplicationType;
import app.vaazar.domain.approval.entity.Approval;
import app.vaazar.domain.approval.entity.ApprovalStatus;
import app.vaazar.domain.deleteAccount.entity.DeleteAccountRequest;
import app.vaazar.domain.deleteAccount.entity.DeleteRequestStatus;
import app.vaazar.domain.user.entity.Role;
import app.vaazar.domain.user.entity.User;
import app.vaazar.endpoint.dto.BasicUser;
import app.vaazar.endpoint.dto.approval.ApprovalDTO;
import app.vaazar.endpoint.dto.deleteAccount.DeleteAccountRequestDTO;
import app.vaazar.endpoint.dto.user.UserDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static app.vaazar.TestDataHelper.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class AdminApiIntegrationTests {

    @Autowired
    AdminApi adminApi;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void registerAdmin() {
        User admin = getAdmin(entityManager);
        registerToSecurityContext(admin);
    }

    @Test
    public void findUsers_whenNoQueryReturnAll() {
        Page<BasicUser> resultPage = adminApi
                .findUsers(Optional.empty(), false, PageRequest.ofSize(20));

        assertEquals(7, resultPage.getTotalElements());
        assertEquals(1L, resultPage.getContent().get(0).getId());
        assertEquals(6L, resultPage.getContent().get(3).getId());
    }

    @Test
    public void findUsers_filterResultsByQueryOrSeller() {
        Page<BasicUser> resultPage = adminApi
                .findUsers(Optional.empty(), true, PageRequest.ofSize(20));

        assertEquals(3, resultPage.getTotalElements());
    }

    @Test
    public void findUser() {
        UserDTO result_1 = adminApi.findUser(1L).getBody();
        assert result_1 != null;
        assertEquals(1L, result_1.getId());
        assertEquals(Role.USER, result_1.getRole());

        UserDTO result_5 = adminApi.findUser(5L).getBody();
        assert result_5 != null;
        assertEquals(5L, result_5.getId());
        assertTrue(result_5.getDeleted());
    }

    @Test
    public void findApprovals() {
        Approval approval = new Approval();
        approval.setApprovalStatus(ApprovalStatus.PENDING);
        approval.setApplicationType(ApplicationType.PRIVATE);
        entityManager.persist(approval);
        entityManager.clear();

        List<ApprovalDTO> resultList = adminApi.findApprovals(Optional.empty());
        assertNotNull(resultList);
        assertEquals(1, resultList.size());
        assertEquals(approval.getApprovalStatus(), resultList.get(0).getApprovalStatus());
        assertEquals(approval.getApplicationType(), resultList.get(0).getApplicationType());
    }

    @Test
    public void respondToApproval_whenApprovedUpdateUserRole() {
        User requester = entityManager.find(User.class, 1L);
        Approval approval = new Approval();
        approval.setApprovalStatus(ApprovalStatus.PENDING);
        approval.setApplicationType(ApplicationType.PRIVATE);
        approval.setRequester(requester);
        entityManager.persist(approval);
        entityManager.clear();

        ApprovalDTO adminResponse = new ApprovalDTO();
        adminResponse.setApprovalStatus(ApprovalStatus.APPROVED);
        adminResponse.setDescription("OK");

        ApprovalDTO result = adminApi
                .respondToApproval(adminResponse, approval.getId());

        assertEquals(approval.getId(), result.getId());
        assertEquals(ApprovalStatus.APPROVED, result.getApprovalStatus());

        User requesterUpdated = entityManager.find(User.class, 1L);
        assertEquals(Role.SELLER, requesterUpdated.getRole());
    }

    @Test
    public void getDeleteAccountRequests() {
        Page<DeleteAccountRequestDTO> resultPage = adminApi
                .getDeleteAccountRequests(PageRequest.ofSize(20), List.of(DeleteRequestStatus.NEW));

        assertEquals(0, resultPage.getTotalElements());
        assertEquals(0, resultPage.getContent().size());
    }

    @Test
    public void acceptDeleteRequest() {
        User seller6 = getPrivateSellers(entityManager).get(0);
        DeleteAccountRequest deleteRequest = new DeleteAccountRequest();
        deleteRequest.setStatus(DeleteRequestStatus.NEW);
        deleteRequest.setRequester(seller6);
        entityManager.persist(deleteRequest);
        entityManager.clear();

        Instant instant = Instant.now().plus(1, ChronoUnit.HOURS).truncatedTo(ChronoUnit.SECONDS);
        DeleteAccountRequestDTO dto = new DeleteAccountRequestDTO();
        dto.setAutoDeleteDate(instant);

        DeleteAccountRequestDTO resultDTO = adminApi.acceptDeleteRequest(deleteRequest.getId(), dto);
        assertEquals(deleteRequest.getId(), resultDTO.getId());
        assertEquals(instant, resultDTO.getAutoDeleteDate());
        entityManager.flush();
        entityManager.clear();

        DeleteAccountRequest updatedRequest = entityManager.find(DeleteAccountRequest.class, deleteRequest.getId());
        assertEquals(DeleteRequestStatus.AUTO_DELETE_ASSIGNED, updatedRequest.getStatus());
        assertEquals(instant, updatedRequest.getAutoDeleteDate());
    }


    @Test
    public void discardDeleteRequest() {
        User seller6 = getPrivateSellers(entityManager).get(0);
        DeleteAccountRequest deleteRequest = new DeleteAccountRequest();
        deleteRequest.setStatus(DeleteRequestStatus.NEW);
        deleteRequest.setRequester(seller6);
        entityManager.persist(deleteRequest);
        entityManager.clear();

        adminApi.discardDeleteRequest(deleteRequest.getId());
        entityManager.flush();
        entityManager.clear();

        assertNull(entityManager.find(DeleteAccountRequest.class, deleteRequest.getId()));
    }

}
