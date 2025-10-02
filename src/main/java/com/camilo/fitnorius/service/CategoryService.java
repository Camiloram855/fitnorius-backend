package com.camilo.fitnorius.service;

import com.camilo.fitnorius.model.Category;
import com.camilo.fitnorius.repository.CategoryRepository;
import com.camilo.fitnorius.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    private final String uploadDir = "uploads/categories/";

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Category createCategory(String name, MultipartFile imageFile) throws IOException {
        Category category = new Category();
        category.setName(name);

        if (imageFile != null && !imageFile.isEmpty()) {
            String fileName = System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
            Path uploadPath = Paths.get(uploadDir);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(fileName);
            Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            category.setImage("/" + uploadDir + fileName);
        }

        return categoryRepository.save(category);
    }

    /**
     * Elimina solo la categoría (si no tiene productos asociados).
     */
    @Transactional
    public boolean deleteCategory(Long id) {
        Optional<Category> categoryOpt = categoryRepository.findById(id);
        if (categoryOpt.isPresent()) {
            Category category = categoryOpt.get();

            // Verificar si la categoría tiene productos antes de borrar
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
     * Elimina la categoría y todos sus productos asociados.
     */
    @Transactional
    public boolean deleteCategoryWithProducts(Long id) {
        Optional<Category> categoryOpt = categoryRepository.findById(id);
        if (categoryOpt.isPresent()) {
            Category category = categoryOpt.get();

            // Eliminar los productos asociados primero
            productRepository.deleteByCategoryId(id);

            // Borrar imagen si existe
            deleteCategoryImage(category);

            // Finalmente eliminar la categoría
            categoryRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * Método auxiliar para borrar la imagen asociada en disco.
     */
    private void deleteCategoryImage(Category category) {
        if (category.getImage() != null) {
            Path imagePath = Paths.get(category.getImage().replaceFirst("/", ""));
            try {
                Files.deleteIfExists(imagePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
