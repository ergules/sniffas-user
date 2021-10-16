package app.vaazar.Domain.Approval.Controller;

import app.vaazar.Domain.Approval.Entity.Approval;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ApprovalRepository extends CrudRepository<Approval, Long> {
    List<Approval> findByRequesterId(Long requester);
}
