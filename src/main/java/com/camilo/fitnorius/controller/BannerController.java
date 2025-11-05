package com.camilo.fitnorius.controller;

import com.camilo.fitnorius.model.Banner;
import com.camilo.fitnorius.service.BannerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/banner")
@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://localhost:3000",
        "https://fitnorius-gym.vercel.app",
        "https://fitnorius-gym-git-main-juan-ks-projects-b6132ea5.vercel.app"
})
public class BannerController {

    @Autowired
    private BannerService bannerService;

    @PostMapping("/upload")
    public ResponseEntity<Banner> uploadBanner(@RequestParam("file") MultipartFile file) {
        Banner savedBanner = bannerService.saveBanner(file);
        return ResponseEntity.ok(savedBanner);
    }

    @GetMapping
    public ResponseEntity<Banner> getBanner() {
        Banner banner = bannerService.getCurrentBanner();
        if (banner == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(banner);
    }

    @DeleteMapping("/reset")
    public ResponseEntity<String> resetBanner() {
        bannerService.resetBanner();
        return ResponseEntity.ok("Banner eliminado");
    }
}
