package com.camilo.fitnorius.service;

import com.camilo.fitnorius.model.Banner;
import com.camilo.fitnorius.repository.BannerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@Service
public class BannerService {

    @Autowired
    private BannerRepository bannerRepository;

    private final String uploadDir = "uploads/banner/";

    public Banner getCurrentBanner() {
        Optional<Banner> banner = bannerRepository.findAll().stream().findFirst();
        return banner.orElse(new Banner("/uploads/banner/default-banner.png"));
    }

    public Banner saveBanner(MultipartFile file) {
        try {
            // Crea el directorio si no existe
            File directory = new File(uploadDir);
            if (!directory.exists()) directory.mkdirs();

            // Guarda el archivo físicamente
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(uploadDir + fileName);
            Files.write(filePath, file.getBytes());

            // Guarda o actualiza el registro en BD
            Banner banner = getCurrentBanner();
            banner.setImageUrl("/uploads/banner/" + fileName);
            bannerRepository.save(banner);

            return banner;

        } catch (IOException e) {
            throw new RuntimeException("Error al guardar banner", e);
        }
    }

    public void resetBanner() {
        bannerRepository.deleteAll();
    }
}
