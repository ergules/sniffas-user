package app.vaazar.domain.approval.boundary;

import app.vaazar.domain.approval.entity.ApplicationType;
import app.vaazar.domain.approval.entity.Approval;
import app.vaazar.domain.approval.entity.ApprovalStatus;
import app.vaazar.domain.user.entity.User;

import java.util.List;
import java.util.Optional;


public interface ApprovalService {

    Approval getApproval(Long id);

    List<Approval> getApprovalsOfUser(Long userId);

    List<Approval> listApprovals(Optional<ApprovalStatus> status);

    Approval requestSellerApproval(ApplicationType applicationType, User requester);

    Approval respondToSellerApproval(Approval adminResponse);
}
