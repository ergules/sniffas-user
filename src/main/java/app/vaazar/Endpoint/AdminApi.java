package app.vaazar.Endpoint;

import app.vaazar.Domain.Approval.Boundary.ApprovalService;
import app.vaazar.Domain.Approval.Entity.Approval;
import app.vaazar.Domain.Approval.Entity.ApprovalStatus;
import app.vaazar.Domain.DeleteAccount.Boundary.DeleteAccountService;
import app.vaazar.Domain.DeleteAccount.Entity.DeleteAccountRequest;
import app.vaazar.Domain.DeleteAccount.Entity.DeleteRequestStatus;
import app.vaazar.Domain.User.Boundary.UserService;
import app.vaazar.Domain.User.Entity.User;
import app.vaazar.Endpoint.Dto.BasicUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/admin")
@SecurityRequirement(name = "jwt")
@PreAuthorize("hasRole('ADMIN')")
public class AdminApi {

    private final UserService userService;
    private final ApprovalService approvalService;
    private final DeleteAccountService deleteAccountService;

    @GetMapping("/users")
    public Page<BasicUser> findUsers(@RequestParam Optional<String> query,
                                     @RequestParam(defaultValue = "false") boolean seller,
                                     Pageable pageable) {
        return userService.findUsers(query, seller, pageable);
    }

    @GetMapping("/users/{userId}")
    public User findUser(@PathVariable Long userId) {
        return userService.findById(userId);
    }

    @GetMapping("/approvals")
    public List<Approval> findApprovals(Optional<ApprovalStatus> status) {
        return approvalService.listApprovals(status);
    }

    @PutMapping("/approvals/{approvalId}")
    public Approval respondToApproval(@RequestBody Approval response,
                                      @PathVariable Long approvalId) {
        response.setId(approvalId);
        return approvalService.respondToSellerApproval(response);
    }

    @GetMapping("/delete-account-requests")
    public Page<DeleteAccountRequest> getDeleteAccountRequests(Pageable pageable,
                                                               @RequestParam List<DeleteRequestStatus> statuses) {
        return deleteAccountService.listDeleteRequests(statuses, pageable);
    }

    @Operation(description = "Only autoDeleteDate is processed. " +
            "This operation accepts the request and assigns a time for deletion.")
    @PutMapping("/delete-account-requests/{requestId}")
    public DeleteAccountRequest acceptDeleteRequest(@PathVariable Long requestId,
                                                    @RequestBody DeleteAccountRequest deleteRequest) {
        deleteRequest.setId(requestId);
        return deleteAccountService.scheduleDelete(deleteRequest);
    }

    @DeleteMapping("/delete-account-requests/{requestId}")
    public void acceptDeleteRequest(@PathVariable Long requestId) {
        deleteAccountService.discardRequest(requestId);
    }

    public AdminApi(UserService userService, ApprovalService approvalService, DeleteAccountService deleteAccountService) {
        this.userService = userService;
        this.approvalService = approvalService;
        this.deleteAccountService = deleteAccountService;
    }
}
