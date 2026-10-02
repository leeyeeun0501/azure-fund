package io.kickscar.cloud.workload.gallery.config;

import io.kickscar.cloud.workload.gallery.storage.AzureBlobImageStorage;
import io.kickscar.cloud.workload.gallery.storage.ImageStorage;
import io.kickscar.cloud.workload.gallery.storage.LocalImageStorage;
import io.kickscar.cloud.workload.gallery.storage.NcpObjectImageStorage;
import io.kickscar.cloud.workload.gallery.storage.NcpServerRoleCredentialsProvider;
import io.kickscar.cloud.workload.gallery.storage.S3ImageStorage;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import software.amazon.awssdk.core.checksums.RequestChecksumCalculation;
import software.amazon.awssdk.core.checksums.ResponseChecksumValidation;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.regions.providers.DefaultAwsRegionProviderChain;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@EnableConfigurationProperties(ImageStorageConfig.ImageStorageProperties.class)
public class ImageStorageConfig implements WebMvcConfigurer {

    private final ImageStorageProperties imageStorageProperties;

    @Autowired
    public ImageStorageConfig(ImageStorageProperties imageStorageProperties) {
        this.imageStorageProperties = imageStorageProperties;
    }

    /**
     * 업로드한 파일을 정적 리소스로 노출한다. 로컬 스토리지일 때만 필요하다.
     *
     * <p>{@code @ConditionalOnProperty}는 {@code @Bean} 메서드와 설정 클래스에서만 평가되고
     * 인터페이스 구현 메서드에는 적용되지 않으므로 여기서는 직접 확인한다.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        if (!"local".equals(imageStorageProperties.type())) {
            return;
        }
        registry.addResourceHandler(imageStorageProperties.baseUrl() + "/**")
                .addResourceLocations(Path.of(imageStorageProperties.resolvedLocalPath()).toUri().toString());
    }

    @Bean
    @ConditionalOnProperty(name = "app.storage.type", havingValue = "local")
    public ImageStorage localImageStorage() {
        return new LocalImageStorage(imageStorageProperties);
    }

    @Bean
    @ConditionalOnProperty(name = "app.storage.type", havingValue = "s3")
    public Region awsRegion() {
        return DefaultAwsRegionProviderChain.builder().build().getRegion();
    }

    @Bean
    @ConditionalOnProperty(name = "app.storage.type", havingValue = "s3")
    public S3Client s3Client(Region region) {
        return S3Client.builder()
                .region(region)
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "app.storage.type", havingValue = "s3")
    public ImageStorage s3ImageStorage(Region region, S3Client s3Client) {
        return new S3ImageStorage(region, s3Client, imageStorageProperties);
    }

    @Bean
    @ConditionalOnProperty(name = "app.storage.type", havingValue = "azure-blob")
    public BlobContainerClient blobContainerClient() {
        String endpoint = String.format("https://%s.blob.core.windows.net", imageStorageProperties.blob().account());
        return new BlobServiceClientBuilder()
                .endpoint(endpoint)
                .credential(new DefaultAzureCredentialBuilder().build())
                .buildClient()
                .getBlobContainerClient(imageStorageProperties.blob().container());
    }

    @Bean
    @ConditionalOnProperty(name = "app.storage.type", havingValue = "azure-blob")
    public ImageStorage azureBlobImageStorage(BlobContainerClient blobContainerClient) {
        return new AzureBlobImageStorage(blobContainerClient, imageStorageProperties);
    }

    @Bean
    @ConditionalOnProperty(name = "app.storage.type", havingValue = "ncp-object")
    public S3Client ncpS3Client() {
        ImageStorageProperties.Ncp ncp = imageStorageProperties.ncp();

        // 자격증명은 Server Role(키리스)만 쓴다. 키를 서버에 두지 않는다.
        return S3Client.builder()
                .region(Region.of(ncp.region()))
                .endpointOverride(URI.create(ncp.endpoint()))
                .credentialsProvider(new NcpServerRoleCredentialsProvider())
                .forcePathStyle(true)
                // SDK 기본값(WHEN_SUPPORTED)은 PutObject 본문을 aws-chunked로 감싸고
                // CRC32 트레일러를 붙인다. NCP Object Storage가 그 형식을 받지 않아
                // AccessDenied로 막히므로 필요한 경우에만 계산하도록 낮춘다.
                .requestChecksumCalculation(RequestChecksumCalculation.WHEN_REQUIRED)
                .responseChecksumValidation(ResponseChecksumValidation.WHEN_REQUIRED)
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "app.storage.type", havingValue = "ncp-object")
    public ImageStorage ncpObjectImageStorage(S3Client ncpS3Client) {
        return new NcpObjectImageStorage(ncpS3Client, imageStorageProperties);
    }

    @ConfigurationProperties(prefix = "app.storage")
    public record ImageStorageProperties(String type, Local local, S3 s3, Blob blob, Ncp ncp) {
        public record Local(String path, Url url) {}
        public record Url(String prefix) {}
        public record S3(String bucket) {}
        public record Blob(String account, String container) {}
        public record Ncp(String bucket, String endpoint, String publicEndpoint,
                          String region) {

            /**
             * 브라우저가 이미지를 받아 가는 주소의 기준.
             *
             * <p>NCP Object Storage는 사설 통신용 도메인이 공인 도메인과 다르다
             * (AWS VPC Endpoint나 Azure Private Endpoint는 같은 FQDN을 쓴다).
             * 서버가 사설 도메인으로 올리더라도 브라우저에 내보내는 URL은 공인
             * 도메인이어야 하므로 둘을 나눈다.
             *
             * <p>지정하지 않으면 {@code endpoint}를 그대로 쓴다.
             */
            public String resolvedPublicEndpoint() {
                return (publicEndpoint == null || publicEndpoint.isBlank()) ? endpoint : publicEndpoint;
            }
        }

        public String baseUrl() {
            return local.url().prefix().replaceAll("/+$", "");
        }

        public String resolvedLocalPath() {
            if (local == null || local.path() == null) {
                throw new IllegalStateException("fs.path is required");
            }

            Path path = Paths.get(local.path());

            if (!path.isAbsolute()) {
                path = Paths.get(System.getProperty("user.dir")).resolve(path);
            }

            return path.normalize().toAbsolutePath().toString();
        }
    }
}
