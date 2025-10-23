package com.camilo.fitnorius.service;

import com.camilo.fitnorius.dto.ProductDTO;
import com.camilo.fitnorius.model.Category;
import com.camilo.fitnorius.model.Product;
import com.camilo.fitnorius.repository.CategoryRepository;
import com.camilo.fitnorius.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.*;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    private static final String UPLOAD_DIR = "uploads/products/";

    // ✅ Crear producto
    public ProductDTO saveProduct(ProductDTO request, MultipartFile image) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + request.getCategoryId()));

        Product product = Product.builder()
                .name(request.getName())
                .price(toBigDecimal(request.getPrice()))
                .oldPrice(toBigDecimal(request.getOldPrice()))
                .discount(toBigDecimal(request.getDiscount()))
                .description(request.getDescription())
                .category(category)
                .build();

        if (image != null && !image.isEmpty()) {
            try {
                product.setImageUrl(saveImage(image));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return mapToDTO(productRepository.save(product));
    }

    // ✅ Actualizar producto
    public ProductDTO updateProduct(Long id, ProductDTO request, MultipartFile image) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));

        product.setName(request.getName());
        product.setPrice(toBigDecimal(request.getPrice()));
        product.setOldPrice(toBigDecimal(request.getOldPrice()));
        product.setDiscount(toBigDecimal(request.getDiscount()));
        product.setDescription(request.getDescription());

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + request.getCategoryId()));
            product.setCategory(category);
        }

        if (image != null && !image.isEmpty()) {
            try {
                String newImageUrl = saveImage(image);
                deleteOldImage(product.getImageUrl());
                product.setImageUrl(newImageUrl);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return mapToDTO(productRepository.save(product));
    }

    // ✅ Listar todos
    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll().stream().map(this::mapToDTO).toList();
    }

    // ✅ Listar por categoría
    public List<ProductDTO> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId).stream().map(this::mapToDTO).toList();
    }

    // ✅ Buscar por ID
    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));
        return mapToDTO(product);
    }

    // ✅ Eliminar
    public boolean deleteProduct(Long id) {
        Optional<Product> productOpt = productRepository.findById(id);
        if (productOpt.isPresent()) {
            deleteOldImage(productOpt.get().getImageUrl());
            productRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // ✅ Buscar por nombre o descripción
    public List<ProductDTO> searchProducts(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllProducts();
        }

        return productRepository
                .findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(query, query)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    // ✅ Guardar imagen
    private String saveImage(MultipartFile image) throws IOException {
        Files.createDirectories(Paths.get(UPLOAD_DIR));
        String fileName = System.currentTimeMillis() + "_" + Paths.get(image.getOriginalFilename()).getFileName();
        Path filePath = Paths.get(UPLOAD_DIR, fileName.toString());
        Files.write(filePath, image.getBytes(), StandardOpenOption.CREATE);
        return "/uploads/products/" + fileName;
    }

    // ✅ Eliminar imagen vieja
    private void deleteOldImage(String imageUrl) {
        if (imageUrl != null && imageUrl.startsWith("/uploads/")) {
            Path oldImagePath = Paths.get(imageUrl.replaceFirst("^/", ""));
            try {
                Files.deleteIfExists(oldImagePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // ✅ Mapper a DTO
    private ProductDTO mapToDTO(Product product) {
        return ProductDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .price(toDouble(product.getPrice()))
                .oldPrice(toDouble(product.getOldPrice()))
                .discount(toDouble(product.getDiscount()))
                .description(product.getDescription())
                .imageUrl(product.getImageUrl())
                .categoryId(product.getCategory().getId())
                .categoryName(product.getCategory().getName())
                .build();
    }

    // ✅ Conversión segura de Double a BigDecimal
    private BigDecimal toBigDecimal(Double value) {
        return value != null ? BigDecimal.valueOf(value).setScale(2, BigDecimal.ROUND_HALF_UP) : BigDecimal.ZERO;
    }

    // ✅ Conversión segura de BigDecimal a Double
    private Double toDouble(BigDecimal value) {
        return value != null ? value.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() : null;
    }
}
