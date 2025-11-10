package com.camilo.fitnorius.controller;

import com.camilo.fitnorius.model.Image;
import com.camilo.fitnorius.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ImageController {

    private final ImageService imageService;

    @GetMapping
    public List<Image> getByProduct(@RequestParam Long productId) {
        return imageService.findByProductId(productId);
    }

    @PostMapping("/upload")
    public List<Image> uploadImages(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "productId", required = false) Long productId
    ) {
        return imageService.saveImages(files, productId);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        imageService.deleteImage(id);
    }
}
