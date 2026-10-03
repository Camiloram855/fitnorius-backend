package com.camilo.fitnorius.service;

import com.camilo.fitnorius.exception.ImageStorageException;
import com.camilo.fitnorius.model.Category;
import com.camilo.fitnorius.repository.CategoryRepository;
import com.camilo.fitnorius.repository.ProductRepository;
import com.camilo.fitnorius.security.ImageUploadValidator;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final Cloudinary cloudinary;

    /**
     * Obtener todas las categori­as (CACHEADO)
     */
    @Cacheable(value = "categories")
    public List<Category> getAllCategories() {
        System.out.println("Categorías desde BD");
        return categoryRepository.findAll();
    }

    /**
     * Crear categori­a con subida a Cloudinary
     * Limpia cache
     */
    @CacheEvict(value = "categories", allEntries = true)
    public Category createCategory(String name, MultipartFile imageFile) throws IOException {
        Category category = new Category();
        category.setName(name);

        if (imageFile != null && !imageFile.isEmpty()) {
            ImageUploadValidator.validate(imageFile);
            Map uploadResult;
            try {
                uploadResult = cloudinary.uploader().upload(
                        imageFile.getBytes(),
                        ObjectUtils.asMap(
                                "folder", "fitnorius/categories",
                                "resource_type", "image"
                        )
                );
            } catch (IOException | RuntimeException exception) {
                // El mensaje de Cloudinary (por ejemplo "cloud_name mismatch")
                // vive en la causa raiz. Se registra aqui porque es el unico
                // punto donde se conserva y explica el motivo real del fallo.
                log.error("Cloudinary rechazó la imagen de la categoría '{}': {}",
                        name, exception.getMessage());
                throw new ImageStorageException(
                        "No se pudo subir la imagen de la categoría", exception);
            }

            String secureUrl = (String) uploadResult.get("secure_url");
            String publicId = (String) uploadResult.get("public_id");

            category.setCloudinaryData(secureUrl, publicId);
        }

        return categoryRepository.save(category);
    }

    /**
     * Actualizar categori­a
     * Limpia cache
     */
    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public Category updateCategory(Long id, String name, MultipartFile imageFile) throws IOException {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + id));

        category.setName(name);

        if (imageFile != null && !imageFile.isEmpty()) {
            ImageUploadValidator.validate(imageFile);
            deleteCategoryImage(category);

            Map uploadResult = cloudinary.uploader().upload(
                    imageFile.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "fitnorius/categories",
                            "resource_type", "image"
                    )
            );

            String secureUrl = (String) uploadResult.get("secure_url");
            String publicId = (String) uploadResult.get("public_id");

            category.setCloudinaryData(secureUrl, publicId);
        }

        return categoryRepository.save(category);
    }

    /**
     * Eliminar categori­a
     *  Limpia cache
     */
    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public boolean deleteCategory(Long id) {
        Optional<Category> categoryOpt = categoryRepository.findById(id);
        if (categoryOpt.isEmpty()) return false;

        Category category = categoryOpt.get();

        if (!productRepository.findByCategoryIdOrderByDisplayOrderAscIdAsc(id).isEmpty()) {
            throw new IllegalStateException("No se puede eliminar la categori­a porque tiene productos asociados.");
        }

        deleteCategoryImage(category);
        categoryRepository.delete(category);
        return true;
    }

    /**
     * Eliminar categori­a junto con productos
     *  Limpia cache
     */
    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public boolean deleteCategoryWithProducts(Long id) {
        Optional<Category> categoryOpt = categoryRepository.findById(id);
        if (categoryOpt.isEmpty()) return false;

        Category category = categoryOpt.get();
        productRepository.deleteByCategoryId(id);
        deleteCategoryImage(category);
        categoryRepository.delete(category);
        return true;
    }

    /**
     * Eliminar imagen de Cloudinary
     */
    private void deleteCategoryImage(Category category) {
        try {
            if (category.getCloudinaryPublicId() != null) {
                cloudinary.uploader().destroy(category.getCloudinaryPublicId(), ObjectUtils.emptyMap());
                System.out.println("Imagen eliminada de Cloudinary: " + category.getCloudinaryPublicId());
            }
        } catch (Exception e) {
            System.err.println("Error eliminando imagen de Cloudinary: " + e.getMessage());
        }
    }
}

