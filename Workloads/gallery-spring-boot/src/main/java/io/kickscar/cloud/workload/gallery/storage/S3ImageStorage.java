package io.kickscar.cloud.workload.gallery.storage;

import io.kickscar.cloud.workload.gallery.config.ImageStorageConfig.ImageStorageProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Slf4j
public class S3ImageStorage implements ImageStorage {

    private final Region region;
    private final S3Client s3Client;
    private final ImageStorageProperties properties;

    public S3ImageStorage(Region region,  S3Client s3Client, ImageStorageProperties properties) {
        this.region = region;
        this.s3Client = s3Client;
        this.properties = properties;

        log.info("S3ImageStorage Initialized [region: {}, s3bucket: {}]", region, properties.s3().bucket());
    }

    @Override
    public String upload(MultipartFile file) {
        try {
            String fileName = "images/" + UUID.randomUUID() + Optional
                    .ofNullable(file.getOriginalFilename())
                    .filter(f -> f.contains("."))
                    .map(f -> f.substring(f.lastIndexOf(".")))
                    .orElse("");

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(properties.s3().bucket())
                    .key(fileName)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));

            return String.format("https://%s.s3.%s.amazonaws.com/%s", properties.s3().bucket(), region, fileName);
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(String url) {
        // URL에서 Key(경로)만 추출하여 삭제 로직 구현
        String key = url.substring(url.lastIndexOf(".com/") + 5);
        s3Client.deleteObject(DeleteObjectRequest.builder().bucket(properties.s3().bucket()).key(key).build());
    }
}
