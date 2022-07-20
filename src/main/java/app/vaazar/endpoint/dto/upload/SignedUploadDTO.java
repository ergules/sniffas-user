package app.vaazar.endpoint.dto.upload;

import lombok.Data;

@Data
public class SignedUploadDTO {
    private String fileName;
    private String signedURL;


    public SignedUploadDTO(String fileName, String signedURL) {
        this.fileName = fileName;
        this.signedURL = signedURL;
    }
}
