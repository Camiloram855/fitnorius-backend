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

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class ProductController {

    private final ProductService productService;

    /**
     * Crear producto con multipart (JSON + Imagen en el mismo formData)
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductDTO> createProductMultipart(
            @RequestPart("product") ProductDTO request,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        try {
            return ResponseEntity.ok(productService.saveProduct(request, image));
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Alternativa: Crear producto usando @ModelAttribute
     * (soporta directamente form-data con campos simples)
     */
    @PostMapping("/form")
    public ResponseEntity<ProductDTO> createProductForm(
            @ModelAttribute ProductDTO productDTO,
            @RequestParam(value = "image", required = false) MultipartFile image
    ) throws IOException {
        return ResponseEntity.ok(productService.saveProduct(productDTO, image));
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
}
