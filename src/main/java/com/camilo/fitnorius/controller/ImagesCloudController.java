package com.camilo.fitnorius.controller;

import com.camilo.fitnorius.model.ImagesCloud;
import com.camilo.fitnorius.service.ImagesCloudService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/images-cloud")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ImagesCloudController {

    private final ImagesCloudService service;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            ImagesCloud image = service.upload(file);
            return ResponseEntity.ok(image);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al subir la imagen: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<ImagesCloud>> getAllImages() {
        return ResponseEntity.ok(service.findAll());
    }

    @DeleteMapping("/{publicId}")
    public ResponseEntity<String> deleteImage(@PathVariable String publicId) {
        try {
            service.delete(publicId);
            return ResponseEntity.ok("Imagen eliminada correctamente");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al eliminar la imagen: " + e.getMessage());
        }
    }
}
