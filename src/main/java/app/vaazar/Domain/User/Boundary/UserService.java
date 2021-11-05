package app.vaazar.Domain.User.Boundary;

import app.vaazar.Domain.Approval.Controller.ApprovalRepository;
import app.vaazar.Domain.Approval.Entity.Approval;
import app.vaazar.Domain.Approval.Entity.ApprovalStatus;
import app.vaazar.Domain.Company.Boundary.CompanyService;
import app.vaazar.Domain.Company.Entity.Company;
import app.vaazar.Domain.User.Control.UserRepository;
import app.vaazar.Domain.User.Entity.Role;
import app.vaazar.Domain.User.Entity.User;
import app.vaazar.Endpoint.Dto.SellerRequestDto;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepo;
    private final ApprovalRepository approvalRepo;
    private final CompanyService companyService;

    public User findById(Long id) {
        return userRepo.findById(id).orElse(null);
    }

    public boolean isUsernameAvailable(String username) {
        return !userRepo.existsByUsername(username);
    }

    public User saveUser(User user) {
        return userRepo.save(user);
    }

    public User updateUserInfo(User user) {
        User toUpdate = userRepo.findById(user.getId()).orElseThrow();
        toUpdate.updateBaseFields(user);
        return userRepo.save(toUpdate);
    }

    public User registerUser(User user, FirebaseToken token) {
        if (token.getEmail() != null && !user.getEmail().equalsIgnoreCase(token.getEmail()))
            throw new IllegalStateException("emails do not match");
        user.setEmail(user.getEmail()); //ensure lowerCase
        user.setFirebaseUid(token.getUid());
        user.setRole(Role.USER);
        return saveUser(user);
    }

    public Approval getApproval(Long id) {
        return approvalRepo.findById(id).orElseThrow();
    }

    public Approval requestSellerApproval(SellerRequestDto requestDto) {
        User user = userRepo.findById(requestDto.getUser().getId()).orElseThrow();
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
                userRepo.save(user);
                break;
            case COMPANY:
                if (user.getCompany() == null) {
                    user.updateBaseFields(requestDto.getUser());
                    user.updateSellerFields(requestDto.getUser());
                    Company company = requestDto.getUser().getCompany();
                    company.setUser(user);
                    user.setCompany(company);
                    companyService.saveCompany(company);
                    userRepo.save(user);
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

    public User findUserById(Long id) {
        return userRepo.findById(id).orElse(null);
    }

    public Optional<User> findByUid(String uid) {
        return userRepo.findByFirebaseUid(uid);
    }

    public UserService(UserRepository userRepo, ApprovalRepository approvalRepo, CompanyService companyService) {
        this.userRepo = userRepo;
        this.approvalRepo = approvalRepo;
        this.companyService = companyService;
    }
}
