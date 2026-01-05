package com.camilo.fitnorius.service;

import com.camilo.fitnorius.model.Category;
import com.camilo.fitnorius.model.Image;
import com.camilo.fitnorius.model.Product;
import com.camilo.fitnorius.repository.ImageRepository;
import com.camilo.fitnorius.repository.ProductRepository;
import com.camilo.fitnorius.repository.CategoryRepository;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageRepository imageRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    // 🔹 Configuración de Cloudinary
    @Value("${cloudinary.cloud_name}")
    private String cloudName;

    @Value("${cloudinary.api_key}")
    private String apiKey;

    @Value("${cloudinary.api_secret}")
    private String apiSecret;

    // 🔹 Cloudinary lazy (una sola instancia)
    private Cloudinary cloudinary;

    private Cloudinary getCloudinary() {
        if (cloudinary == null) {
            cloudinary = new Cloudinary(ObjectUtils.asMap(
                    "cloud_name", cloudName,
                    "api_key", apiKey,
                    "api_secret", apiSecret
            ));
            log.info("Cloudinary inicializado correctamente");
        }
        return cloudinary;
    }

    // ✅ Buscar imágenes por producto
    public List<Image> findByProductId(Long productId) {
        return imageRepository.findByProductId(productId);
    }

    // ✅ Buscar imágenes por categoría
    public List<Image> findByCategoryId(Long categoryId) {
        return imageRepository.findByCategoryId(categoryId);
    }

    // ✅ Subir imágenes a Cloudinary (producto o categoría)
    @Transactional
    public List<Image> saveImages(List<MultipartFile> files, Long productId, Long categoryId) {

        Product product = null;
        Category category = null;

        if (productId != null) {
            product = productRepository.findById(productId)
                    .orElseThrow(() ->
                            new RuntimeException("Producto no encontrado con ID: " + productId)
                    );
        }

        if (categoryId != null) {
            category = categoryRepository.findById(categoryId)
                    .orElseThrow(() ->
                            new RuntimeException("Categoría no encontrada con ID: " + categoryId)
                    );
        }

        List<Image> savedImages = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                String folder = (product != null)
                        ? "fitnorius/products/gallery/"
                        : "fitnorius/categories/gallery/";

                Map uploadResult = getCloudinary()
                        .uploader()
                        .upload(file.getBytes(), ObjectUtils.asMap("folder", folder));

                String imageUrl = uploadResult.get("secure_url").toString();
                String publicId = uploadResult.get("public_id").toString();

                Image image = Image.builder()
                        .url(imageUrl)
                        .publicId(publicId)
                        .product(product)
                        .category(category)
                        .build();

                savedImages.add(imageRepository.saveAndFlush(image));

                log.debug("Imagen subida correctamente: {}", imageUrl);

            } catch (IOException e) {
                log.error("Error subiendo imagen a Cloudinary", e);
                throw new RuntimeException("Error al subir imagen a Cloudinary", e);
            }
        }

        return savedImages;
    }

    // ✅ Compatibilidad con controladores antiguos
    @Transactional
    public List<Image> saveImages(List<MultipartFile> files, Long productId) {
        return saveImages(files, productId, null);
    }

    // ✅ Eliminar imagen en Cloudinary y BD
    @Transactional
    public boolean deleteImage(Long id) {

        log.info("Intentando eliminar imagen con ID {}", id);

        return imageRepository.findById(id).map(img -> {
            try {
                if (img.getPublicId() != null && !img.getPublicId().isEmpty()) {
                    Map result = getCloudinary()
                            .uploader()
                            .destroy(img.getPublicId(), ObjectUtils.emptyMap());

                    log.info("Imagen eliminada de Cloudinary: {}", result);
                } else {
                    log.warn("Imagen sin publicId en BD (ID {})", id);
                }

                imageRepository.delete(img);
                imageRepository.flush();

                log.info("Imagen eliminada de BD (ID {})", id);
                return true;

            } catch (Exception e) {
                log.error("Error eliminando imagen con ID {}", id, e);
                throw new RuntimeException("Error eliminando imagen ID " + id, e);
            }
        }).orElseGet(() -> {
            log.warn("Imagen no encontrada con ID {}", id);
            return false;
        });
    }
}
