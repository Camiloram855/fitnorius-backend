package com.camilo.fitnorius.service;

import com.camilo.fitnorius.model.PromotionPopup;
import com.camilo.fitnorius.repository.PromotionPopupRepository;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@Service
public class PromotionPopupService {

    @Autowired
    private PromotionPopupRepository repository;

    @Value("${cloudinary.cloud_name}")
    private String cloudName;

    @Value("${cloudinary.api_key}")
    private String apiKey;

    @Value("${cloudinary.api_secret}")
    private String apiSecret;

    public PromotionPopup getCurrentPopup() {
        return repository.findFirstByOrderByIdAsc().orElse(null);
    }

    @Transactional
    public PromotionPopup savePopup(MultipartFile file, boolean active) {
        try {
            Optional<PromotionPopup> existingOptional = repository.findFirstByOrderByIdAsc();
            PromotionPopup popup = existingOptional.orElseGet(PromotionPopup::new);

            if ((file == null || file.isEmpty()) && (popup.getImageUrl() == null || popup.getImageUrl().isBlank())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debes subir una imagen para crear el popup promocional");
            }

            if (file != null && !file.isEmpty()) {
                String previousPublicId = popup.getPublicId();

                Cloudinary cloudinary = buildCloudinary();
                Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                        "folder", "fitnorius/promotion-popup/",
                        "transformation", "c_limit,w_1600,q_auto,f_auto"
                ));

                popup.setImageUrl(uploadResult.get("secure_url").toString());
                popup.setPublicId(uploadResult.get("public_id").toString());

                if (previousPublicId != null && !previousPublicId.isBlank()) {
                    try {
                        deleteFromCloudinary(previousPublicId);
                    } catch (IOException ignored) {
                        // No interrumpimos el guardado si falla la limpieza del archivo anterior.
                    }
                }
            }

            popup.setActive(active);
            return repository.save(popup);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al guardar el popup promocional", e);
        }
    }

    @Transactional
    public PromotionPopup updateVisibility(boolean active) {
        PromotionPopup popup = repository.findFirstByOrderByIdAsc().orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe un popup promocional para actualizar")
        );
        popup.setActive(active);
        return repository.save(popup);
    }

    @Transactional
    public void deletePopup() {
        repository.findFirstByOrderByIdAsc().ifPresent(popup -> {
            if (popup.getPublicId() != null && !popup.getPublicId().isBlank()) {
                try {
                    deleteFromCloudinary(popup.getPublicId());
                } catch (IOException e) {
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al eliminar la imagen del popup", e);
                }
            }
            repository.delete(popup);
        });
    }

    private void deleteFromCloudinary(String publicId) throws IOException {
        Cloudinary cloudinary = buildCloudinary();
        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    }

    private Cloudinary buildCloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret
        ));
    }
}
