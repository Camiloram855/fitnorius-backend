package com.camilo.fitnorius.controller;

import com.camilo.fitnorius.model.Image;
import com.camilo.fitnorius.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<List<Image>> getByProduct(@RequestParam Long productId) {
        try {
            List<Image> images = imageService.findByProductId(productId);
            return ResponseEntity.ok(images);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<List<Image>> uploadImages(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "productId", required = false) Long productId
    ) {
        try {
            List<Image> savedImages = imageService.saveImages(files, productId);
            return ResponseEntity.ok(savedImages);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        try {
            boolean deleted = imageService.deleteImage(id);
            if (deleted) {
                return ResponseEntity.ok("✅ Imagen eliminada correctamente (ID: " + id + ")");
            } else {
                return ResponseEntity.status(404).body("⚠️ Imagen no encontrada con ID: " + id);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body("❌ Error eliminando imagen ID: " + id + " — " + e.getMessage());
        }
    }
}
