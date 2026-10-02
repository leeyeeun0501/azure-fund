package io.kickscar.cloud.workload.gallery.storage;

import io.kickscar.cloud.workload.gallery.config.ImageStorageConfig.ImageStorageProperties;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobHttpHeaders;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Slf4j
public class AzureBlobImageStorage implements ImageStorage {

    private final BlobContainerClient containerClient;
    private final ImageStorageProperties properties;

    public AzureBlobImageStorage(BlobContainerClient containerClient, ImageStorageProperties properties) {
        this.containerClient = containerClient;
        this.properties = properties;

        log.info("AzureBlobImageStorage Initialized [account: {}, container: {}]",
                properties.blob().account(), properties.blob().container());
    }

    @Override
    public String upload(MultipartFile file) {
        try {
            String blobName = "images/" + UUID.randomUUID() + Optional
                    .ofNullable(file.getOriginalFilename())
                    .filter(f -> f.contains("."))
                    .map(f -> f.substring(f.lastIndexOf(".")))
                    .orElse("");

            BlobClient blobClient = containerClient.getBlobClient(blobName);
            blobClient.upload(BinaryData.fromBytes(file.getBytes()), true);
            blobClient.setHttpHeaders(new BlobHttpHeaders().setContentType(file.getContentType()));

            return blobClient.getBlobUrl();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(String url) {
        String prefix = containerClient.getBlobContainerUrl();
        if (!prefix.endsWith("/")) {
            prefix = prefix + "/";
        }

        String blobName = url.substring(url.indexOf(prefix) + prefix.length());
        containerClient.getBlobClient(blobName).deleteIfExists();
    }
}
