package app.vaazar.Domain.Approval.Controller;

import app.vaazar.Domain.Approval.Entity.Approval;
import app.vaazar.Domain.Approval.Entity.ApprovalStatus;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ApprovalRepository extends CrudRepository<Approval, Long> {
    List<Approval> findByRequesterId(Long requester);
    List<Approval> findByApprovalStatus(ApprovalStatus approvalStatus);
}
