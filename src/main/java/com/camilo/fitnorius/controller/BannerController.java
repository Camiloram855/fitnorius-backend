package com.camilo.fitnorius.controller;

import com.camilo.fitnorius.model.Banner;
import com.camilo.fitnorius.repository.BannerRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

@RestController
@RequestMapping("/api/banner")
@CrossOrigin(origins = "*")
public class BannerController {

    private final BannerRepository bannerRepository;

    @Value("${upload.dir:uploads/banner}")
    private String uploadDir;

    public BannerController(BannerRepository bannerRepository) {
        this.bannerRepository = bannerRepository;
    }

    @GetMapping
    public ResponseEntity<Banner> getBanner() {
        Optional<Banner> banner = bannerRepository.findById(1L);
        return banner.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadBanner(@RequestParam("file") MultipartFile file) throws IOException {
        File dir = new File(uploadDir);
        if (!dir.exists()) dir.mkdirs();

        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        File destination = new File(dir, fileName);
        file.transferTo(destination);

        String imageUrl = "/uploads/banner/" + fileName;

        // Actualiza el banner existente o crea uno nuevo
        Banner banner = bannerRepository.findById(1L).orElse(new Banner());
        banner.setImageUrl(imageUrl);
        bannerRepository.save(banner);

        return ResponseEntity.ok(banner);
    }
}
