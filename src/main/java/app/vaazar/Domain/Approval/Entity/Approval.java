package app.vaazar.Domain.Approval.Entity;

import app.vaazar.Domain.BaseEntity.BaseEntity;
import app.vaazar.Domain.BaseEntity.IdSerializer;
import app.vaazar.Domain.User.Entity.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import javax.persistence.*;

@Entity
@Table(name = "APPROVALS")
@JsonIgnoreProperties(value = {"deleted", "version"})
public class Approval extends BaseEntity {

    @ManyToOne
    private User requester;
    @Enumerated(EnumType.STRING)
    private ApplicationType applicationType;
    @Enumerated(EnumType.STRING)
    private ApprovalStatus approvalStatus;
    @ManyToOne
    @JsonSerialize(using = IdSerializer.class)
    private User evaluator;
    private String description;

    public boolean isPending() {
        return !getDeleted() && approvalStatus == ApprovalStatus.PENDING;
    }

    public User getRequester() {
        return requester;
    }

    public void setRequester(User requester) {
        this.requester = requester;
    }

    public ApplicationType getApplicationType() {
        return applicationType;
    }

    public void setApplicationType(ApplicationType applicationType) {
        this.applicationType = applicationType;
    }

    public ApprovalStatus getApprovalStatus() {
        return approvalStatus;
    }

    public void setApprovalStatus(ApprovalStatus approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    public User getEvaluator() {
        return evaluator;
    }

    public void setEvaluator(User evaluator) {
        this.evaluator = evaluator;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
