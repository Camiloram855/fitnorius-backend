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
import java.nio.file.*;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    private static final String UPLOAD_DIR = "uploads/products/";

    public ProductDTO saveProduct(ProductDTO request, MultipartFile image) throws IOException {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + request.getCategoryId()));

        Product product = Product.builder()
                .name(request.getName())
                .price(request.getPrice())
                .oldPrice(request.getOldPrice())
                .discount(request.getDiscount())
                .category(category)
                .build();

        if (image != null && !image.isEmpty()) {
            Files.createDirectories(Paths.get(UPLOAD_DIR));
            String fileName = System.currentTimeMillis() + "_" + image.getOriginalFilename();
            Path filePath = Paths.get(UPLOAD_DIR, fileName);
            Files.write(filePath, image.getBytes());
            product.setImageUrl("/" + UPLOAD_DIR + fileName);
        }

        Product saved = productRepository.save(product);
        return mapToDTO(saved);
    }

    public List<ProductDTO> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    private ProductDTO mapToDTO(Product product) {
        return ProductDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .oldPrice(product.getOldPrice())
                .discount(product.getDiscount())
                .imageUrl(product.getImageUrl())
                .categoryId(product.getCategory().getId())
                .categoryName(product.getCategory().getName())
                .build();
    }
}
