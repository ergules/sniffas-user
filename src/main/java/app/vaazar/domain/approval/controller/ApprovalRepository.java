package app.vaazar.domain.approval.controller;

import app.vaazar.domain.approval.entity.Approval;
import app.vaazar.domain.approval.entity.ApprovalStatus;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ApprovalRepository extends CrudRepository<Approval, Long> {
    List<Approval> findByRequesterId(Long requester);
    List<Approval> findByApprovalStatus(ApprovalStatus approvalStatus);
}
