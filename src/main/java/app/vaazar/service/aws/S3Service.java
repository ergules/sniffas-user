package app.vaazar.service.aws;

import app.vaazar.domain.upload.entity.UploadType;
import app.vaazar.service.FileStorage;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import javax.annotation.PostConstruct;
import java.time.Duration;

import static app.vaazar.domain.upload.entity.UploadType.PROFILE_PIC;

@Service
public class S3Service implements FileStorage {

    private final Logger log;
    private final S3Presigner presigner;
    private final S3Client s3Client;
    private static final Duration signExpire = Duration.ofMinutes(3);

    @Value("${aws.s3.userDocumentsBucket}")
    String documentsBucket;
    @Value("${aws.s3.userProfilesBucket}")
    String profilePicsBucket;

    public String preSignWithObjectKey(String key, UploadType type) {
        log.info("Sign key {} for {}", key, type.name());
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(type == PROFILE_PIC ? profilePicsBucket : documentsBucket)
                .key(key)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(signExpire)
                .putObjectRequest(objectRequest)
                .build();

        PresignedPutObjectRequest request = presigner.presignPutObject(presignRequest);
        return request.url().toString();
    }


    public S3Service(S3Presigner presigner, S3Client s3Client, Logger log) {
        this.log = log;
        this.presigner = presigner;
        this.s3Client = s3Client;
    }

    @PostConstruct
    public void initS3Service() {
        log.info("s3 service started with {}, {}", presigner, s3Client);
        log.info("\t profile bucket : {}, document bucket {}", profilePicsBucket, documentsBucket);
    }
}
