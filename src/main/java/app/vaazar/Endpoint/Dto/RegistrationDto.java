package app.vaazar.Endpoint.Dto;

import app.vaazar.Domain.User.Entity.User;

public class RegistrationDto {
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
