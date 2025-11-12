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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    // ✅ Buscar imágenes por producto
    public List<Image> findByProductId(Long productId) {
        return imageRepository.findByProductId(productId);
    }

    // ✅ Buscar imágenes por categoría
    public List<Image> findByCategoryId(Long categoryId) {
        return imageRepository.findByCategoryId(categoryId);
    }

    // ✅ Subir imágenes a Cloudinary (de producto o categoría)
    @Transactional
    public List<Image> saveImages(List<MultipartFile> files, Long productId, Long categoryId) {
        Product product = null;
        Category category = null;

        if (productId != null) {
            product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("❌ Producto no encontrado con ID: " + productId));
        }

        if (categoryId != null) {
            category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new RuntimeException("❌ Categoría no encontrada con ID: " + categoryId));
        }

        Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret
        ));

        List<Image> savedImages = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                String folder = (product != null)
                        ? "fitnorius/products/gallery/"
                        : "fitnorius/categories/gallery/";

                Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap("folder", folder));

                String imageUrl = uploadResult.get("secure_url").toString();
                String publicId = uploadResult.get("public_id").toString();

                Image image = Image.builder()
                        .url(imageUrl)
                        .publicId(publicId)
                        .product(product)
                        .category(category)
                        .build();

                savedImages.add(imageRepository.saveAndFlush(image));

                System.out.println("📸 Imagen subida y guardada: " + imageUrl);

            } catch (IOException e) {
                throw new RuntimeException("❌ Error al subir imagen a Cloudinary", e);
            }
        }

        return savedImages;
    }

    // ✅ Versión auxiliar (compatibilidad con controladores antiguos)
    // 🔹 Permite subir imágenes solo con productId, sin categoría
    @Transactional
    public List<Image> saveImages(List<MultipartFile> files, Long productId) {
        return saveImages(files, productId, null);
    }

    // ✅ Eliminar imagen tanto en Cloudinary como en BD
    @Transactional
    public boolean deleteImage(Long id) {
        System.out.println("🗑️ Intentando eliminar imagen con ID: " + id);

        return imageRepository.findById(id).map(img -> {
            try {
                Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
                        "cloud_name", cloudName,
                        "api_key", apiKey,
                        "api_secret", apiSecret
                ));

                // 🔥 Eliminar en Cloudinary
                if (img.getPublicId() != null && !img.getPublicId().isEmpty()) {
                    Map result = cloudinary.uploader().destroy(img.getPublicId(), ObjectUtils.emptyMap());
                    System.out.println("✅ Imagen eliminada de Cloudinary: " + result);
                } else {
                    System.out.println("⚠️ Imagen sin public_id en BD");
                }

                // 🔥 Eliminar de la BD
                imageRepository.delete(img);
                imageRepository.flush();

                System.out.println("✅ Imagen eliminada de BD (ID: " + id + ")");
                return true;

            } catch (Exception e) {
                throw new RuntimeException("❌ Error eliminando imagen ID " + id + ": " + e.getMessage(), e);
            }
        }).orElseGet(() -> {
            System.err.println("⚠️ Imagen no encontrada con ID: " + id);
            return false;
        });
    }
}
