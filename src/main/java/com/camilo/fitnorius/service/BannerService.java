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
import java.util.List;

@Service
public class BannerService {

    @Autowired
    private BannerRepository bannerRepository;

    private final String uploadDir = "uploads/banner/";

    // ✅ Obtener el banner actual (si no hay, devolver null)
    public Banner getCurrentBanner() {
        List<Banner> banners = bannerRepository.findAll();
        return banners.isEmpty() ? null : banners.get(0);
    }

    // ✅ Guardar o actualizar el banner
    public Banner saveBanner(MultipartFile file) {
        try {
            // Crear directorio si no existe
            File directory = new File(uploadDir);
            if (!directory.exists()) directory.mkdirs();

            // Guardar archivo físicamente
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(uploadDir + fileName);
            Files.write(filePath, file.getBytes());

            // 🧹 Eliminar banners anteriores para mantener solo uno
            bannerRepository.deleteAll();

            // Crear nuevo banner con la URL del archivo
            Banner banner = new Banner();
            banner.setImageUrl("/uploads/banner/" + fileName);

            return bannerRepository.save(banner);

        } catch (IOException e) {
            throw new RuntimeException("Error al guardar el banner", e);
        }
    }

    // ✅ Eliminar banner (reset)
    public void resetBanner() {
        bannerRepository.deleteAll();
    }
}
