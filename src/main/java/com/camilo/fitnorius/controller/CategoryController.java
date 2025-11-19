package com.camilo.fitnorius.controller;

import com.camilo.fitnorius.model.Category;
import com.camilo.fitnorius.repository.CategoryRepository;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryRepository categoryRepository;
    private final Cloudinary cloudinary;

    // 📦 Obtener todas las categorías
    @GetMapping
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    // 📤 Crear una nueva categoría con imagen
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<?> createCategory(
            @RequestParam("name") String name,
            @RequestParam("image") MultipartFile image
    ) {
        try {
            // 🖼️ Subir imagen a Cloudinary
            Map uploadResult = cloudinary.uploader().upload(
                    image.getBytes(),
                    ObjectUtils.asMap("folder", "fitnorius/categories")
            );

            // 🧱 Crear categoría con URL de la imagen
            Category category = new Category();
            category.setName(name);
            category.setImageUrl(uploadResult.get("secure_url").toString());
            category.setCloudinaryPublicId(uploadResult.get("public_id").toString());

            Category savedCategory = categoryRepository.save(category);

            return ResponseEntity.ok(savedCategory);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body("Error al subir imagen o guardar categoría ❌");
        }
    }

    // 🗑️ Eliminar categoría y su imagen de Cloudinary
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        return categoryRepository.findById(id)
                .map(category -> {
                    try {
                        if (category.getCloudinaryPublicId() != null) {
                            cloudinary.uploader().destroy(category.getCloudinaryPublicId(), ObjectUtils.emptyMap());
                        }
                        categoryRepository.delete(category);
                        return ResponseEntity.ok("Categoría eliminada ✅");
                    } catch (IOException e) {
                        e.printStackTrace();
                        return ResponseEntity.internalServerError()
                                .body("Error al eliminar imagen de Cloudinary ❌");
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
