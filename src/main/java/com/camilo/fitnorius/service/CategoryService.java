package com.camilo.fitnorius.service;

import com.camilo.fitnorius.model.Category;
import com.camilo.fitnorius.repository.CategoryRepository;
import com.camilo.fitnorius.repository.ProductRepository;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    private final Cloudinary cloudinary;

    // 🔧 Constructor con Cloudinary configurado desde variables de entorno
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

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    /**
     * 🆕 Crea una categoría con nombre e imagen (subida a Cloudinary)
     */
    public Category createCategory(String name, MultipartFile imageFile) throws IOException {
        Category category = new Category();
        category.setName(name);

        if (imageFile != null && !imageFile.isEmpty()) {
            Map uploadResult = cloudinary.uploader().upload(imageFile.getBytes(), ObjectUtils.asMap(
                    "folder", "fitnorius/categories/"
            ));

            category.setImageUrl(uploadResult.get("secure_url").toString());
            category.setCloudinaryPublicId(uploadResult.get("public_id").toString());
        }

        return categoryRepository.save(category);
    }

    /**
     * 🔄 Actualiza una categoría (nombre y opcionalmente imagen en Cloudinary)
     */
    @Transactional
    public Category updateCategory(Long id, String name, MultipartFile imageFile) throws IOException {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        category.setName(name);

        if (imageFile != null && !imageFile.isEmpty()) {
            // 🗑️ Eliminar la imagen anterior de Cloudinary
            deleteCategoryImage(category);

            // 📤 Subir la nueva imagen
            Map uploadResult = cloudinary.uploader().upload(imageFile.getBytes(), ObjectUtils.asMap(
                    "folder", "fitnorius/categories/"
            ));

            category.setImageUrl(uploadResult.get("secure_url").toString());
            category.setCloudinaryPublicId(uploadResult.get("public_id").toString());
        }

        return categoryRepository.save(category);
    }

    /**
     * ❌ Elimina una categoría (si no tiene productos)
     */
    @Transactional
    public boolean deleteCategory(Long id) {
        Optional<Category> categoryOpt = categoryRepository.findById(id);
        if (categoryOpt.isPresent()) {
            Category category = categoryOpt.get();

            if (!productRepository.findByCategoryId(id).isEmpty()) {
                throw new IllegalStateException("No se puede eliminar la categoría porque tiene productos asociados.");
            }

            deleteCategoryImage(category);
            categoryRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * ❌ Elimina la categoría junto con sus productos asociados
     */
    @Transactional
    public boolean deleteCategoryWithProducts(Long id) {
        Optional<Category> categoryOpt = categoryRepository.findById(id);
        if (categoryOpt.isPresent()) {
            Category category = categoryOpt.get();

            productRepository.deleteByCategoryId(id);
            deleteCategoryImage(category);
            categoryRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * 🧹 Elimina la imagen asociada en Cloudinary
     */
    private void deleteCategoryImage(Category category) {
        try {
            if (category.getCloudinaryPublicId() != null) {
                cloudinary.uploader().destroy(category.getCloudinaryPublicId(), ObjectUtils.emptyMap());
                System.out.println("🗑️ Imagen eliminada de Cloudinary: " + category.getCloudinaryPublicId());
            }
        } catch (Exception e) {
            System.err.println("⚠️ Error eliminando imagen en Cloudinary: " + e.getMessage());
        }
    }
}
