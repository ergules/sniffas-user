package app.vaazar.endpoint.dto;

import app.vaazar.domain.approval.entity.ApplicationType;
import app.vaazar.endpoint.dto.user.UserDTO;
import lombok.Data;

@Data
public class SellerRequestDto {
    private ApplicationType applicationType;
    private UserDTO user;
}
