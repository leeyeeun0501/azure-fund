package io.kickscar.cloud.workload.gallery.storage;

import io.kickscar.cloud.workload.gallery.config.ImageStorageConfig.ImageStorageProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

/**
 * NCP Object Storage 연동.
 *
 * NCP Object Storage는 Amazon S3 API 호환이라 AWS SDK v2 S3Client를 그대로 사용한다.
 * region(kr-standard)과 credential은 Config에서 NCP용으로 주입한다.
 *
 * <p>S3ImageStorage와 두 곳이 다르다.
 *
 * <ol>
 *   <li><b>업로드할 때 public-read ACL을 건다.</b> AWS는 Bucket Policy로, Azure는
 *       컨테이너 public access로 앱 밖에서 공개를 여는데, NCP에는 Bucket Policy가 없고
 *       버킷 공개는 목록만 공개한다. 파일마다 ACL을 걸지 않으면 브라우저에서 403이 온다.
 *   <li><b>올리는 주소와 내보내는 주소가 다를 수 있다.</b> 사설 도메인으로 올리더라도
 *       브라우저가 받아 갈 URL은 공인 도메인이어야 한다.
 * </ol>
 *
 * <p>공개 객체 URL은 path-style({endpoint}/{bucket}/{key})이다(2026-09-03 실측).
 */
@Slf4j
public class NcpObjectImageStorage implements ImageStorage {

    private final S3Client s3Client;
    private final ImageStorageProperties properties;

    public NcpObjectImageStorage(S3Client s3Client, ImageStorageProperties properties) {
        this.s3Client = s3Client;
        this.properties = properties;

        log.info("NcpObjectImageStorage Initialized [endpoint: {}, publicEndpoint: {}, bucket: {}]",
                properties.ncp().endpoint(),
                properties.ncp().resolvedPublicEndpoint(),
                properties.ncp().bucket());
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
                    .bucket(properties.ncp().bucket())
                    .key(fileName)
                    .contentType(file.getContentType())
                    // 이것이 없으면 올라간 객체가 비공개라 브라우저에서 403이 온다.
                    .acl(ObjectCannedACL.PUBLIC_READ)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));

            // 저장하고 화면에 내보내는 주소는 공인 도메인 기준이다.
            // path-style: {publicEndpoint}/{bucket}/{key}
            return String.format("%s/%s/%s",
                    properties.ncp().resolvedPublicEndpoint(), properties.ncp().bucket(), fileName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(String url) {
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(properties.ncp().bucket())
                .key(extractKey(url))
                .build());
    }

    /**
     * path-style URL에서 key를 잘라 낸다.
     *
     * <p>저장된 URL은 공인 도메인 기준이지만, 설정을 바꾸기 전에 저장된 것이 있을 수
     * 있으므로 업로드용 주소로도 맞춰 본다.
     */
    private String extractKey(String url) {
        ImageStorageProperties.Ncp ncp = properties.ncp();
        for (String base : new String[]{ncp.resolvedPublicEndpoint(), ncp.endpoint()}) {
            String prefix = base + "/" + ncp.bucket() + "/";
            if (url.startsWith(prefix)) {
                return url.substring(prefix.length());
            }
        }
        throw new IllegalArgumentException("Not an NCP Object Storage URL: " + url);
    }
}
