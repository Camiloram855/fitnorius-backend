package com.camilo.fitnorius.service;

import com.camilo.fitnorius.model.Category;
import com.camilo.fitnorius.repository.CategoryRepository;
import com.camilo.fitnorius.repository.ProductRepository;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final Cloudinary cloudinary;

    // 🔧 Constructor con configuración de Cloudinary
    public CategoryService(
            CategoryRepository categoryRepository,
            ProductRepository productRepository,
            @Value("${cloudinary.cloud_name}") String cloudName,
            @Value("${cloudinary.api_key}") String apiKey,
            @Value("${cloudinary.api_secret}") String apiSecret
    ) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret
        ));
    }

    /**
     * 📦 Obtener todas las categorías
     */
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    /**
     * 🆕 Crear una nueva categoría con imagen subida a Cloudinary
     */
    public Category createCategory(String name, MultipartFile imageFile) throws IOException {
        Category category = new Category();
        category.setName(name);

        if (imageFile != null && !imageFile.isEmpty()) {
            Map uploadResult = cloudinary.uploader().upload(imageFile.getBytes(), ObjectUtils.asMap(
                    "folder", "fitnorius/categories/"
            ));

            // ✅ Guardar secure_url y public_id correctamente
            category.setCloudinaryData(
                    uploadResult.get("secure_url").toString(),
                    uploadResult.get("public_id").toString()
            );
        }

        return categoryRepository.save(category);
    }

    /**
     * 🔄 Actualiza nombre e imagen (si se envía una nueva)
     */
    @Transactional
    public Category updateCategory(Long id, String name, MultipartFile imageFile) throws IOException {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("❌ Categoría no encontrada con ID: " + id));

        category.setName(name);

        // 🖼️ Si llega nueva imagen, eliminar la anterior de Cloudinary
        if (imageFile != null && !imageFile.isEmpty()) {
            deleteCategoryImage(category);

            Map uploadResult = cloudinary.uploader().upload(imageFile.getBytes(), ObjectUtils.asMap(
                    "folder", "fitnorius/categories/"
            ));

            category.setCloudinaryData(
                    uploadResult.get("secure_url").toString(),
                    uploadResult.get("public_id").toString()
            );
        }

        return categoryRepository.save(category);
    }

    /**
     * ❌ Eliminar categoría solo si no tiene productos asociados
     */
    @Transactional
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
     * 🧹 Eliminar categoría junto con sus productos
     */
    @Transactional
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
     * 🗑️ Eliminar imagen de Cloudinary si existe
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
