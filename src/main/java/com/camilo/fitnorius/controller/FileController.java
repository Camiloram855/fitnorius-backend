package com.camilo.fitnorius.controller;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/uploads")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173" , "https://fitnorius-gym.vercel.app",
        "https://fitnorius-gym-git-main-juan-ks-projects-b6132ea5.vercel.app",
        "https://fitnorius-aghr9tnpz-juan-ks-projects-b6132ea5.vercel.app",})
public class FileController {

    // ✅ Endpoint para devolver imágenes de productos o categorías
    @GetMapping("/{folder}/{filename:.+}")
    public ResponseEntity<Resource> getFile(
            @PathVariable String folder,
            @PathVariable String filename
    ) {
        try {
            Path filePath = Paths.get("uploads")
                    .resolve(folder)
                    .resolve(filename)
                    .normalize();

            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                return ResponseEntity.noContent().build(); // 👈 evita reintentos
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.noContent().build(); // 👈 silencioso
        }
    }

}
