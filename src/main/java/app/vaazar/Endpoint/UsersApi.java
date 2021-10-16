package app.vaazar.Endpoint;

import app.vaazar.Domain.Address.Boundary.AddressService;
import app.vaazar.Domain.Address.Entity.Address;
import app.vaazar.Domain.Approval.Entity.Approval;
import app.vaazar.Domain.Company.Boundary.CompanyService;
import app.vaazar.Domain.Company.Entity.Company;
import app.vaazar.Domain.Company.Entity.ShareHolder;
import app.vaazar.Domain.User.Boundary.UserService;
import app.vaazar.Domain.User.Entity.User;
import app.vaazar.Endpoint.Dto.SellerRequestDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;

@RestController
@RequestMapping("/users/{userId}")
@PreAuthorize("isAuthenticated()")
public class UsersApi {

    private final UserService userService;
    private final CompanyService companyService;
    private final AddressService addressService;


    @GetMapping
    public User updateUser(@PathVariable Long userId,
                           UsernamePasswordAuthenticationToken contextUser) {
        User loggedUser = (User) contextUser.getPrincipal();
        if (!userId.equals(loggedUser.getId()))
            throw new AccessDeniedException("logged userId do not match with target");
        return userService.findById(userId);
    }

    @PutMapping
    public User updateUser(@PathVariable Long userId,
                           @Valid @RequestBody User user,
                           UsernamePasswordAuthenticationToken contextUser) {
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

    @DeleteMapping("/addresses/{addressId}")
    public ResponseEntity<Void> deleteAddress(@PathVariable Long userId,
                                              @PathVariable Long addressId,
                                              @Valid @RequestBody Address address,
                                              Principal principal) {
        User loggedUser = (User) principal;
        if (!userId.equals(loggedUser.getId()))
            throw new AccessDeniedException("logged userId do not match with target");
        address.setId(addressId);

        addressService.deleteAddress(address, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/seller-requests")
    public ResponseEntity<Approval> requestSellerApproval(@PathVariable Long userId,
                                                          @Valid @RequestBody SellerRequestDto sellerRequestDto,
                                                          UsernamePasswordAuthenticationToken contextUser) {
        User loggedUser = (User) contextUser.getPrincipal();
        if (!userId.equals(loggedUser.getId()) ||
                !sellerRequestDto.getUser().getId().equals(userId))
            throw new AccessDeniedException("logged userId do not match with target");
        return ResponseEntity.ok(userService.requestSellerApproval(sellerRequestDto));
    }

    @GetMapping("/seller-requests/{approvalId}")
    public Approval getSellerApproval(@PathVariable Long userId, @PathVariable Long approvalId) {
        Approval approval = userService.getApproval(approvalId);
        if (approval.getRequester().getId().equals(userId))
            return approval;
        else
            throw new AccessDeniedException("request belongs an other user");
    }

    public UsersApi(UserService userService, CompanyService companyService, AddressService addressService) {
        this.userService = userService;
        this.companyService = companyService;
        this.addressService = addressService;
    }
}
