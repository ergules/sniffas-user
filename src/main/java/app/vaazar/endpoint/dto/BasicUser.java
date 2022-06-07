package app.vaazar.endpoint.dto;

import app.vaazar.domain.user.entity.Role;

public class BasicUser {
    private Long id;
    private String username;
    private String firstname;
    private String lastname;
    private String profilePhoto;
    private Role role;

    public BasicUser() {
    }

    public BasicUser(Long id, String username, String firstname, String lastname, String profilePhoto, String role) {
        this.id = id;
        this.username = username;
        this.firstname = firstname;
        this.lastname = lastname;
        this.profilePhoto = profilePhoto;
        if (role != null)
            this.role = Role.valueOf(role);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getProfilePhoto() {
        return profilePhoto;
    }

    public void setProfilePhoto(String profilePhoto) {
        this.profilePhoto = profilePhoto;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
