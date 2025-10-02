package com.camilo.fitnorius.controller;

import com.camilo.fitnorius.model.Category;
import com.camilo.fitnorius.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "http://localhost:5173")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    // Obtener todas las categorías
    @GetMapping
    public List<Category> getAllCategories() {
        return categoryService.getAllCategories();
    }

    // Crear nueva categoría con imagen
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<Category> createCategory(
            @RequestParam("name") String name,
            @RequestParam("image") MultipartFile imageFile) throws IOException {

        Category savedCategory = categoryService.createCategory(name, imageFile);
        return ResponseEntity.ok(savedCategory);
    }

    // Eliminar categoría y sus productos
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCategory(@PathVariable Long id) {
        try {
            boolean deleted = categoryService.deleteCategoryWithProducts(id);
            if (deleted) {
                return ResponseEntity.ok("Categoría y productos eliminados correctamente.");
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al eliminar la categoría: " + e.getMessage());
        }
    }
}
