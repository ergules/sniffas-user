package app.vaazar.endpoint.dto.address;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.Instant;

import static com.fasterxml.jackson.annotation.JsonInclude.Include;

@Data
@JsonInclude(Include.NON_NULL)
public class AddressDTO {

    private Long id;
    private Long createdBy;
    private Instant createdAt;
    private Boolean deleted;

    private String title;
    private String country;
    private String province;
    private String city;
    private String town;
    private String zipCode;
    private String streetName;
    private String streetNo;
    private String addressString;

    @JsonProperty("user")
    private Long userId;
    @JsonProperty("company")
    private Long companyId;
    private String phrase;

}
