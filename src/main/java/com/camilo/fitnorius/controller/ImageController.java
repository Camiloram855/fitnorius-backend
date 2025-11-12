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

    // 🔹 Obtener imágenes por producto
    @GetMapping("/product")
    public ResponseEntity<List<Image>> getByProduct(@RequestParam Long productId) {
        try {
            List<Image> images = imageService.findByProductId(productId);
            return ResponseEntity.ok(images);
        } catch (Exception e) {
            System.err.println("❌ Error obteniendo imágenes del producto ID " + productId + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // 🔹 Obtener imágenes por categoría
    @GetMapping("/category")
    public ResponseEntity<List<Image>> getByCategory(@RequestParam Long categoryId) {
        try {
            List<Image> images = imageService.findByCategoryId(categoryId);
            return ResponseEntity.ok(images);
        } catch (Exception e) {
            System.err.println("❌ Error obteniendo imágenes de la categoría ID " + categoryId + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // 🔹 Subir imágenes (producto o categoría)
    @PostMapping("/upload")
    public ResponseEntity<List<Image>> uploadImages(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "productId", required = false) Long productId,
            @RequestParam(value = "categoryId", required = false) Long categoryId
    ) {
        try {
            List<Image> savedImages = imageService.saveImages(files, productId, categoryId);

            String target = (productId != null)
                    ? "producto ID: " + productId
                    : (categoryId != null)
                    ? "categoría ID: " + categoryId
                    : "sin destino especificado";

            System.out.println("✅ Imágenes subidas correctamente para " + target);
            return ResponseEntity.ok(savedImages);

        } catch (Exception e) {
            System.err.println("❌ Error subiendo imágenes: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // 🔹 Eliminar imagen individual
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        try {
            boolean deleted = imageService.deleteImage(id);
            if (deleted) {
                System.out.println("🗑️ Imagen eliminada correctamente (ID: " + id + ")");
                return ResponseEntity.ok("✅ Imagen eliminada correctamente (ID: " + id + ")");
            } else {
                System.err.println("⚠️ Imagen no encontrada con ID: " + id);
                return ResponseEntity.status(404).body("⚠️ Imagen no encontrada con ID: " + id);
            }
        } catch (Exception e) {
            System.err.println("❌ Error eliminando imagen ID " + id + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body("❌ Error eliminando imagen ID: " + id + " — " + e.getMessage());
        }
    }
}
