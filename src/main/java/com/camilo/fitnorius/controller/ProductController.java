package com.camilo.fitnorius.controller;

import com.camilo.fitnorius.dto.ProductDTO;
import com.camilo.fitnorius.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class ProductController {

    private final ProductService productService;

    /**
     * Crear producto con multipart (JSON + Imagen en el mismo formData)
     * 👉 Ruta: POST /api/products/upload
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductDTO> createProductMultipart(
            @RequestPart("product") ProductDTO request,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) throws IOException {
        return ResponseEntity.ok(productService.saveProduct(request, image));
    }

    /**
     * Crear producto solo con JSON (sin imagen)
     * 👉 Ruta: POST /api/products/json
     */
    @PostMapping(value = "/json", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductDTO> createProductJson(@RequestBody ProductDTO productDTO) {
        return ResponseEntity.ok(productService.saveProduct(productDTO, null));
    }

    /**
     * Listar todos los productos
     */
    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    /**
     * Listar productos por categoría
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ProductDTO>> getProductsByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(productService.getProductsByCategory(categoryId));
    }

    /**
     * Obtener un producto por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    /**
     * Actualizar producto con multipart (JSON + Imagen opcional)
     * 👉 Ruta: PUT /api/products/{id}/upload
     */
    @PutMapping(value = "/{id}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductDTO> updateProductMultipart(
            @PathVariable Long id,
            @RequestPart("product") ProductDTO request,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        return ResponseEntity.ok(productService.updateProduct(id, request, image));
    }

    /**
     * Actualizar producto solo con JSON (sin imagen)
     * 👉 Ruta: PUT /api/products/{id}
     */
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductDTO> updateProductJson(
            @PathVariable Long id,
            @RequestBody ProductDTO request
    ) {
        return ResponseEntity.ok(productService.updateProduct(id, request, null));
    }

    /**
     * 🗑️ Eliminar un producto por ID (ahora devuelve JSON en lugar de 204 vacío)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteProduct(@PathVariable Long id) {
        boolean deleted = productService.deleteProduct(id);

        Map<String, String> response = new HashMap<>();
        if (deleted) {
            response.put("message", "Producto eliminado correctamente");
            return ResponseEntity.ok(response);
        } else {
            response.put("error", "Producto no encontrado");
            return ResponseEntity.status(404).body(response);
        }
    }
}
