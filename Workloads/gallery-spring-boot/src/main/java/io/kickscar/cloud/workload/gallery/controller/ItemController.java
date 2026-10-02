package io.kickscar.cloud.workload.gallery.controller;

import io.kickscar.cloud.workload.gallery.domain.Item;
import io.kickscar.cloud.workload.gallery.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @GetMapping("/")
    public String readItems(Model model) {
        List<Item> items = itemService.getItems();
        model.addAttribute("items", items);

        return "index";
    }

    @PostMapping(value="/new", consumes={MediaType.MULTIPART_FORM_DATA_VALUE})
    public String createItem(@RequestParam("file") MultipartFile file, @RequestParam(defaultValue = "") String comment) {

        log.info("Request[POST, Content-Type: multipart/form-data] [{}, {}]", file.getOriginalFilename(), comment);

        itemService.registerItem(file, comment);
        return "redirect:/";
    }

    @GetMapping("/{id}/delete")
    public String deleteItem(@PathVariable Long id) {
        log.info("Request[DELETE {}]", id);

        itemService.unregistItem(id);
        return "redirect:/";
    }

}
