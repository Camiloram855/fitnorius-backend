package com.camilo.fitnorius.controller;

import com.camilo.fitnorius.dto.ProductDTO;
import com.camilo.fitnorius.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
     * Alternativa: Crear producto usando @ModelAttribute
     * 👉 Ruta: POST /api/products/form
     */
    @PostMapping("/form")
    public ResponseEntity<ProductDTO> createProductForm(
            @ModelAttribute ProductDTO productDTO,
            @RequestParam(value = "image", required = false) MultipartFile image
    ) throws IOException {
        return ResponseEntity.ok(productService.saveProduct(productDTO, image));
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
     * 👉 Ruta: GET /api/products
     */
    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    /**
     * Listar productos por categoría
     * 👉 Ruta: GET /api/products/category/{categoryId}
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ProductDTO>> getProductsByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(productService.getProductsByCategory(categoryId));
    }

    /**
     * Obtener un producto por ID
     * 👉 Ruta: GET /api/products/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    /**
     * 🔍 Buscar productos por nombre, descripción o categoría (GET)
     * 👉 Ruta: GET /api/products/search?query=texto
     */
    @GetMapping("/search")
    public ResponseEntity<List<ProductDTO>> searchProducts(@RequestParam("query") String query) {
        return ResponseEntity.ok(productService.searchProducts(query));
    }

    /**
     * 🔍 Buscar productos por nombre, descripción o categoría (POST)
     * 👉 Ruta: POST /api/products/search
     * Permite enviar { "query": "texto" } en el cuerpo JSON.
     */
    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ProductDTO>> searchProductsPost(@RequestBody Map<String, String> body) {
        String query = body.get("query");
        return ResponseEntity.ok(productService.searchProducts(query));
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
     * Eliminar un producto por ID
     * 👉 Ruta: DELETE /api/products/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        boolean deleted = productService.deleteProduct(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
