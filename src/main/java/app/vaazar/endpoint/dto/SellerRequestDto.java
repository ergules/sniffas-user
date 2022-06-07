package app.vaazar.endpoint.dto;

import app.vaazar.domain.approval.entity.ApplicationType;
import app.vaazar.domain.user.entity.User;

public class SellerRequestDto {
    private ApplicationType applicationType;
    private User user;

    public ApplicationType getApplicationType() {
        return applicationType;
    }

    public void setApplicationType(ApplicationType applicationType) {
        this.applicationType = applicationType;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
