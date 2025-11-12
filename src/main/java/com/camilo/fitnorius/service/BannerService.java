package com.camilo.fitnorius.service;

import com.camilo.fitnorius.model.Banner;
import com.camilo.fitnorius.repository.BannerRepository;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@Service
public class BannerService {

    @Autowired
    private BannerRepository bannerRepository;

    // 🔹 Variables desde application.properties
    @Value("${cloudinary.cloud_name}")
    private String cloudName;

    @Value("${cloudinary.api_key}")
    private String apiKey;

    @Value("${cloudinary.api_secret}")
    private String apiSecret;

    // 🔹 Obtener el banner actual
    public Banner getCurrentBanner() {
        Optional<Banner> banner = bannerRepository.findAll().stream().findFirst();
        return banner.orElse(new Banner("https://res.cloudinary.com/" + cloudName + "/image/upload/v1720000000/default-banner.png"));
    }

    // 🔹 Subir el banner a Cloudinary
    public Banner saveBanner(MultipartFile file) {
        try {
            // Crear instancia de Cloudinary
            Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
                    "cloud_name", cloudName,
                    "api_key", apiKey,
                    "api_secret", apiSecret
            ));

            // 📤 Subir imagen
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", "fitnorius/banner/"
            ));

            String imageUrl = uploadResult.get("secure_url").toString();

            // Guardar o actualizar el banner en la base de datos
            Banner banner = getCurrentBanner();
            banner.setImageUrl(imageUrl);
            bannerRepository.save(banner);

            return banner;
        } catch (IOException e) {
            throw new RuntimeException("❌ Error al subir el banner a Cloudinary", e);
        }
    }

    // 🔹 Restablecer el banner
    public void resetBanner() {
        bannerRepository.deleteAll();
    }
}
