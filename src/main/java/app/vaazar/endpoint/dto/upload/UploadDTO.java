package app.vaazar.endpoint.dto.upload;

import app.vaazar.domain.upload.entity.UploadType;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class UploadDTO {
    @NotNull
    private UploadType uploadType;
    @NotNull
    @Size(min = 2, max = 4)
    private String extension;
    private Long entityId;
}
