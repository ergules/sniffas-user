package app.vaazar.Domain.User.Boundary;

import app.vaazar.Domain.Approval.Controller.ApprovalRepository;
import app.vaazar.Domain.Approval.Entity.Approval;
import app.vaazar.Domain.Approval.Entity.ApprovalStatus;
import app.vaazar.Domain.Company.Boundary.CompanyService;
import app.vaazar.Domain.Company.Entity.Company;
import app.vaazar.Domain.User.Control.UserRepository;
import app.vaazar.Domain.User.Entity.Role;
import app.vaazar.Domain.User.Entity.User;
import app.vaazar.Endpoint.Dto.BasicUser;
import app.vaazar.Endpoint.Dto.SellerRequestDto;
import app.vaazar.Service.Firebase.FirebaseAuthService;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
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
    private final FirebaseAuthService firebaseAuthService;

    public User findById(Long id) {
        return userRepo.findById(id).orElse(null);
    }

    public boolean isUsernameAvailable(String username) {
        return !userRepo.existsByUsername(username);
    }

    public User saveUser(User user) {
        return userRepo.save(user);
    }

    public List<BasicUser> findBasicUsers(List<Long> idList) {
        return userRepo.findBasicUsersInList(idList);
    }

    public User updateUserInfo(User user) throws FirebaseAuthException {
        User toUpdate = userRepo.findById(user.getId()).orElseThrow();
        toUpdate.updateBaseFields(user);
        UserRecord firebaseRecord = null;
        if (!toUpdate.getEmail().equalsIgnoreCase(user.getEmail())) {
            firebaseRecord = firebaseAuthService.getFirebaseRecord(toUpdate.getFirebaseUid());
            if (firebaseRecord.getEmail().equals(user.getEmail())) {
                toUpdate.setEmail(user.getEmail());
            }
        }
        if (user.getMobilePhone() != null && !user.getMobilePhone().equals(toUpdate.getMobilePhone())) {
            if (firebaseRecord == null)
                firebaseRecord = firebaseAuthService.getFirebaseRecord(toUpdate.getFirebaseUid());
            if (user.getMobilePhone().equals(firebaseRecord.getPhoneNumber())) {
                toUpdate.setMobilePhone(user.getMobilePhone());
            }
        }
        return userRepo.save(toUpdate);
    }

    public User registerUser(User user, FirebaseToken token) throws FirebaseAuthException {
        if (token.getEmail() != null && !user.getEmail().equalsIgnoreCase(token.getEmail()))
            throw new IllegalStateException("emails do not match");

        user.setEmail(user.getEmail()); //ensure lowerCase
        user.setFirebaseUid(token.getUid());
        user.setRole(Role.USER);
        if (user.getMobilePhone() != null) {
            UserRecord firebaseRecord = firebaseAuthService.getFirebaseRecord(token.getUid());
            user.setMobilePhone(firebaseRecord.getPhoneNumber());
        } // trust the record on firebase
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

    public UserService(UserRepository userRepo, ApprovalRepository approvalRepo, CompanyService companyService, FirebaseAuthService firebaseAuthService) {
        this.userRepo = userRepo;
        this.approvalRepo = approvalRepo;
        this.companyService = companyService;
        this.firebaseAuthService = firebaseAuthService;
    }
}
