package com.camilo.fitnorius.controller;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

@RestController
@RequestMapping("/uploads")
public class FileController {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            ".jpg", ".jpeg", ".png", ".webp", ".gif", ".avif"
    );

    // ✅ Endpoint para devolver imágenes de productos o categorías
    @GetMapping("/{folder}/{filename:.+}")
    public ResponseEntity<Resource> getFile(
            @PathVariable String folder,
            @PathVariable String filename
    ) {
        try {
            Path basePath = Paths.get("uploads").toAbsolutePath().normalize();
            Path filePath = basePath.resolve(folder).resolve(filename).normalize();

            // Impide traversal (..) y limita el endpoint a imágenes conocidas.
            if (!filePath.startsWith(basePath) || !Files.isRegularFile(filePath)) {
                return ResponseEntity.notFound().build();
            }
            String lowerName = filePath.getFileName().toString().toLowerCase(java.util.Locale.ROOT);
            boolean allowedExtension = ALLOWED_EXTENSIONS.stream().anyMatch(lowerName::endsWith);
            if (!allowedExtension) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new UrlResource(filePath.toUri());
            MediaType mediaType = MediaTypeFactory.getMediaType(resource)
                    .orElse(MediaType.APPLICATION_OCTET_STREAM);
            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline()
                            .filename(resource.getFilename(), StandardCharsets.UTF_8)
                            .build().toString())
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

}
