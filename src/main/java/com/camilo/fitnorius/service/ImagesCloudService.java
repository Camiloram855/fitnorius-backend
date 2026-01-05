package com.camilo.fitnorius.service;

import com.camilo.fitnorius.model.ImagesCloud;
import com.camilo.fitnorius.repository.ImagesCloudRepository;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImagesCloudService {

    private final Cloudinary cloudinary;
    private final ImagesCloudRepository repository;

    // ✅ Subir imagen a Cloudinary
    public ImagesCloud upload(MultipartFile file) throws IOException {

        try {
            Map uploadResult = cloudinary.uploader()
                    .upload(file.getBytes(), ObjectUtils.emptyMap());

            ImagesCloud image = ImagesCloud.builder()
                    .publicId((String) uploadResult.get("public_id"))
                    .secureUrl((String) uploadResult.get("secure_url"))
                    .format((String) uploadResult.get("format"))
                    .width((Integer) uploadResult.get("width"))
                    .height((Integer) uploadResult.get("height"))
                    .bytes(Long.parseLong(uploadResult.get("bytes").toString()))
                    .createdAt((String) uploadResult.get("created_at"))
                    .build();

            log.debug("Imagen subida a Cloudinary: {}", image.getSecureUrl());

            return repository.save(image);

        } catch (IOException e) {
            log.error("Error subiendo imagen a Cloudinary", e);
            throw e; // ⚠️ se mantiene el contrato del método
        }
    }

    // ✅ Eliminar imagen
    public void delete(String publicId) throws IOException {

        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            repository.findByPublicId(publicId).ifPresent(repository::delete);

            log.info("Imagen eliminada correctamente: {}", publicId);

        } catch (IOException e) {
            log.error("Error eliminando imagen con publicId {}", publicId, e);
            throw e;
        }
    }

    // ✅ Listar imágenes
    public List<ImagesCloud> findAll() {
        return repository.findAll();
    }
}
