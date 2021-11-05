package app.vaazar.Endpoint.Dto;

import app.vaazar.Domain.User.Entity.User;

import javax.validation.Valid;

public class RegistrationDto {
    @Valid
    private User user;
    private String token;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
