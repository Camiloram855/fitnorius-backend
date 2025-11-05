package com.camilo.fitnorius.controller;

import com.camilo.fitnorius.model.Banner;
import com.camilo.fitnorius.service.BannerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/banner")
@CrossOrigin(origins = "*")
public class BannerController {

    @Autowired
    private BannerService bannerService;

    @GetMapping
    public ResponseEntity<Banner> getBanner() {
        return ResponseEntity.ok(bannerService.getCurrentBanner());
    }

    @PostMapping("/upload")
    public ResponseEntity<Banner> uploadBanner(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(bannerService.saveBanner(file));
    }

    @DeleteMapping("/reset")
    public ResponseEntity<Void> resetBanner() {
        bannerService.resetBanner();
        return ResponseEntity.noContent().build();
    }
}
