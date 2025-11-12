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
@CrossOrigin(origins = {
        "http://localhost:3000",
        "http://localhost:5173",
        "https://fitnorius-gym.vercel.app",
        "https://fitnorius-gym-git-main-juan-ks-projects-b6132ea5.vercel.app",
        "https://fitnorius-aghr9tnpz-juan-ks-projects-b6132ea5.vercel.app"
})
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    // ✅ Obtener todas las categorías
    @GetMapping
    public List<Category> getAllCategories() {
        return categoryService.getAllCategories();
    }

    // ✅ Crear nueva categoría con imagen (manejo seguro con @RequestPart)
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<Category> createCategory(
            @RequestPart("name") String name,
            @RequestPart(value = "image", required = false) MultipartFile imageFile
    ) throws IOException {

        System.out.println("📩 Petición recibida en createCategory");
        System.out.println("🧾 Nombre recibido: " + name);
        System.out.println("📸 Archivo recibido: " + (imageFile != null ? imageFile.getOriginalFilename() : "NULO"));

        Category savedCategory = categoryService.createCategory(name, imageFile);

        System.out.println("✅ Categoría creada con ID: " + savedCategory.getId());
        System.out.println("🌐 URL de imagen guardada: " + savedCategory.getImageUrl());

        return ResponseEntity.ok(savedCategory);
    }

    // ✅ Actualizar categoría (nombre e imagen opcional)
    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<Category> updateCategory(
            @PathVariable Long id,
            @RequestPart("name") String name,
            @RequestPart(value = "image", required = false) MultipartFile imageFile
    ) throws IOException {

        System.out.println("✏️ Actualizando categoría ID: " + id);
        Category updatedCategory = categoryService.updateCategory(id, name, imageFile);
        return ResponseEntity.ok(updatedCategory);
    }

    // ✅ Eliminar categoría junto con sus productos asociados
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCategory(@PathVariable Long id) {
        try {
            boolean deleted = categoryService.deleteCategoryWithProducts(id);
            if (deleted) {
                System.out.println("🗑️ Categoría eliminada correctamente (ID: " + id + ")");
                return ResponseEntity.ok("Categoría y productos eliminados correctamente.");
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.err.println("⚠️ Error eliminando categoría: " + e.getMessage());
            return ResponseEntity.status(500).body("Error al eliminar la categoría: " + e.getMessage());
        }
    }
}
