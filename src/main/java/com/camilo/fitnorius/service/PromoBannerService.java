package com.camilo.fitnorius.service;

import com.camilo.fitnorius.model.PromoBanner;
import com.camilo.fitnorius.repository.PromoBannerRepository;
import com.camilo.fitnorius.security.ImageUploadValidator;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Map;

/**
 * Gestión del banner de promociones. Reutiliza el mismo almacenamiento de
 * Cloudinary y el mismo validador de imágenes que el resto del proyecto.
 */
@Service
public class PromoBannerService {

    private static final String FOLDER = "fitnorius/promo-banner/";
    private static final String DEFAULT_TITLE = "¡Ofertas especiales!";
    private static final int MAX_TITLE = 120;
    private static final int MAX_SUBTITLE = 255;

    private final PromoBannerRepository repository;
    private final Cloudinary cloudinary;

    public PromoBannerService(PromoBannerRepository repository, Cloudinary cloudinary) {
        this.repository = repository;
        this.cloudinary = cloudinary;
    }

    /** Banner vigente o null si el administrador aún no ha configurado ninguno. */
    @Transactional(readOnly = true)
    public PromoBanner getCurrentBanner() {
        return repository.findFirstByOrderByIdAsc().orElse(null);
    }

    @Transactional
    public PromoBanner saveBanner(MultipartFile file, String title, String subtitle, Boolean active) {
        PromoBanner banner = repository.findFirstByOrderByIdAsc().orElseGet(PromoBanner::new);

        banner.setTitle(normalizeTitle(title));
        banner.setSubtitle(normalizeSubtitle(subtitle));
        if (active != null) {
            banner.setActive(active);
        }

        if (file != null && !file.isEmpty()) {
            ImageUploadValidator.validate(file);
            String previousPublicId = banner.getPublicId();
            try {
                Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                        "folder", FOLDER,
                        "transformation", "c_limit,w_1600,q_auto,f_auto"
                ));
                banner.setImageUrl(uploadResult.get("secure_url").toString());
                banner.setPublicId(uploadResult.get("public_id").toString());

                // El asset anterior se elimina para no dejar basura en Cloudinary.
                if (StringUtils.hasText(previousPublicId)) {
                    try {
                        cloudinary.uploader().destroy(previousPublicId, ObjectUtils.emptyMap());
                    } catch (IOException ignored) {
                        // No se interrumpe el guardado si falla la limpieza.
                    }
                }
            } catch (IOException exception) {
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR, "Error al subir la imagen del banner", exception);
            }
        }

        return repository.save(banner);
    }

    @Transactional
    public PromoBanner updateText(String title, String subtitle) {
        PromoBanner banner = repository.findFirstByOrderByIdAsc().orElse(null);
        if (banner == null) {
            // Se crea sin imagen para poder configurar el texto antes de subirla.
            banner = new PromoBanner();
            banner.setTitle(normalizeTitle(title));
            banner.setSubtitle(normalizeSubtitle(subtitle));
            return repository.save(banner);
        }

        banner.setTitle(normalizeTitle(title));
        banner.setSubtitle(normalizeSubtitle(subtitle));
        return repository.save(banner);
    }

    @Transactional
    public PromoBanner updateVisibility(boolean active) {
        PromoBanner banner = repository.findFirstByOrderByIdAsc().orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No hay un banner de promociones configurado"));
        banner.setActive(active);
        return repository.save(banner);
    }

    @Transactional
    public void deleteBanner() {
        repository.findFirstByOrderByIdAsc().ifPresent(banner -> {
            if (StringUtils.hasText(banner.getPublicId())) {
                try {
                    cloudinary.uploader().destroy(banner.getPublicId(), ObjectUtils.emptyMap());
                } catch (IOException exception) {
                    throw new ResponseStatusException(
                            HttpStatus.INTERNAL_SERVER_ERROR, "Error al eliminar la imagen del banner", exception);
                }
            }
            repository.delete(banner);
        });
    }

    private static String normalizeTitle(String title) {
        if (!StringUtils.hasText(title)) {
            return DEFAULT_TITLE;
        }
        String trimmed = title.trim();
        if (trimmed.length() > MAX_TITLE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El título es demasiado largo");
        }
        return trimmed;
    }

    private static String normalizeSubtitle(String subtitle) {
        if (!StringUtils.hasText(subtitle)) {
            return null;
        }
        String trimmed = subtitle.trim();
        if (trimmed.length() > MAX_SUBTITLE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El texto del banner es demasiado largo");
        }
        return trimmed;
    }
}