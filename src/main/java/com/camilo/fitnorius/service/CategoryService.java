package com.camilo.fitnorius.service;

import com.camilo.fitnorius.model.Category;
import com.camilo.fitnorius.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    private final String uploadDir = "uploads/categories/";

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Category createCategory(String name, MultipartFile imageFile) throws IOException {
        Category category = new Category();
        category.setName(name);

        if (imageFile != null && !imageFile.isEmpty()) {
            // Guardar la imagen en una carpeta local
            String fileName = System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
            Path uploadPath = Paths.get(uploadDir);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(fileName);
            Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Guardar solo la ruta relativa en la DB
            category.setImage("/" + uploadDir + fileName);
        }

        return categoryRepository.save(category);
    }

    public boolean deleteCategory(Long id) {
        Optional<Category> categoryOpt = categoryRepository.findById(id);
        if (categoryOpt.isPresent()) {
            Category category = categoryOpt.get();

            // Borrar imagen del disco si existe
            if (category.getImage() != null) {
                Path imagePath = Paths.get(category.getImage().replaceFirst("/", ""));
                try {
                    Files.deleteIfExists(imagePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            categoryRepository.deleteById(id);
            return true;
        }
        return false;
    }
}