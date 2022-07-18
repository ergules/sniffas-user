package app.vaazar.endpoint.dto.user;

import app.vaazar.domain.i18n.SupportedLanguage;
import app.vaazar.domain.user.entity.Role;
import app.vaazar.endpoint.dto.address.AddressDTO;
import app.vaazar.endpoint.dto.company.CompanyDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include;

@Data
@JsonInclude(Include.NON_NULL)
public class UserDTO {

    private Long id;
    private Long createdBy;
    private Instant createdAt;
    private Boolean deleted;

    @NotNull
    private String firstname;
    @NotNull
    private String lastname;
    @Email
    @NotNull
    private String email;
    @NotNull
    private String username;
    private String firebaseUid;
    private String mobilePhone;
    private LocalDate birthdate;
    private String profilePhoto;

    private String storeName; // role.SELLER or role.COMPANY must fill this field
    private String storeLink;
    private String IBAN;

    private Role role = Role.USER;
    private SupportedLanguage language;
    private String password;

    private List<AddressDTO> addresses;
    private CompanyDTO company;
}
