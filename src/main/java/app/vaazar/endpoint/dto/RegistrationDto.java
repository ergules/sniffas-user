package app.vaazar.endpoint.dto;

import app.vaazar.endpoint.dto.user.UserDTO;
import lombok.Data;

import javax.validation.Valid;

@Data
public class RegistrationDto {
    @Valid
    private UserDTO user;
    private String token;
}
