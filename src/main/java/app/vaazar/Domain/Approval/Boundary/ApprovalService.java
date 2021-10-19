package app.vaazar.Domain.Approval.Boundary;

import app.vaazar.Domain.Approval.Controller.ApprovalRepository;
import app.vaazar.Domain.Approval.Entity.Approval;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ApprovalService {

    public final ApprovalRepository approvalRepo;

    public List<Approval> getApprovalsOfUser(Long userId) {
        return approvalRepo.findByRequesterId(userId);
    }


    public ApprovalService(ApprovalRepository approvalRepo) {
        this.approvalRepo = approvalRepo;
    }
}
