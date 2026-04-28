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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class BannerService {

    @Autowired
    private BannerRepository bannerRepository;

    @Value("${cloudinary.cloud_name}")
    private String cloudName;

    @Value("${cloudinary.api_key}")
    private String apiKey;

    @Value("${cloudinary.api_secret}")
    private String apiSecret;

    // Compatibilidad con cliente existente
    public Banner getCurrentBanner() {
        Optional<Banner> banner = bannerRepository.findAll().stream().findFirst();
        return banner.orElse(new Banner("https://res.cloudinary.com/" + cloudName + "/image/upload/v1720000000/default-banner.png"));
    }

    // Lista completa para carrusel
    public List<Banner> getAllBanners() {
        List<Banner> banners = bannerRepository.findAll();
        if (banners.isEmpty()) {
            return Collections.singletonList(
                    new Banner("https://res.cloudinary.com/" + cloudName + "/image/upload/v1720000000/default-banner.png")
            );
        }
        return banners;
    }

    // Sube una nueva imagen a Cloudinary y la guarda como slide
    public Banner saveBanner(MultipartFile file) {
        try {
            Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
                    "cloud_name", cloudName,
                    "api_key", apiKey,
                    "api_secret", apiSecret
            ));

            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", "fitnorius/banner/",
                    "transformation", "c_fit,w_1920,h_720"
            ));

            String imageUrl = uploadResult.get("secure_url").toString();

            Banner banner = new Banner();
            banner.setImageUrl(imageUrl);
            bannerRepository.save(banner);

            return banner;
        } catch (IOException e) {
            throw new RuntimeException("Error al subir el banner a Cloudinary", e);
        }
    }

    public void resetBanner() {
        bannerRepository.deleteAll();
    }
}
