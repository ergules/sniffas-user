package app.vaazar.domain.user.entity;

import app.vaazar.domain.address.entity.Address;
import app.vaazar.domain.baseEntity.BaseEntity;
import app.vaazar.domain.company.entity.Company;
import app.vaazar.domain.i18n.SupportedLanguage;
import app.vaazar.endpoint.dto.BasicUser;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static app.vaazar.domain.user.entity.User.*;

@SqlResultSetMapping(name = "BasicUserMapping",
        classes = @ConstructorResult(
                targetClass = BasicUser.class,
                columns = {
                        @ColumnResult(name = "id", type = Long.class),
                        @ColumnResult(name = "username", type = String.class),
                        @ColumnResult(name = "firstname", type = String.class),
                        @ColumnResult(name = "lastname", type = String.class),
                        @ColumnResult(name = "profile_photo", type = String.class),
                        @ColumnResult(name = "role", type = String.class)
                }
        )
)
@SqlResultSetMapping(name = "CountMapping",
        classes = @ConstructorResult(
                targetClass = Long.class,
                columns = {
                        @ColumnResult(name = "count(*)", type = Long.class),
                }
        )
)
@NamedNativeQuery(name = BASIC_USERS_IN_LIST,
        query = "SELECT id, username, firstname, lastname, profile_photo, role FROM users u WHERE deleted <> 1 and id IN ?1",
        resultSetMapping = "BasicUserMapping")
@NamedQuery(name = BASIC_USERS_NO_QUERY,
        query = SELECT_FIELDS + "WHERE u.deleted <> true " + SELLER_FILTER)
@NamedQuery(name = BASIC_USERS_BY_NAME_QUERY,
        query = SELECT_FIELDS + "WHERE u.deleted <> true  AND " + NAME_QUERY + SELLER_FILTER)
@NamedQuery(name = BASIC_USERS_BY_EMAIL_QUERY,
        query = SELECT_FIELDS + "WHERE u.deleted <> true AND " + EMAIL_QUERY + SELLER_FILTER)
@Entity
@Table(name = "USERS")
public class User extends BaseEntity implements UserDetails, Serializable {

    private static final String PREFIX = "USER.";
    public static final String BASIC_USERS_IN_LIST = PREFIX + "BasicUsersInList";
    public static final String BASIC_USERS_NO_QUERY = PREFIX + "BasicUsersAll";
    public static final String BASIC_USERS_BY_NAME_QUERY = PREFIX + "BasicUsersByQuery";
    public static final String BASIC_USERS_BY_EMAIL_QUERY = PREFIX + "BasicUsersByEmail";

    static final String SELECT_FIELDS = "select new User(u.id, u.username, u.firstname, u.lastname, u.profilePhoto, u.role) from User u ";
    static final String NAME_QUERY = "(lower(concat(u.firstname, ' ', u.lastname)) like lower(concat('%', :qry, '%')) or lower(u.username) like lower(concat('%',:qry,'%'))) ";
    static final String EMAIL_QUERY = "(u.email like lower(concat('%',:qry,'%'))) ";
    static final String SELLER_FILTER = " AND (:sellerOnly = false OR u.role in ('SELLER', 'COMPANY')) ";

    @NotNull
    private String firstname;
    @NotNull
    private String lastname;
    @Email
    @NotNull
    private String email;
    @NotNull
    @Column(unique = true)
    private String username;
    @Column(unique = true) // unique = true did not work with nullable = false on hibernate dialect, add manually to db
    private String firebaseUid; // did not use @notNull bc validation would fail on register
    private String mobilePhone;
    private LocalDate birthdate;
    private String profilePhoto;

    private String storeName; // role.SELLER or role.COMPANY must fill this field
    private String storeLink;
    private String IBAN;

    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;
    @Enumerated(EnumType.STRING)
    private SupportedLanguage language;
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

    public User(Long id, String email, String username, Role role) {
        setId(id);
        this.email = email;
        this.username = username;
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role.name());
        authorities = Set.of(authority);
        language = SupportedLanguage.EN;
    }

    public User(Long id, String username, String firstname, String lastname, String profilePhoto, Role role) {
        setId(id);
        setRole(role);
        this.username = username;
        this.firstname = firstname;
        this.lastname = lastname;
        this.profilePhoto = profilePhoto;
    }

    @Override
    public String toString() {
        return "User{" +
                "firstname='" + firstname +
                "', lastname='" + lastname +
                "', email='" + email +
                "', firebaseUid='" + firebaseUid +
                "', mobilePhone='" + mobilePhone +
                "', birthdate=" + birthdate +
                ", profilePhoto='" + profilePhoto +
                "', storeName='" + storeName +
                "', storeLink='" + storeLink +
                "', IBAN='" + IBAN +
                "', role=" + role +
                ", language=" + language +
                ", username='" + username +
                ", company=" + company +
                '}';
    }

    public void updateBaseFields(User other) {
        this.firstname = other.firstname;
        this.lastname = other.lastname;
        this.birthdate = other.birthdate;
        this.profilePhoto = other.profilePhoto;
        this.language = other.language;
        this.username = other.username;
    }

    public void updateSellerFields(User other) {
        this.storeName = other.storeName;
        this.storeLink = other.storeLink;
        this.IBAN = other.IBAN;
    }

    public boolean checkSellerInfo() {
        return ObjectUtils.allNotNull(username, IBAN, email);
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
        if (role == null) return;
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
