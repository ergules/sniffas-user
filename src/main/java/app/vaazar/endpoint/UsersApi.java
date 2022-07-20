package app.vaazar.endpoint;

import app.vaazar.config.exception.AuthorisationException;
import app.vaazar.domain.address.boundary.AddressService;
import app.vaazar.domain.address.entity.Address;
import app.vaazar.domain.approval.boundary.ApprovalService;
import app.vaazar.domain.approval.entity.Approval;
import app.vaazar.domain.company.boundary.CompanyService;
import app.vaazar.domain.company.entity.Company;
import app.vaazar.domain.company.entity.ShareHolder;
import app.vaazar.domain.upload.control.UploadUtil;
import app.vaazar.domain.user.boundary.UserService;
import app.vaazar.domain.user.entity.User;
import app.vaazar.endpoint.dto.SellerRequestDto;
import app.vaazar.endpoint.dto.address.AddressDTO;
import app.vaazar.endpoint.dto.approval.ApprovalDTO;
import app.vaazar.endpoint.dto.company.CompanyDTO;
import app.vaazar.endpoint.dto.company.ShareholderDTO;
import app.vaazar.endpoint.dto.upload.SignedUploadDTO;
import app.vaazar.endpoint.dto.upload.UploadDTO;
import app.vaazar.endpoint.dto.user.UserDTO;
import app.vaazar.service.FileStorage;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/users")
@PreAuthorize("isAuthenticated()")
@SecurityRequirement(name = "jwt")
public class UsersApi {

    private final UserService userService;
    private final CompanyService companyService;
    private final AddressService addressService;
    private final FileStorage fileStorage;
    private final ApprovalService approvalService;

    private final ModelMapper modelMapper;

    @GetMapping("/{userId}")
    public UserDTO getUser(@PathVariable Long userId,
                           UsernamePasswordAuthenticationToken contextUser) {
        User loggedUser = (User) contextUser.getPrincipal();
        if (!userId.equals(loggedUser.getId()))
            throw new AccessDeniedException("logged userId do not match with target");
        User found = userService.findById(userId);
        if (found == null) return null;
        return modelMapper.map(found, UserDTO.class);
    }

    @PutMapping("/{userId}")
    public UserDTO updateUser(@PathVariable Long userId,
                              @Valid @RequestBody UserDTO userDTO,
                              UsernamePasswordAuthenticationToken contextUser) throws AuthorisationException {
        User loggedUser = (User) contextUser.getPrincipal();
        if (!userId.equals(loggedUser.getId()) || userDTO.getId() != null && !userId.equals(userDTO.getId()))
            throw new AccessDeniedException("logged userId do not match with target");
        User updated = userService.updateUserInfo(modelMapper.map(userDTO, User.class));
        return modelMapper.map(updated, UserDTO.class);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId,
                                        UsernamePasswordAuthenticationToken contextUser) {
        User loggedUser = (User) contextUser.getPrincipal();
        if (!userId.equals(loggedUser.getId()))
            throw new AccessDeniedException("logged userId do not match with target");

        return ResponseEntity.ok(userService.deleteUser(userId));
    }

    @PostMapping("/{userId}/addresses")
    public AddressDTO addAddressToUser(@PathVariable Long userId,
                                       @Valid @RequestBody AddressDTO addressDTO,
                                       UsernamePasswordAuthenticationToken contextUser) {
        User loggedUser = (User) contextUser.getPrincipal();
        if (!userId.equals(loggedUser.getId()))
            throw new AccessDeniedException("logged userId do not match with target");
        Address newAddress = modelMapper.map(addressDTO, Address.class);
        Address persisted = addressService.addAddressToUser(newAddress, userId);
        return modelMapper.map(persisted, AddressDTO.class);
    }

    @PutMapping("/{userId}/addresses/{addressId}")
    public AddressDTO updateAddress(@PathVariable Long userId,
                                    @PathVariable Long addressId,
                                    @Valid @RequestBody AddressDTO addressDTO,
                                    UsernamePasswordAuthenticationToken contextUser) {
        User loggedUser = (User) contextUser.getPrincipal();
        if (!userId.equals(loggedUser.getId()))
            throw new AccessDeniedException("logged userId do not match with target");
        addressDTO.setId(addressId);

        Address toBeUpdated = modelMapper.map(addressDTO, Address.class);
        Address merged = addressService.updateAddress(toBeUpdated, userId);
        return modelMapper.map(merged, AddressDTO.class);
    }

    @DeleteMapping("/{userId}/addresses/{addressId}")
    public ResponseEntity<Void> deleteAddress(@PathVariable Long userId,
                                              @PathVariable Long addressId,
                                              UsernamePasswordAuthenticationToken contextUser) {
        User loggedUser = (User) contextUser.getPrincipal();
        if (!userId.equals(loggedUser.getId()))
            throw new AccessDeniedException("logged userId do not match with target");

        addressService.deleteAddress(addressId, userId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{userId}/companies/{companyId}")
    public CompanyDTO updateCompanyInfo(@PathVariable Long userId,
                                        @PathVariable Long companyId,
                                        @Valid @RequestBody CompanyDTO companyDTO,
                                        UsernamePasswordAuthenticationToken contextUser) {
        User loggedUser = (User) contextUser.getPrincipal();
        if (!userId.equals(loggedUser.getId()))
            throw new AccessDeniedException("logged userId do not match with target");
        companyDTO.setId(companyId);

        Company company = modelMapper.map(companyDTO, Company.class);
        Company merged = companyService.updateCompany(company, userId);
        return modelMapper.map(merged, CompanyDTO.class);
    }

    @PostMapping("/{userId}/share-holders")
    public ShareholderDTO createShareHolder(@PathVariable Long userId,
                                            @Valid @RequestBody ShareholderDTO shareholderDTO,
                                            UsernamePasswordAuthenticationToken contextUser) {
        User loggedUser = (User) contextUser.getPrincipal();
        if (!userId.equals(loggedUser.getId()))
            throw new AccessDeniedException("logged userId do not match with target");
        ShareHolder newShareholder = modelMapper.map(shareholderDTO, ShareHolder.class);
        ShareHolder persisted = companyService.createShareHolder(newShareholder, userId);
        return modelMapper.map(persisted, ShareholderDTO.class);
    }

    @PutMapping("/{userId}/share-holders/{shareHolderId}")
    public ShareholderDTO updateShareHolder(@PathVariable Long userId,
                                            @PathVariable Long shareHolderId,
                                            @Valid @RequestBody ShareholderDTO shareholderDTO,
                                            UsernamePasswordAuthenticationToken contextUser) {
        User loggedUser = (User) contextUser.getPrincipal();
        if (!userId.equals(loggedUser.getId()))
            throw new AccessDeniedException("logged userId do not match with target");
        shareholderDTO.setId(shareHolderId);

        ShareHolder toBeUpdated = modelMapper.map(shareholderDTO, ShareHolder.class);
        ShareHolder merged = companyService.updateShareHolder(toBeUpdated, userId);
        return modelMapper.map(merged, ShareholderDTO.class);
    }

    @DeleteMapping("/{userId}/share-holders/{shareHolderId}")
    public ResponseEntity<Void> deleteShareHolder(@PathVariable Long userId,
                                                  @PathVariable Long shareHolderId,
                                                  UsernamePasswordAuthenticationToken contextUser) {
        User loggedUser = (User) contextUser.getPrincipal();
        if (!userId.equals(loggedUser.getId()))
            throw new AccessDeniedException("logged userId do not match with target");
        companyService.deleteShareHolder(shareHolderId, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{userId}/seller-requests")
    public ResponseEntity<List<ApprovalDTO>> getSellerApprovals(@PathVariable Long userId,
                                                                UsernamePasswordAuthenticationToken contextUser) {
        User loggedUser = (User) contextUser.getPrincipal();
        if (!userId.equals(loggedUser.getId()))
            throw new AccessDeniedException("logged userId do not match with target");
        List<ApprovalDTO> dtoList = modelMapper.map(approvalService.getApprovalsOfUser(userId),
                new TypeToken<List<ApprovalDTO>>() {
                }.getType());
        return ResponseEntity.ok(dtoList);
    }

    @PostMapping("/{userId}/seller-requests")
    public ResponseEntity<ApprovalDTO> requestSellerApproval(@PathVariable Long userId,
                                                             @Valid @RequestBody SellerRequestDto sellerRequestDto,
                                                             UsernamePasswordAuthenticationToken contextUser) {
        User loggedUser = (User) contextUser.getPrincipal();
        if (!userId.equals(loggedUser.getId()) ||
                !sellerRequestDto.getUser().getId().equals(userId))
            throw new AccessDeniedException("logged userId do not match with target");
        User requester = modelMapper.map(sellerRequestDto.getUser(), User.class);
        Approval approval = approvalService
                .requestSellerApproval(sellerRequestDto.getApplicationType(), requester);
        return ResponseEntity.ok(modelMapper.map(approval, ApprovalDTO.class));
    }

    @GetMapping("/{userId}/seller-requests/{approvalId}")
    public ApprovalDTO getSellerApproval(@PathVariable Long userId, @PathVariable Long approvalId) {
        Approval approval = approvalService.getApproval(approvalId);
        if (approval.getRequester().getId().equals(userId))
            return modelMapper.map(approval, ApprovalDTO.class);
        else
            throw new AccessDeniedException("request belongs an other user");
    }

    @PostMapping("/sign-upload-link")
    public SignedUploadDTO getPresignedLink(@Valid @RequestBody UploadDTO uploadDTO,
                                            UsernamePasswordAuthenticationToken contextUser) {
        User loggedUser = (User) contextUser.getPrincipal();

        String uploadName = UploadUtil.nameUpload(uploadDTO.getUploadType(),
                loggedUser,
                uploadDTO.getEntityId(),
                uploadDTO.getExtension());
        String signedURL = fileStorage.preSignWithObjectKey(uploadName, uploadDTO.getUploadType());
        return new SignedUploadDTO(uploadName, signedURL);
    }

    public UsersApi(UserService userService, CompanyService companyService,
                    AddressService addressService,
                    FileStorage fileStorage, ApprovalService approvalService,
                    ModelMapper modelMapper) {
        this.userService = userService;
        this.companyService = companyService;
        this.addressService = addressService;
        this.fileStorage = fileStorage;
        this.approvalService = approvalService;
        this.modelMapper = modelMapper;
    }
}
