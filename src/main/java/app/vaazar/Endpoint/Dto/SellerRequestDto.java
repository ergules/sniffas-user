package app.vaazar.Endpoint.Dto;

import app.vaazar.Domain.Approval.Entity.ApplicationType;
import app.vaazar.Domain.User.Entity.User;

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
