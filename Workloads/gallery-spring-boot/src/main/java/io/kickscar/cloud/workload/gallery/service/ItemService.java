package io.kickscar.cloud.workload.gallery.service;

import io.kickscar.cloud.workload.gallery.domain.Item;
import io.kickscar.cloud.workload.gallery.repository.ItemRepository;
import io.kickscar.cloud.workload.gallery.storage.ImageStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemService {

    private final ImageStorage imageStorage;
    private final ItemRepository itemRepository;

    public List<Item> getItems() {
        return itemRepository.findAll();
    }

    public void registerItem(MultipartFile file, String comment) {
        String url = imageStorage.upload(file);
        Item newItem = itemRepository.save(new Item(url, comment));

        log.info("Uploaded: {}", newItem);
    }

    public void unregistItem(Long id) {
        itemRepository.findById(id).ifPresent(item -> {
            boolean removed = itemRepository.deleteById(item.getId());
            if(removed) {
                imageStorage.delete(item.getUrl());
            }
        });
    }
}
