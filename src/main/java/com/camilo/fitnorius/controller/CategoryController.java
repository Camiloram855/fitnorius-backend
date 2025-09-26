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

    // Eliminar categoría
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        boolean deleted = categoryService.deleteCategory(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
