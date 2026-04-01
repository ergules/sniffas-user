package app.vaazar.domain.approval.boundary;

import app.vaazar.domain.approval.boundary.impl.ApprovalServiceImpl;
import app.vaazar.domain.approval.controller.ApprovalRepository;
import app.vaazar.domain.approval.entity.Approval;
import app.vaazar.domain.company.boundary.CompanyService;
import app.vaazar.domain.company.boundary.impl.CompanyServiceImpl;
import app.vaazar.domain.company.control.CompanyRepository;
import app.vaazar.domain.user.boundary.UserService;
import app.vaazar.domain.user.boundary.impl.UserServiceImpl;
import app.vaazar.domain.user.control.UserRepository;
import app.vaazar.domain.user.entity.Role;
import app.vaazar.domain.user.entity.User;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static app.vaazar.TestDataHelper.*;
import static app.vaazar.domain.approval.entity.ApplicationType.COMPANY;
import static app.vaazar.domain.approval.entity.ApplicationType.PRIVATE;
import static app.vaazar.domain.approval.entity.ApprovalStatus.*;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
public class ApprovalServiceImplIntegrationTests {

    private final TestEntityManager entityManager;
    private final ApprovalServiceImpl service;


    @Test
    public void getApproval() {
        User user2 = entityManager.find(User.class, 2L);
        Approval approval = new Approval();
        approval.setRequester(user2);
        approval.setApplicationType(PRIVATE);
        approval.setApprovalStatus(PENDING);
        entityManager.persist(approval);
        entityManager.clear();

        Approval found = service.getApproval(approval.getId());
        assertEquals(approval.getId(), found.getId());
        assertEquals(approval.getApplicationType(), found.getApplicationType());
        assertEquals(approval.getApprovalStatus(), found.getApprovalStatus());

        assertThrows(NoSuchElementException.class, () -> service.getApproval(99L));
    }

    @Test
    public void getApprovalsOfUser() {
        persist3Approvals();

        List<Approval> user1Approvals = service.getApprovalsOfUser(1L);
        List<Approval> user2Approvals = service.getApprovalsOfUser(2L);
        List<Approval> user3Approvals = service.getApprovalsOfUser(3L);

        assertEquals(1, user1Approvals.size());
        assertEquals(2, user2Approvals.size());
        assertEquals(0, user3Approvals.size());

        assertEquals(1L, user1Approvals.get(0).getRequester().getId());
        assertEquals(REJECTED, user1Approvals.get(0).getApprovalStatus());
        assertEquals(2L, user2Approvals.get(0).getRequester().getId());
    }

    @Test
    public void listApprovals() {
        persist3Approvals();
        List<Approval> pendingApprovals = service.listApprovals(Optional.empty());
        List<Approval> rejectedApprovals = service.listApprovals(Optional.of(REJECTED));

        assertEquals(1, pendingApprovals.size());
        assertEquals(2, rejectedApprovals.size());
        assertEquals(REJECTED, rejectedApprovals.get(0).getApprovalStatus());
    }

    @Test
    public void requestSellerApproval_privateSeller() {
        String iban = "DE1212341234123412";
        User user = new User();
        user.updateBaseFields(entityManager.find(User.class, 1L));
        user.setId(1L);
        user.setIBAN(iban);
        Approval approval = service.requestSellerApproval(PRIVATE, user);

        assertNotNull(approval.getId());
        assertEquals(PENDING, approval.getApprovalStatus());
        assertEquals(PRIVATE, approval.getApplicationType());
        assertEquals(iban, approval.getRequester().getIBAN());
    }

    @Test
    public void requestSellerApproval_whenPendingExistsThrowException() {
        persist3Approvals(); // persists pending for u2

        User user = new User();
        user.updateBaseFields(entityManager.find(User.class, 2L));
        user.setId(2L);

        assertThrows(IllegalStateException.class, () -> service.requestSellerApproval(PRIVATE, user));
    }

    @Test
    public void requestSellerApproval_company() {
        String iban = "DE1212341234123412";
        User user = new User();
        user.updateBaseFields(entityManager.find(User.class, 1L));
        user.setId(1L);
        user.setIBAN(iban);
        user.setCompany(generateValidCompany());

        Approval approval = service.requestSellerApproval(COMPANY, user);

        assertNotNull(approval.getId());
        assertNotNull(approval.getRequester());
        assertNotNull(approval.getRequester().getCompany().getId());
        assertEquals(PENDING, approval.getApprovalStatus());
        assertEquals(COMPANY, approval.getApplicationType());
        assertEquals(iban, approval.getRequester().getIBAN());
        assertEquals(user.getCompany().getCompanyType(), approval.getRequester().getCompany().getCompanyType());
    }

    @Test
    public void respondToSellerApproval_approvePrivateSeller() {
        User user1 = entityManager.find(User.class, 1L);
        Approval approval_1_1 = new Approval();
        approval_1_1.setRequester(user1);
        approval_1_1.setApplicationType(PRIVATE);
        approval_1_1.setApprovalStatus(PENDING);
        entityManager.persist(approval_1_1);

        User admin = getAdmin(entityManager);
        registerToSecurityContext(admin);
        Approval adminResponse = new Approval();
        adminResponse.setApprovalStatus(APPROVED);
        adminResponse.setId(approval_1_1.getId());
        entityManager.clear();


        Approval result = service.respondToSellerApproval(adminResponse);
        assertEquals(approval_1_1.getId(), result.getId());
        assertEquals(Role.SELLER, result.getRequester().getRole());
        assertEquals(APPROVED, result.getApprovalStatus());
    }

    @Test
    public void respondToSellerApproval_approveCompany() {
        User user1 = entityManager.find(User.class, 1L);
        Approval approval_1_1 = new Approval();
        approval_1_1.setRequester(user1);
        approval_1_1.setApplicationType(COMPANY);
        approval_1_1.setApprovalStatus(PENDING);
        entityManager.persist(approval_1_1);

        User admin = getAdmin(entityManager);
        registerToSecurityContext(admin);
        Approval adminResponse = new Approval();
        adminResponse.setApprovalStatus(APPROVED);
        adminResponse.setId(approval_1_1.getId());
        entityManager.clear();


        Approval result = service.respondToSellerApproval(adminResponse);
        assertEquals(approval_1_1.getId(), result.getId());
        assertEquals(Role.COMPANY, result.getRequester().getRole());
        assertEquals(APPROVED, result.getApprovalStatus());
    }

    @Test
    public void respondToSellerApproval_reject() {
        User user1 = entityManager.find(User.class, 1L);
        Approval approval_1_1 = new Approval();
        approval_1_1.setRequester(user1);
        approval_1_1.setApplicationType(PRIVATE);
        approval_1_1.setApprovalStatus(PENDING);
        entityManager.persist(approval_1_1);

        User admin = getAdmin(entityManager);
        registerToSecurityContext(admin);
        Approval adminResponse = new Approval();
        adminResponse.setApprovalStatus(REJECTED);
        adminResponse.setId(approval_1_1.getId());
        entityManager.clear();

        Approval result = service.respondToSellerApproval(adminResponse);
        assertEquals(approval_1_1.getId(), result.getId());
        assertEquals(Role.USER, result.getRequester().getRole());
        assertEquals(REJECTED, result.getApprovalStatus());
    }


    private void persist3Approvals() {
        User user1 = entityManager.find(User.class, 1L);
        Approval approval_1_1 = new Approval();
        approval_1_1.setRequester(user1);
        approval_1_1.setApplicationType(PRIVATE);
        approval_1_1.setApprovalStatus(REJECTED);
        entityManager.persist(approval_1_1);

        User user2 = entityManager.find(User.class, 2L);
        Approval approval_2_1 = new Approval();
        approval_2_1.setRequester(user2);
        approval_2_1.setApplicationType(PRIVATE);
        approval_2_1.setApprovalStatus(REJECTED);
        entityManager.persist(approval_2_1);
        Approval approval_2_2 = new Approval();
        approval_2_2.setRequester(user2);
        approval_2_2.setApplicationType(PRIVATE);
        approval_2_2.setApprovalStatus(PENDING);
        entityManager.persist(approval_2_2);
    }


    @Autowired
    public ApprovalServiceImplIntegrationTests(TestEntityManager entityManager,
                                               ApprovalRepository approvalRepo,
                                               UserService userService,
                                               CompanyService companyService) {
        this.entityManager = entityManager;
        this.service = new ApprovalServiceImpl(approvalRepo, userService, companyService);
    }

    @TestConfiguration
    static class Producers {
        @Autowired
        UserRepository userRepo;
        @Autowired
        CompanyRepository companyRepo;

        @Bean
        public UserService userService() {
            return new UserServiceImpl(userRepo, null, null, null, null);
        }

        @Bean
        public CompanyService companyService() {
            Logger logger = LoggerFactory.getLogger(CompanyServiceImpl.class);
            return new CompanyServiceImpl(logger, companyRepo, null);
        }

    }
}
