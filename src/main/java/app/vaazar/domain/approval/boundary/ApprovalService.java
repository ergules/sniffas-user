package app.vaazar.domain.approval.boundary;

import app.vaazar.domain.approval.controller.ApprovalRepository;
import app.vaazar.domain.approval.entity.ApplicationType;
import app.vaazar.domain.approval.entity.Approval;
import app.vaazar.domain.approval.entity.ApprovalStatus;
import app.vaazar.domain.company.boundary.CompanyService;
import app.vaazar.domain.company.entity.Company;
import app.vaazar.domain.user.boundary.UserService;
import app.vaazar.domain.user.entity.Role;
import app.vaazar.domain.user.entity.User;
import app.vaazar.endpoint.dto.SellerRequestDto;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ApprovalService {

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

    public Approval requestSellerApproval(SellerRequestDto requestDto) {
        User user = userService.findById(requestDto.getUser().getId());
        List<Approval> approvals = approvalRepo.findByRequesterId(user.getId());
        if (approvals.stream().anyMatch(Approval::isPending))
            throw new IllegalStateException("there is an ongoing process");
        switch (requestDto.getApplicationType()) {
            case PRIVATE:
                user.updateBaseFields(requestDto.getUser());
                user.updateSellerFields(requestDto.getUser());
                if (!user.checkSellerInfo())
                    throw new IllegalStateException("missing required fields");
                else if (user.getRole().equals(Role.SELLER))
                    throw new IllegalStateException("already approved");
                userService.saveUser(user);
                break;
            case COMPANY:
                if (user.getCompany() == null) {
                    user.updateBaseFields(requestDto.getUser());
                    user.updateSellerFields(requestDto.getUser());
                    Company company = requestDto.getUser().getCompany();
                    company.setUser(user);
                    user.setCompany(company);
                    companyService.saveCompany(company);
                    userService.saveUser(user);
                } // if company not null, updates must be made with via crud methods
                if (!user.checkCompanyInfo())
                    throw new IllegalStateException("missing required fields");
                else if (user.getRole().equals(Role.COMPANY))
                    throw new IllegalStateException("already approved");
        }

        Approval approval = new Approval();
        approval.setRequester(user);
        approval.setApplicationType(requestDto.getApplicationType());
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


    public ApprovalService(ApprovalRepository approvalRepo, UserService userService, CompanyService companyService) {
        this.approvalRepo = approvalRepo;
        this.userService = userService;
        this.companyService = companyService;
    }
}
