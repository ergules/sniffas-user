package app.vaazar.domain.deleteAccount.entity;

import app.vaazar.domain.baseEntity.BaseEntity;
import app.vaazar.domain.user.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "DELETE_ACCOUNT_REQUESTS")
@JsonIgnoreProperties(value = {"deleted", "version"})
public class DeleteAccountRequest extends BaseEntity {

    @ManyToOne
    private User requester;
    @Enumerated(EnumType.STRING)
    private DeleteRequestStatus status;
    private Instant autoDeleteDate;

    public User getRequester() {
        return requester;
    }

    public void setRequester(User requester) {
        this.requester = requester;
    }

    public DeleteRequestStatus getStatus() {
        return status;
    }

    public void setStatus(DeleteRequestStatus status) {
        this.status = status;
    }

    public Instant getAutoDeleteDate() {
        return autoDeleteDate;
    }

    public void setAutoDeleteDate(Instant autoDeleteDate) {
        this.autoDeleteDate = autoDeleteDate;
    }

    @Override
    public String toString() {
        return "DeleteAccountRequest{" +
                "requester=" + requester.getId() +
                ", status=" + status +
                ", autoDeleteDate=" + autoDeleteDate +
                '}';
    }
}
