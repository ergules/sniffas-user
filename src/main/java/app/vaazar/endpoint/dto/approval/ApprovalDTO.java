package app.vaazar.endpoint.dto.approval;

import app.vaazar.domain.approval.entity.ApplicationType;
import app.vaazar.domain.approval.entity.ApprovalStatus;
import app.vaazar.domain.user.entity.User;
import app.vaazar.endpoint.dto.user.UserDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.Instant;

import static com.fasterxml.jackson.annotation.JsonInclude.Include;

@Data
@JsonInclude(Include.NON_NULL)
public class ApprovalDTO {

    private Long id;
    private Long createdBy;
    private Instant createdAt;
    private Boolean deleted;

    private UserDTO requester;
    private ApplicationType applicationType;
    private ApprovalStatus approvalStatus;
    @JsonProperty("evaluator")
    private User evaluatorId;
    private String description;
}
