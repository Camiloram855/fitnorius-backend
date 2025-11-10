package com.camilo.fitnorius.controller;

import com.camilo.fitnorius.model.ProductImagen;
import com.camilo.fitnorius.model.Product;
import com.camilo.fitnorius.repository.ProductImagenRepository;
import com.camilo.fitnorius.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/product-images")
@RequiredArgsConstructor
@CrossOrigin(origins = {
        "http://localhost:3000",
        "http://localhost:5173",
        "https://fitnorius-gym.vercel.app",
        "https://fitnorius-gym-git-main-juan-ks-projects-b6132ea5.vercel.app",
        "https://fitnorius-aghr9tnpz-juan-ks-projects-b6132ea5.vercel.app"
})
public class ProductImagenController {

    private final ProductRepository productRepository;
    private final ProductImagenRepository productImagenRepository;

    private static final String UPLOAD_DIR = "uploads/products/";

    // 🟢 Subir una o varias imágenes de un producto
    @PostMapping(value = "/upload/{productId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadImages(
            @PathVariable Long productId,
            @RequestPart("images") List<MultipartFile> images
    ) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + productId));

        try {
            Files.createDirectories(Paths.get(UPLOAD_DIR));

            for (MultipartFile image : images) {
                if (image != null && !image.isEmpty()) {
                    String fileName = System.currentTimeMillis() + "_" + Paths.get(image.getOriginalFilename()).getFileName();
                    Path filePath = Paths.get(UPLOAD_DIR, fileName);
                    Files.write(filePath, image.getBytes(), StandardOpenOption.CREATE);

                    ProductImagen imagen = ProductImagen.builder()
                            .product(product)
                            .imageUrl("/uploads/products/" + fileName)
                            .build();

                    productImagenRepository.save(imagen);
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Imágenes subidas correctamente");
            response.put("productId", productId);

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", "Error al guardar las imágenes"));
        }
    }

    // 🟢 Listar todas las imágenes de un producto
    @GetMapping("/{productId}")
    public ResponseEntity<List<ProductImagen>> getImagesByProduct(@PathVariable Long productId) {
        List<ProductImagen> images = productImagenRepository.findByProductId(productId);
        return ResponseEntity.ok(images);
    }

    // 🗑️ Eliminar una imagen específica
    @DeleteMapping("/{imageId}")
    public ResponseEntity<Map<String, String>> deleteImage(@PathVariable Long imageId) {
        ProductImagen imagen = productImagenRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Imagen no encontrada con ID: " + imageId));

        try {
            // ✅ Evitar errores de ruta o referencias circulares
            String relativePath = imagen.getImageUrl().replaceFirst("^/", "");
            Path imagePath = Paths.get(relativePath);

            if (Files.exists(imagePath)) {
                Files.delete(imagePath);
            }

            productImagenRepository.delete(imagen);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Imagen eliminada correctamente");
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", "Error al eliminar la imagen"));
        }
    }
}
