package app.vaazar.endpoint.dto;

import app.vaazar.domain.user.entity.User;

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
