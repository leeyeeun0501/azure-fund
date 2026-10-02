package io.kickscar.cloud.workload.gallery.storage;


import org.springframework.web.multipart.MultipartFile;

public interface ImageStorage {
    String upload(MultipartFile file);
    void delete(String url);
}
