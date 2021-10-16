package app.vaazar.Domain.User.Entity;

import app.vaazar.Domain.Address.Entity.Address;
import app.vaazar.Domain.BaseEntity.BaseEntity;
import app.vaazar.Domain.Company.Entity.Company;
import app.vaazar.Domain.i18n.SupportedLanguage;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Set;


@Entity
@Table(name = "USERS")
public class User extends BaseEntity implements UserDetails, Serializable {

    @NotNull
    private String firstname;
    @NotNull
    private String lastname;
    @Email @NotNull
    private String email;
    @NotNull
    @Column(unique = true)
    private String firebaseUid;
    private String mobilePhone;
    @NotNull
    private LocalDate birthdate;
    private String profilePhoto;

    private String storeName; // role.SELLER or role.COMPANY must fill this field
    private String storeLink;
    private String IBAN;

    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;
    @Enumerated(EnumType.STRING)
    private SupportedLanguage language;
    private String username;
    private String password;

    @OneToMany(mappedBy = "user")
    private List<Address> addresses;
    @OneToOne(mappedBy = "user")
    private Company company;

    @Transient
    @JsonIgnore
    private Set<SimpleGrantedAuthority> authorities;

    @PostLoad
    private void postLoadUser() {
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role.name());
        authorities = Set.of(authority);
    }

    public User() {
    }

    public void updateBaseFields(User other) {
        this.firstname = other.firstname;
        this.lastname = other.lastname;
        this.birthdate = other.birthdate;
        this.profilePhoto = other.profilePhoto;
        this.language = other.language;
    }

    public void updateSellerFields(User other) {
        this.storeName = other.storeName;
        this.storeLink = other.storeLink;
        this.IBAN = other.IBAN;
    }

    public boolean checkSellerInfo() {
        return ObjectUtils.allNotNull(storeName, IBAN, email);
    }

    public boolean checkCompanyInfo() {
        return checkSellerInfo() && company != null && company.checkForRequiredFields();
    }

    @Override
    public boolean isAccountNonExpired() {
        return !getDeleted();
    }

    @Override
    public boolean isAccountNonLocked() {
        return !getDeleted();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return !getDeleted();
    }

    @Override
    public boolean isEnabled() {
        return !getDeleted();
    }

    @Override
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
        postLoadUser();
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (email != null)
            this.email = email.strip().toLowerCase(Locale.ENGLISH);
        else this.email = null;
    }

    public String getFirebaseUid() {
        return firebaseUid;
    }

    public void setFirebaseUid(String firebaseUid) {
        this.firebaseUid = firebaseUid;
    }

    public String getMobilePhone() {
        return mobilePhone;
    }

    public void setMobilePhone(String mobilePhone) {
        this.mobilePhone = mobilePhone;
    }

    public LocalDate getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(LocalDate birthdate) {
        this.birthdate = birthdate;
    }

    public String getProfilePhoto() {
        return profilePhoto;
    }

    public void setProfilePhoto(String profilePhoto) {
        this.profilePhoto = profilePhoto;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getStoreLink() {
        return storeLink;
    }

    public void setStoreLink(String storeLink) {
        this.storeLink = storeLink;
    }

    public List<Address> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<Address> addresses) {
        this.addresses = addresses;
    }

    public String getIBAN() {
        return IBAN;
    }

    public void setIBAN(String IBAN) {
        this.IBAN = IBAN;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public SupportedLanguage getLanguage() {
        if (language == null)
            language = SupportedLanguage.getDefault();
        return language;
    }

    public void setLanguage(SupportedLanguage language) {
        this.language = language;
    }

    public void setAuthorities(Set<SimpleGrantedAuthority> authorities) {
        this.authorities = authorities;
    }

    @Override
    public Set<SimpleGrantedAuthority> getAuthorities() {
        return authorities;
    }
}
