package app.vaazar.domain.approval.boundary;

import app.vaazar.domain.approval.boundary.impl.ApprovalServiceImpl;
import app.vaazar.domain.approval.controller.ApprovalRepository;
import app.vaazar.domain.approval.entity.ApplicationType;
import app.vaazar.domain.approval.entity.Approval;
import app.vaazar.domain.approval.entity.ApprovalStatus;
import app.vaazar.domain.company.boundary.CompanyService;
import app.vaazar.domain.company.entity.Company;
import app.vaazar.domain.user.boundary.UserService;
import app.vaazar.domain.user.entity.Role;
import app.vaazar.domain.user.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
public class ApprovalServiceImplUnitTests {

    @Mock
    ApprovalRepository approvalRepo;
    @Mock
    UserService userService;
    @Mock
    CompanyService companyService;
    @InjectMocks
    ApprovalServiceImpl service;

    @Test
    public void getApproval() {
        Approval approval = new Approval();
        approval.setId(10L);

        when(approvalRepo.findById(any())).thenReturn(Optional.of(approval));

        assertEquals(approval, service.getApproval(0L));
    }

    @Test
    public void getApprovalsOfUser() {
        Approval approval = new Approval();
        approval.setId(10L);

        List<Approval> returner = List.of(approval);

        when(approvalRepo.findByRequesterId(any())).thenReturn(returner);

        assertThat(service.getApprovalsOfUser(10L), is(returner));
    }

    @Test
    public void listApprovals_noStatus() {
        service.listApprovals(Optional.empty());
        ArgumentCaptor<ApprovalStatus> approvalStatusCaptor =
                ArgumentCaptor.forClass(ApprovalStatus.class);

        verify(approvalRepo).findByApprovalStatus(approvalStatusCaptor.capture());
        assertEquals(approvalStatusCaptor.getValue(), ApprovalStatus.PENDING);
    }

    @Test
    public void listApprovals_withStatus() {
        ApprovalStatus requestedStatus = ApprovalStatus.APPROVED;
        service.listApprovals(Optional.of(requestedStatus));
        ArgumentCaptor<ApprovalStatus> approvalStatusCaptor =
                ArgumentCaptor.forClass(ApprovalStatus.class);

        verify(approvalRepo).findByApprovalStatus(approvalStatusCaptor.capture());
        assertEquals(approvalStatusCaptor.getValue(), requestedStatus);
    }

    @Test
    public void requestSellerApproval_whenDuplicateRequestMade_thenThrowIllegalStateException() {
        Approval pastApproval = new Approval();
        pastApproval.setApprovalStatus(ApprovalStatus.PENDING);

        User persistedUser = Mockito.mock(User.class);
        when(persistedUser.getId()).thenReturn(0L);

        when(userService.findById(any())).thenReturn(persistedUser);
        when(approvalRepo.findByRequesterId(0L)).thenReturn(List.of(pastApproval));

        assertThrows(IllegalStateException.class, () ->
                service.requestSellerApproval(ApplicationType.PRIVATE, persistedUser));
    }

    @Test
    public void requestSellerApproval_privateSeller_happy() {
        User requester = new User();
        requester.setId(0L);

        User persistedUser = Mockito.mock(User.class);
        when(persistedUser.getId()).thenReturn(0L);
        when(persistedUser.getRole()).thenReturn(Role.USER);
        when(persistedUser.checkSellerInfo()).thenReturn(true);

        when(userService.findById(any())).thenReturn(persistedUser);
        when(approvalRepo.findByRequesterId(0L)).thenReturn(Collections.emptyList());
        when(approvalRepo.save(any())).thenAnswer(i -> i.getArguments()[0]);

        Approval saved = service.requestSellerApproval(ApplicationType.PRIVATE, requester);
        assertEquals(saved.getApprovalStatus(), ApprovalStatus.PENDING);
    }

    @Test
    public void requestSellerApproval_whenPrivateSellerIsAlreadyApproved_thenThrowIllegalStateException() {

        User persistedUser = Mockito.mock(User.class);
        when(persistedUser.getId()).thenReturn(0L);
        when(persistedUser.getRole()).thenReturn(Role.SELLER);
        when(persistedUser.checkSellerInfo()).thenReturn(true);

        when(userService.findById(any())).thenReturn(persistedUser);
        when(approvalRepo.findByRequesterId(0L)).thenReturn(Collections.emptyList());

        Exception exception = assertThrows(IllegalStateException.class, () ->
                service.requestSellerApproval(ApplicationType.PRIVATE, persistedUser));
        assertEquals(exception.getMessage(), "already approved");
    }

    @Test
    public void requestSellerApproval_whenPrivateSellerHasMissingInfo_thenThrowIllegalArgumentException() {
        User requester = new User();
        requester.setId(0L);

        User persistedUser = Mockito.mock(User.class);
        when(persistedUser.getId()).thenReturn(0L);
        when(persistedUser.getRole()).thenReturn(Role.SELLER);
        when(persistedUser.checkSellerInfo()).thenReturn(false);

        when(userService.findById(any())).thenReturn(persistedUser);
        when(approvalRepo.findByRequesterId(0L)).thenReturn(Collections.emptyList());
        when(approvalRepo.save(any())).thenAnswer(i -> i.getArguments()[0]);

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                service.requestSellerApproval(ApplicationType.PRIVATE, persistedUser));
        assertTrue(exception.getMessage().contains("required"));
    }

    @Test
    public void requestSellerApproval_company_happy() {
        User requester = new User();
        requester.setId(0L);
        requester.setCompany(new Company());

        User persistedUser = Mockito.mock(User.class);
        when(persistedUser.getId()).thenReturn(0L);
        when(persistedUser.getRole()).thenReturn(Role.USER);
        when(persistedUser.checkSellerInfo()).thenReturn(true);
        when(persistedUser.checkCompanyInfo()).thenReturn(true);

        when(userService.findById(any())).thenReturn(persistedUser);
        when(approvalRepo.save(any())).thenAnswer(i -> i.getArguments()[0]);
        when(approvalRepo.findByRequesterId(0L)).thenReturn(Collections.emptyList());

        Approval saved = service.requestSellerApproval(ApplicationType.COMPANY, requester);
        assertEquals(saved.getApprovalStatus(), ApprovalStatus.PENDING);
    }

    @Test
    public void requestSellerApproval_whenAlreadyApproved_thenThrowIllegalStateException() {
        User requester = new User();
        requester.setId(0L);
        requester.setCompany(new Company());

        User persistedUser = Mockito.mock(User.class);
        when(persistedUser.getId()).thenReturn(0L);
        when(persistedUser.getRole()).thenReturn(Role.COMPANY);
        when(persistedUser.checkSellerInfo()).thenReturn(true);
        when(persistedUser.checkCompanyInfo()).thenReturn(true);

        when(userService.findById(any())).thenReturn(persistedUser);
        when(approvalRepo.findByRequesterId(0L)).thenReturn(Collections.emptyList());

        Exception exception = assertThrows(IllegalStateException.class, () ->
                service.requestSellerApproval(ApplicationType.COMPANY, requester));
        assertEquals(exception.getMessage(), "already approved");
    }

    @Test
    public void requestSellerApproval_whenCompanyHasMissingInfo_thenThrowIllegalArgumentException() {
        User requester = new User();
        requester.setId(0L);
        requester.setCompany(new Company());

        User persistedUser = Mockito.mock(User.class);
        when(persistedUser.getId()).thenReturn(0L);
        when(persistedUser.getRole()).thenReturn(Role.USER);
        when(persistedUser.checkSellerInfo()).thenReturn(true);
        when(persistedUser.checkCompanyInfo()).thenReturn(false);

        when(userService.findById(any())).thenReturn(persistedUser);
        when(approvalRepo.findByRequesterId(0L)).thenReturn(Collections.emptyList());
        when(approvalRepo.save(any())).thenAnswer(i -> i.getArguments()[0]);

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                service.requestSellerApproval(ApplicationType.COMPANY, requester));
        assertTrue(exception.getMessage().contains("missing"));
    }

    @Test
    public void respondToSellerApproval_approve_happy() {
        Long approvalId = 15L;
        Long adminId = 1L;
        Long userId = 10L;
        User admin = new User(adminId, null, null, Role.ADMIN);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(admin, null, admin.getAuthorities()));

        User user = new User(userId, null, null, Role.USER);
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        Approval adminResp = new Approval();
        adminResp.setApprovalStatus(ApprovalStatus.APPROVED);
        adminResp.setId(approvalId);

        Approval approval = new Approval();
        approval.setId(approvalId);
        approval.setApprovalStatus(ApprovalStatus.PENDING);
        approval.setApplicationType(ApplicationType.COMPANY);
        approval.setRequester(user);

        when(userService.findById(adminId)).thenReturn(admin);
        when(userService.findById(userId)).thenReturn(user);
        when(approvalRepo.findById(approvalId)).thenReturn(Optional.of(approval));
        when(approvalRepo.save(any())).thenAnswer(i -> i.getArguments()[0]);

        // COMPANY REGISTRATION
        Approval returnerCompany = service.respondToSellerApproval(adminResp);
        verify(userService).saveUser(userCaptor.capture());

        assertEquals(userCaptor.getValue().getRole(), Role.COMPANY);
        assertEquals(returnerCompany.getEvaluator(), admin);
        assertEquals(returnerCompany.getApprovalStatus(), ApprovalStatus.APPROVED);

        // PRIVATE SELLER REGISTRATION
        approval.setApprovalStatus(ApprovalStatus.PENDING);
        approval.setApplicationType(ApplicationType.PRIVATE);


        Approval returnerPrivateSeller = service.respondToSellerApproval(adminResp);
        verify(userService, times(2)).saveUser(userCaptor.capture());

        assertEquals(userCaptor.getValue().getRole(), Role.SELLER);
        assertEquals(returnerCompany.getApprovalStatus(), ApprovalStatus.APPROVED);

    }

    @Test
    public void respondToSellerApproval_reject_happy() {
        Long approvalId = 15L;
        Long adminId = 1L;
        Long userId = 10L;
        User admin = new User(adminId, null, null, Role.ADMIN);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(admin, null, admin.getAuthorities()));

        User user = new User(userId, null, null, Role.USER);

        Approval adminResp = new Approval();
        adminResp.setApprovalStatus(ApprovalStatus.REJECTED);
        adminResp.setId(approvalId);

        Approval approval = new Approval();
        approval.setId(approvalId);
        approval.setApprovalStatus(ApprovalStatus.PENDING);
        approval.setApplicationType(ApplicationType.COMPANY);
        approval.setRequester(user);

        when(userService.findById(adminId)).thenReturn(admin);
        when(userService.findById(userId)).thenReturn(user);
        when(approvalRepo.findById(approvalId)).thenReturn(Optional.of(approval));
        when(approvalRepo.save(any())).thenAnswer(i -> i.getArguments()[0]);

        Approval returner = service.respondToSellerApproval(adminResp);

        assertEquals(returner.getEvaluator(), admin);
        assertEquals(returner.getApprovalStatus(), ApprovalStatus.REJECTED);
    }

    @Test
    public void respondToSellerApproval_whenWrongStatus_thenThrowIllegalArgumentException() {
        Long approvalId = 15L;

        Approval adminResp = new Approval();
        adminResp.setApprovalStatus(ApprovalStatus.PENDING);
        adminResp.setId(approvalId);

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                service.respondToSellerApproval(adminResp));
        assertTrue(exception.getMessage().contains("valid status must be specified"));

        adminResp.setApprovalStatus(null);
        Exception exception2 = assertThrows(IllegalArgumentException.class, () ->
                service.respondToSellerApproval(adminResp));
        assertTrue(exception2.getMessage().contains("valid status must be specified"));
    }

    @Test
    public void respondToSellerApproval_whenDuplicateResponse_thenThrowIllegalStateException() {
        Long approvalId = 15L;

        Approval adminResp = new Approval();
        adminResp.setApprovalStatus(ApprovalStatus.APPROVED);
        adminResp.setId(approvalId);

        Approval approval = new Approval();
        approval.setId(approvalId);
        approval.setApprovalStatus(ApprovalStatus.REJECTED);


        when(approvalRepo.findById(approvalId)).thenReturn(Optional.of(approval));

        Exception exception = assertThrows(IllegalStateException.class, () ->
                service.respondToSellerApproval(adminResp));
        assertEquals("approval status must be PENDING", exception.getMessage());
    }

}
