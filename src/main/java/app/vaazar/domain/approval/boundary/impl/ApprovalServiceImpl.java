package app.vaazar.domain.approval.boundary.impl;

import app.vaazar.domain.approval.boundary.ApprovalService;
import app.vaazar.domain.approval.controller.ApprovalRepository;
import app.vaazar.domain.approval.entity.ApplicationType;
import app.vaazar.domain.approval.entity.Approval;
import app.vaazar.domain.approval.entity.ApprovalStatus;
import app.vaazar.domain.company.boundary.CompanyService;
import app.vaazar.domain.company.entity.Company;
import app.vaazar.domain.user.boundary.UserService;
import app.vaazar.domain.user.entity.Role;
import app.vaazar.domain.user.entity.User;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ApprovalServiceImpl implements ApprovalService {

    private final ApprovalRepository approvalRepo;
    private final UserService userService;
    private final CompanyService companyService;

    public Approval getApproval(Long id) {
        return approvalRepo.findById(id).orElseThrow();
    }

    public List<Approval> getApprovalsOfUser(Long userId) {
        return approvalRepo.findByRequesterId(userId);
    }

    public List<Approval> listApprovals(Optional<ApprovalStatus> status) {
        return approvalRepo.findByApprovalStatus(status.orElse(ApprovalStatus.PENDING));
    }

    public Approval requestSellerApproval(ApplicationType applicationType, User requester) {
        User persistedUser = userService.findById(requester.getId());
        List<Approval> approvals = approvalRepo.findByRequesterId(persistedUser.getId());
        if (approvals.stream().anyMatch(Approval::isPending))
            throw new IllegalStateException("there is an ongoing process");
        switch (applicationType) {
            case PRIVATE:
                persistedUser.updateBaseFields(requester);
                persistedUser.updateSellerFields(requester);
                if (!persistedUser.checkSellerInfo())
                    throw new IllegalArgumentException("missing required fields");
                else if (persistedUser.getRole() == Role.SELLER || persistedUser.getRole() == Role.COMPANY)
                    throw new IllegalStateException("already approved");
                userService.saveUser(persistedUser);
                break;
            case COMPANY:
                if (persistedUser.getCompany() == null) {
                    persistedUser.updateBaseFields(requester);
                    persistedUser.updateSellerFields(requester);
                    Company company = requester.getCompany();
                    company.setUser(persistedUser);
                    persistedUser.setCompany(company);
                    companyService.saveCompany(company);
                    userService.saveUser(persistedUser);
                } // if company not null, updates must be made with via crud methods
                if (!persistedUser.checkCompanyInfo())
                    throw new IllegalArgumentException("missing required fields");
                else if (persistedUser.getRole().equals(Role.COMPANY))
                    throw new IllegalStateException("already approved");
        }

        Approval approval = new Approval();
        approval.setRequester(persistedUser);
        approval.setApplicationType(applicationType);
        approval.setApprovalStatus(ApprovalStatus.PENDING);
        return approvalRepo.save(approval);
    }

    public Approval respondToSellerApproval(Approval adminResponse) {
        if (adminResponse.getApprovalStatus() == null || adminResponse.getApprovalStatus() == ApprovalStatus.PENDING)
            throw new IllegalArgumentException("a valid status must be specified");
        Approval approval = approvalRepo.findById(adminResponse.getId()).orElseThrow();
        if (!approval.isPending())
            throw new IllegalStateException("approval status must be PENDING");
        Long loggedAdmin = ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        User evaluator = userService.findById(loggedAdmin);
        approval.setDescription(adminResponse.getDescription());
        approval.setEvaluator(evaluator);
        approval.setApprovalStatus(adminResponse.getApprovalStatus());
        if (adminResponse.getApprovalStatus() == ApprovalStatus.APPROVED) {
            if (approval.getApplicationType() == ApplicationType.COMPANY) {
                approval.getRequester().setRole(Role.COMPANY);
            } else {
                approval.getRequester().setRole(Role.SELLER);
            }
            userService.saveUser(approval.getRequester());
        }
        return approvalRepo.save(approval);
    }


    public ApprovalServiceImpl(ApprovalRepository approvalRepo, UserService userService, CompanyService companyService) {
        this.approvalRepo = approvalRepo;
        this.userService = userService;
        this.companyService = companyService;
    }
}
