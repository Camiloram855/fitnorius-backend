package com.camilo.fitnorius.service;

import com.camilo.fitnorius.model.Category;
import com.camilo.fitnorius.repository.CategoryRepository;
import com.camilo.fitnorius.repository.ProductRepository;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
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
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final Cloudinary cloudinary;

    /**
     * 📦 Obtener todas las categorías (CACHEADO)
     */
    @Cacheable(value = "categories")
    public List<Category> getAllCategories() {
        System.out.println("📦 Categorías desde BD");
        return categoryRepository.findAll();
    }

    /**
     * 🆕 Crear categoría con subida a Cloudinary
     * 🧹 Limpia cache
     */
    @CacheEvict(value = "categories", allEntries = true)
    public Category createCategory(String name, MultipartFile imageFile) throws IOException {
        Category category = new Category();
        category.setName(name);

        if (imageFile != null && !imageFile.isEmpty()) {
            Map uploadResult = cloudinary.uploader().upload(
                    imageFile.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "fitnorius/categories",
                            "resource_type", "image"
                    )
            );

            System.out.println("📸 Resultado Cloudinary (Category): " + uploadResult);

            String secureUrl = (String) uploadResult.get("secure_url");
            String publicId = (String) uploadResult.get("public_id");

            category.setCloudinaryData(secureUrl, publicId);
        }

        return categoryRepository.save(category);
    }

    /**
     * 🔄 Actualizar categoría
     * 🧹 Limpia cache
     */
    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public Category updateCategory(Long id, String name, MultipartFile imageFile) throws IOException {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("❌ Categoría no encontrada con ID: " + id));

        category.setName(name);

        if (imageFile != null && !imageFile.isEmpty()) {
            deleteCategoryImage(category);

            Map uploadResult = cloudinary.uploader().upload(
                    imageFile.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "fitnorius/categories",
                            "resource_type", "image"
                    )
            );

            System.out.println("📸 Resultado Cloudinary (Update): " + uploadResult);

            String secureUrl = (String) uploadResult.get("secure_url");
            String publicId = (String) uploadResult.get("public_id");

            category.setCloudinaryData(secureUrl, publicId);
        }

        return categoryRepository.save(category);
    }

    /**
     * ❌ Eliminar categoría
     * 🧹 Limpia cache
     */
    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public boolean deleteCategory(Long id) {
        Optional<Category> categoryOpt = categoryRepository.findById(id);
        if (categoryOpt.isEmpty()) return false;

        Category category = categoryOpt.get();

        if (!productRepository.findByCategoryId(id).isEmpty()) {
            throw new IllegalStateException("⚠️ No se puede eliminar la categoría porque tiene productos asociados.");
        }

        deleteCategoryImage(category);
        categoryRepository.delete(category);
        return true;
    }

    /**
     * 🧹 Eliminar categoría junto con productos
     * 🧹 Limpia cache
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
     * 🗑️ Eliminar imagen de Cloudinary
     */
    private void deleteCategoryImage(Category category) {
        try {
            if (category.getCloudinaryPublicId() != null) {
                cloudinary.uploader().destroy(category.getCloudinaryPublicId(), ObjectUtils.emptyMap());
                System.out.println("🗑️ Imagen eliminada de Cloudinary: " + category.getCloudinaryPublicId());
            }
        } catch (Exception e) {
            System.err.println("⚠️ Error eliminando imagen de Cloudinary: " + e.getMessage());
        }
    }
}
