package app.vaazar.endpoint.dto.deleteAccount;

import app.vaazar.domain.deleteAccount.entity.DeleteRequestStatus;
import app.vaazar.endpoint.dto.user.UserDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.Instant;

import static com.fasterxml.jackson.annotation.JsonInclude.Include;

@Data
@JsonInclude(Include.NON_NULL)
public class DeleteAccountRequestDTO {

    private Long id;
    private Long createdBy;
    private Instant createdAt;
    private Boolean deleted;

    private UserDTO requester;
    private DeleteRequestStatus status;
    private Instant autoDeleteDate;
}
