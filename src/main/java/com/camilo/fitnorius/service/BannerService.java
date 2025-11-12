package com.camilo.fitnorius.service;

import com.camilo.fitnorius.model.Banner;
import com.camilo.fitnorius.repository.BannerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

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

    // 📁 Directorio donde se guardarán las imágenes
    private final String uploadDir = "uploads/banner/";

    // 🔹 Obtener el banner actual
    public Banner getCurrentBanner() {
        Optional<Banner> banner = bannerRepository.findAll().stream().findFirst();
        return banner.orElse(new Banner("/uploads/banner/default-banner.png"));
    }

    // 🔹 Guardar o actualizar el banner
    public Banner saveBanner(MultipartFile file) {
        try {
            // Crear el directorio si no existe
            File directory = new File(uploadDir);
            if (!directory.exists()) directory.mkdirs();

            // Nombre único para evitar conflictos
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(uploadDir + fileName);
            Files.write(filePath, file.getBytes());

            // ✅ Generar URL relativa (para que React no cambie nada)
            String imageUrl = "/uploads/banner/" + fileName;

            // También podrías generar URL absoluta:
            // String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().toUriString();
            // String imageUrl = baseUrl + "/uploads/banner/" + fileName;

            // Actualizar o crear el banner
            Banner banner = getCurrentBanner();
            banner.setImageUrl(imageUrl);
            bannerRepository.save(banner);

            return banner;
        } catch (IOException e) {
            throw new RuntimeException("❌ Error al guardar el banner", e);
        }
    }

    // 🔹 Restablecer (borra el registro en la base de datos)
    public void resetBanner() {
        bannerRepository.deleteAll();
    }
}
