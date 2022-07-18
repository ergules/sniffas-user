package app.vaazar.endpoint.dto.company;

import app.vaazar.domain.company.entity.CompanyType;
import app.vaazar.endpoint.dto.address.AddressDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.Set;

import static com.fasterxml.jackson.annotation.JsonInclude.Include;

@Data
@JsonInclude(Include.NON_NULL)
public class CompanyDTO {

    private Long id;
    private Long createdBy;
    private Instant createdAt;
    private Boolean deleted;

    private String companyName;
    private String website;
    @Size(min = 11, max = 11)
    private String taxId;
    private String tradeRegistry;   // file
    private String taxRegistry;     // file
    private String IBAN;
    private CompanyType companyType;

    private String companyExecutiveFirstName;
    private String companyExecutiveLastName;
    private String companyExecutiveIdentity; // file

    private String phoneNumber;
    private AddressDTO address;

    private Set<ShareholderDTO> shareHolders;

    @JsonProperty("user")
    private Long userId;

}
