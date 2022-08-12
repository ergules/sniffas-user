package app.vaazar.endpoint;

import app.vaazar.domain.approval.boundary.ApprovalService;
import app.vaazar.domain.approval.entity.Approval;
import app.vaazar.domain.approval.entity.ApprovalStatus;
import app.vaazar.domain.deleteAccount.boundary.DeleteAccountService;
import app.vaazar.domain.deleteAccount.entity.DeleteAccountRequest;
import app.vaazar.domain.deleteAccount.entity.DeleteRequestStatus;
import app.vaazar.domain.user.boundary.UserService;
import app.vaazar.domain.user.entity.User;
import app.vaazar.endpoint.dto.BasicUser;
import app.vaazar.endpoint.dto.approval.ApprovalDTO;
import app.vaazar.endpoint.dto.deleteAccount.DeleteAccountRequestDTO;
import app.vaazar.endpoint.dto.user.UserDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin")
@SecurityRequirement(name = "jwt")
@PreAuthorize("hasRole('ADMIN')")
public class AdminApi {

    private final UserService userService;
    private final ApprovalService approvalService;
    private final DeleteAccountService deleteAccountService;
    private final ModelMapper modelMapper;

    @GetMapping("/users")
    public Page<BasicUser> findUsers(@RequestParam Optional<String> query,
                                     @RequestParam(defaultValue = "false") boolean seller,
                                     Pageable pageable) {
        Page<User> resultPage = userService.findUsers(query, seller, pageable);
        List<BasicUser> dtoList = mapList(resultPage.getContent(), BasicUser.class);
        return new PageImpl<>(dtoList, resultPage.getPageable(), resultPage.getTotalElements());
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<UserDTO> findUser(@PathVariable Long userId) {
        User result = userService.findById(userId);

        return result == null
                ? ResponseEntity.status(404).build()
                : ResponseEntity.ok(modelMapper.map(result, UserDTO.class));
    }

    @GetMapping("/approvals")
    public List<ApprovalDTO> findApprovals(Optional<ApprovalStatus> status) {
        List<Approval> approvals = approvalService.listApprovals(status);
        return mapList(approvals, ApprovalDTO.class);
    }

    @PutMapping("/approvals/{approvalId}")
    public ApprovalDTO respondToApproval(@RequestBody ApprovalDTO responseDTO,
                                         @PathVariable Long approvalId) {
        responseDTO.setId(approvalId);
        Approval response = modelMapper.map(responseDTO, Approval.class);
        Approval merged = approvalService.respondToSellerApproval(response);
        return modelMapper.map(merged, ApprovalDTO.class);
    }

    @GetMapping("/delete-account-requests")
    public Page<DeleteAccountRequestDTO> getDeleteAccountRequests(Pageable pageable,
                                                                  @RequestParam List<DeleteRequestStatus> statuses) {
        Page<DeleteAccountRequest> resultPage = deleteAccountService
                .listDeleteRequests(statuses, pageable);
        List<DeleteAccountRequestDTO> dtoList = mapList(resultPage.getContent(), DeleteAccountRequestDTO.class);
        return new PageImpl<>(dtoList, resultPage.getPageable(), resultPage.getTotalElements());
    }

    @Operation(description = "Only autoDeleteDate is processed. " +
            "This operation accepts the request and assigns a time for deletion.")
    @PutMapping("/delete-account-requests/{requestId}")
    public DeleteAccountRequestDTO acceptDeleteRequest(@PathVariable Long requestId,
                                                       @RequestBody DeleteAccountRequestDTO deleteRequestDTO) {
        deleteRequestDTO.setId(requestId);

        DeleteAccountRequest deleteRequest = modelMapper.map(deleteRequestDTO, DeleteAccountRequest.class);
        DeleteAccountRequest result = deleteAccountService.scheduleDelete(deleteRequest);
        return modelMapper.map(result, DeleteAccountRequestDTO.class);
    }

    @DeleteMapping("/delete-account-requests/{requestId}")
    public void discardDeleteRequest(@PathVariable Long requestId) {
        deleteAccountService.discardRequest(requestId);
    }

    <S, T> List<T> mapList(List<S> source, Class<T> targetClass) {
        return source.stream()
                .map(element -> modelMapper.map(element, targetClass))
                .collect(Collectors.toList());
    }

    public AdminApi(UserService userService, ApprovalService approvalService, DeleteAccountService deleteAccountService, ModelMapper modelMapper) {
        this.userService = userService;
        this.approvalService = approvalService;
        this.deleteAccountService = deleteAccountService;
        this.modelMapper = modelMapper;
    }
}
