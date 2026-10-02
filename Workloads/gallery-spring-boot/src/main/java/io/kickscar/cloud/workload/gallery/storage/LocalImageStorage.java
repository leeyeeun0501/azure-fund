package io.kickscar.cloud.workload.gallery.storage;

import io.kickscar.cloud.workload.gallery.config.ImageStorageConfig.ImageStorageProperties;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

@Slf4j
public class LocalImageStorage implements ImageStorage {

    private final ImageStorageProperties properties;

    public LocalImageStorage(ImageStorageProperties properties) {
        this.properties = properties;

        log.info("LocalImageStorage Initialized [storage local path: {}, url prefix: {}]", properties.resolvedLocalPath(), properties.baseUrl());
    }

    @Override
    public String upload(MultipartFile file) {
        try {
            File uploadDirectory = new File(properties.resolvedLocalPath());

            if (!uploadDirectory.exists() && !uploadDirectory.mkdirs()) {
                return null;
            }

            if (file.isEmpty()) {
                return null;
            }

            String originFilename = Optional.ofNullable(file.getOriginalFilename()).orElse("");
            String extName = originFilename.substring(originFilename.lastIndexOf('.'));
            String saveFilename = UUID.randomUUID().toString().replace("-", "") + extName;

            OutputStream os = new FileOutputStream(properties.resolvedLocalPath() + "/" + saveFilename);
            os.write(file.getBytes());
            os.close();

            return properties.baseUrl() + "/" + saveFilename;
        } catch (IOException e) {
            log.error("Error occurred while uploading file: {}", file, e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(String url) {
        if (url == null || url.isEmpty()) {
            return;
        }

        try {
            String fileName = url.substring(url.lastIndexOf("/") + 1);

            Path filePath = Paths.get(properties.resolvedLocalPath()).resolve(fileName);
            File file = filePath.toFile();

            if (!file.exists()) {
                log.warn("File not found for deletion: {}", fileName);
                return;
            }

            if (!file.delete()) {
                log.warn("Failed to delete file: {}", fileName);
                return;
            }

            log.info("File deleted successfully: {}", fileName);

        } catch (Exception e) {
            log.error("Error occurred while deleting file: {}", url, e);
            throw new RuntimeException(e);
        }
    }
}
