package app.vaazar.Endpoint;

import app.vaazar.Domain.Address.Boundary.AddressService;
import app.vaazar.Domain.Address.Entity.Address;
import app.vaazar.Domain.Approval.Boundary.ApprovalService;
import app.vaazar.Domain.Approval.Entity.Approval;
import app.vaazar.Domain.Company.Boundary.CompanyService;
import app.vaazar.Domain.Company.Entity.Company;
import app.vaazar.Domain.Company.Entity.ShareHolder;
import app.vaazar.Domain.Upload.Control.UploadUtil;
import app.vaazar.Domain.Upload.Entity.UploadType;
import app.vaazar.Domain.User.Boundary.UserService;
import app.vaazar.Domain.User.Entity.User;
import app.vaazar.Endpoint.Dto.SellerRequestDto;
import app.vaazar.Service.FileStorage;
import com.google.firebase.auth.FirebaseAuthException;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/users/{userId}")
@PreAuthorize("isAuthenticated()")
@SecurityRequirement(name = "jwt")
public class UsersApi {

    private final UserService userService;
    private final CompanyService companyService;
    private final AddressService addressService;
    private final FileStorage fileStorage;
    private final ApprovalService approvalService;

    @GetMapping
    public User getUser(@PathVariable Long userId,
                        UsernamePasswordAuthenticationToken contextUser) {
        User loggedUser = (User) contextUser.getPrincipal();
        if (!userId.equals(loggedUser.getId()))
            throw new AccessDeniedException("logged userId do not match with target");
        return userService.findById(userId);
    }

    @PutMapping
    public User updateUser(@PathVariable Long userId,
                           @Valid @RequestBody User user,
                           UsernamePasswordAuthenticationToken contextUser) throws FirebaseAuthException {
        User loggedUser = (User) contextUser.getPrincipal();
        if (!userId.equals(loggedUser.getId()) || user.getId() != null && !userId.equals(user.getId()))
            throw new AccessDeniedException("logged userId do not match with target");
        return userService.updateUserInfo(user);
    }

    @PostMapping("/addresses")
    public Address addAddressToUser(@PathVariable Long userId,
                                    @Valid @RequestBody Address address,
                                    UsernamePasswordAuthenticationToken contextUser) {
        User loggedUser = (User) contextUser.getPrincipal();
        if (!userId.equals(loggedUser.getId()))
            throw new AccessDeniedException("logged userId do not match with target");
        return addressService.addAddressToUser(address, userId);
    }

    @PutMapping("/addresses/{addressId}")
    public Address updateAddress(@PathVariable Long userId,
                                 @PathVariable Long addressId,
                                 @Valid @RequestBody Address address,
                                 UsernamePasswordAuthenticationToken contextUser) {
        User loggedUser = (User) contextUser.getPrincipal();
        if (!userId.equals(loggedUser.getId()))
            throw new AccessDeniedException("logged userId do not match with target");
        address.setId(addressId);
        return addressService.updateAddress(address, userId);
    }

    @DeleteMapping("/addresses/{addressId}")
    public ResponseEntity<Void> deleteAddress(@PathVariable Long userId,
                                              @PathVariable Long addressId,
                                              UsernamePasswordAuthenticationToken contextUser) {
        User loggedUser = (User) contextUser.getPrincipal();
        if (!userId.equals(loggedUser.getId()))
            throw new AccessDeniedException("logged userId do not match with target");

        addressService.deleteAddress(addressId, userId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/companies/{companyId}")
    public Company updateCompanyInfo(@PathVariable Long userId,
                                     @PathVariable Long companyId,
                                     @Valid @RequestBody Company company,
                                     UsernamePasswordAuthenticationToken contextUser) {
        User loggedUser = (User) contextUser.getPrincipal();
        if (!userId.equals(loggedUser.getId()))
            throw new AccessDeniedException("logged userId do not match with target");
        company.setId(companyId);
        return companyService.updateCompany(company, userId);
    }

    @PostMapping("/share-holders")
    public ShareHolder createShareHolder(@PathVariable Long userId,
                                         @Valid @RequestBody ShareHolder shareHolder,
                                         UsernamePasswordAuthenticationToken contextUser) {
        User loggedUser = (User) contextUser.getPrincipal();
        if (!userId.equals(loggedUser.getId()))
            throw new AccessDeniedException("logged userId do not match with target");
        return companyService.createShareHolder(shareHolder, userId);
    }

    @PutMapping("/share-holders/{shareHolderId}")
    public ShareHolder updateShareHolder(@PathVariable Long userId,
                                         @PathVariable Long shareHolderId,
                                         @Valid @RequestBody ShareHolder shareHolder,
                                         UsernamePasswordAuthenticationToken contextUser) {
        User loggedUser = (User) contextUser.getPrincipal();
        if (!userId.equals(loggedUser.getId()))
            throw new AccessDeniedException("logged userId do not match with target");
        shareHolder.setId(shareHolderId);
        return companyService.updateShareHolder(shareHolder, userId);
    }

    @DeleteMapping("/share-holders/{shareHolderId}")
    public ResponseEntity<Void> deleteShareHolder(@PathVariable Long userId,
                                                  @PathVariable Long shareHolderId,
                                                  UsernamePasswordAuthenticationToken contextUser) {
        User loggedUser = (User) contextUser.getPrincipal();
        if (!userId.equals(loggedUser.getId()))
            throw new AccessDeniedException("logged userId do not match with target");
        companyService.deleteShareHolder(shareHolderId, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/seller-requests")
    public ResponseEntity<List<Approval>> getSellerApprovals(@PathVariable Long userId,
                                                             UsernamePasswordAuthenticationToken contextUser) {
        User loggedUser = (User) contextUser.getPrincipal();
        if (!userId.equals(loggedUser.getId()))
            throw new AccessDeniedException("logged userId do not match with target");
        return ResponseEntity.ok(approvalService.getApprovalsOfUser(userId));
    }

    @PostMapping("/seller-requests")
    public ResponseEntity<Approval> requestSellerApproval(@PathVariable Long userId,
                                                          @Valid @RequestBody SellerRequestDto sellerRequestDto,
                                                          UsernamePasswordAuthenticationToken contextUser) {
        User loggedUser = (User) contextUser.getPrincipal();
        if (!userId.equals(loggedUser.getId()) ||
                !sellerRequestDto.getUser().getId().equals(userId))
            throw new AccessDeniedException("logged userId do not match with target");
        return ResponseEntity.ok(approvalService.requestSellerApproval(sellerRequestDto));
    }

    @GetMapping("/seller-requests/{approvalId}")
    public Approval getSellerApproval(@PathVariable Long userId, @PathVariable Long approvalId) {
        Approval approval = approvalService.getApproval(approvalId);
        if (approval.getRequester().getId().equals(userId))
            return approval;
        else
            throw new AccessDeniedException("request belongs an other user");
    }

    @GetMapping("/upload-link")
    public String getPresignedLink(@PathVariable Long userId,
                                    @RequestParam UploadType type,
                                    UsernamePasswordAuthenticationToken contextUser) {
        User loggedUser = (User) contextUser.getPrincipal();
        if (!userId.equals(loggedUser.getId()))
            throw new AccessDeniedException("logged userId do not match with target");
        String uploadName = UploadUtil.nameUpload(type, userId);
        return fileStorage.preSignWithObjectKey(uploadName, type);
    }

    public UsersApi(UserService userService, CompanyService companyService, AddressService addressService, FileStorage fileStorage, ApprovalService approvalService) {
        this.userService = userService;
        this.companyService = companyService;
        this.addressService = addressService;
        this.fileStorage = fileStorage;
        this.approvalService = approvalService;
    }
}
