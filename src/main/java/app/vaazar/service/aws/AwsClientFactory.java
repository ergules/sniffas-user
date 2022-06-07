package app.vaazar.service.aws;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import javax.annotation.PostConstruct;

@Configuration
public class AwsClientFactory {

    @Value("${aws.credentials.bucketUserAccessId}")
    private String accessId;
    @Value("${aws.credentials.bucketUserSecretKey}")
    private String secretKey;
    @Value("${aws.region.preferred}")
    private String defaultRegion;

    AwsCredentialsProvider credentialsProvider;

    @PostConstruct
    public void initAwsClientFactory() {
        final AwsCredentials credentials = AwsBasicCredentials.create(accessId, secretKey);
        credentialsProvider = StaticCredentialsProvider.create(credentials);
    }

    @Bean
    public S3Client getS3Client() {
        return S3Client.builder()
                .region(Region.of(defaultRegion))
                .credentialsProvider(credentialsProvider)
                .build();
    }

    @Bean
    public S3Presigner getS3Presigner() {
        return S3Presigner.builder()
                .region(Region.of(defaultRegion))
                .credentialsProvider(credentialsProvider)
                .build();
    }

}
