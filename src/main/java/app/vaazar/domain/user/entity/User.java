package app.vaazar.domain.user.entity;

import app.vaazar.domain.address.Entity.Address;
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
        query = SELECT_BASIC_FIELDS + "WHERE id IN ?1",
        resultSetMapping = "BasicUserMapping")
@NamedNativeQuery(name = BASIC_USERS_ALL,
        query = SELECT_BASIC_FIELDS + "WHERE deleted <> 1",
        resultSetMapping = "BasicUserMapping")
@NamedNativeQuery(name = BASIC_USERS_ALL + ".count", resultSetMapping = "CountMapping",
        query = "SELECT count(*) FROM users WHERE deleted <> 1")
@NamedNativeQuery(name = BASIC_USERS_BY_NAME_QUERY,
        query = SELECT_BASIC_FIELDS + "WHERE deleted <> 1 AND " + NAME_FIELDS_QUERY,
        resultSetMapping = "BasicUserMapping")
@NamedNativeQuery(name = BASIC_USERS_BY_NAME_QUERY + ".count", resultSetMapping = "CountMapping",
        query = "SELECT count(*) FROM users WHERE deleted <> 1 AND " + NAME_FIELDS_QUERY)
@NamedNativeQuery(name = BASIC_USERS_BY_EMAIL_QUERY,
        query = SELECT_BASIC_FIELDS + "WHERE deleted <> 1 AND email like LOWER(CONCAT('%',?1,'%'))",
        resultSetMapping = "BasicUserMapping")
@NamedNativeQuery(name = BASIC_USERS_BY_EMAIL_QUERY + ".count", resultSetMapping = "CountMapping",
        query = "SELECT count(*) FROM users WHERE deleted <> 1 AND email like LOWER(CONCAT('%',?1,'%'))")
@NamedNativeQuery(name = BASIC_SELLERS_ALL,
        query = SELECT_BASIC_FIELDS + "WHERE deleted <> 1 AND role IN ('SELLER','COMPANY')",
        resultSetMapping = "BasicUserMapping")
@NamedNativeQuery(name = BASIC_SELLERS_ALL + ".count", resultSetMapping = "CountMapping",
        query = "SELECT count(*) FROM users WHERE deleted <> 1 AND role IN ('SELLER','COMPANY')")
@NamedNativeQuery(name = BASIC_SELLERS_BY_NAME_QUERY,
        query = SELECT_BASIC_FIELDS + "WHERE deleted <> 1 AND role IN ('SELLER','COMPANY') AND " + NAME_FIELDS_QUERY,
        resultSetMapping = "BasicUserMapping")
@NamedNativeQuery(name = BASIC_SELLERS_BY_NAME_QUERY + ".count", resultSetMapping = "CountMapping",
        query = "SELECT count(*) FROM users WHERE deleted <> 1 AND role IN ('SELLER','COMPANY') AND " + NAME_FIELDS_QUERY)
@NamedNativeQuery(name = BASIC_SELLERS_BY_EMAIL_QUERY,
        query = SELECT_BASIC_FIELDS + "WHERE deleted <> 1 AND role IN ('SELLER','COMPANY') AND email like LOWER(CONCAT('%',?1,'%'))",
        resultSetMapping = "BasicUserMapping")
@NamedNativeQuery(name = BASIC_SELLERS_BY_EMAIL_QUERY + ".count", resultSetMapping = "CountMapping",
        query = "SELECT count(*) FROM users WHERE deleted <> 1 AND role IN ('SELLER','COMPANY') AND email like LOWER(CONCAT('%',?1,'%'))")
@Entity
@Table(name = "USERS")
public class User extends BaseEntity implements UserDetails, Serializable {

    private static final String PREFIX = "USER.";
    public static final String BASIC_USERS_IN_LIST = PREFIX + "BasicUsersInList";
    public static final String BASIC_USERS_ALL = PREFIX + "BasicUsersAll";
    public static final String BASIC_USERS_BY_NAME_QUERY = PREFIX + "BasicUsersByQuery";
    public static final String BASIC_USERS_BY_EMAIL_QUERY = PREFIX + "BasicUsersByEmail";
    public static final String BASIC_SELLERS_ALL = PREFIX + "BasicSellersAll";
    public static final String BASIC_SELLERS_BY_NAME_QUERY = PREFIX + "BasicSellersByQuery";
    public static final String BASIC_SELLERS_BY_EMAIL_QUERY = PREFIX + "BasicSellersByEmail";

    static final String SELECT_BASIC_FIELDS = "SELECT id, username, firstname, lastname, profile_photo, role  FROM users u ";
    static final String NAME_FIELDS_QUERY = "(LOWER(CONCAT(firstname, ' ', lastname)) LIKE LOWER(CONCAT('%',?1,'%')) OR LOWER(username) LIKE LOWER(CONCAT('%',?1,'%')))";

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
