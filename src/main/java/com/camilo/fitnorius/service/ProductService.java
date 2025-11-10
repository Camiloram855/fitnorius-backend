package com.camilo.fitnorius.service;

import com.camilo.fitnorius.dto.ProductDTO;
import com.camilo.fitnorius.model.Category;
import com.camilo.fitnorius.model.Product;
import com.camilo.fitnorius.model.ProductImage;
import com.camilo.fitnorius.repository.CategoryRepository;
import com.camilo.fitnorius.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    private static final String UPLOAD_DIR = "uploads/products/";

    // ✅ Crear producto con múltiples imágenes
    public ProductDTO saveProduct(ProductDTO request, List<MultipartFile> images) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + request.getCategoryId()));

        Product product = Product.builder()
                .name(request.getName())
                .price(request.getPrice())
                .oldPrice(request.getOldPrice())
                .discount(request.getDiscount())
                .description(request.getDescription())
                .category(category)
                .build();

        // Guardar imágenes si existen
        if (images != null && !images.isEmpty()) {
            List<ProductImage> productImages = images.stream()
                    .map(file -> {
                        try {
                            String url = saveImage(file);
                            return ProductImage.builder()
                                    .url(url)
                                    .product(product)
                                    .build();
                        } catch (IOException e) {
                            e.printStackTrace();
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .toList();

            product.setImages(productImages);
        }

        return mapToDTO(productRepository.save(product));
    }

    // ✅ Actualizar producto (ahora soporta múltiples imágenes)
    public ProductDTO updateProduct(Long id, ProductDTO request, List<MultipartFile> images) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));

        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setOldPrice(request.getOldPrice());
        product.setDiscount(request.getDiscount());
        product.setDescription(request.getDescription());

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + request.getCategoryId()));
            product.setCategory(category);
        }

        // 🔁 Reemplazar imágenes si se envían nuevas
        if (images != null && !images.isEmpty()) {
            deleteOldImages(product); // Borra las anteriores

            List<ProductImage> newImages = images.stream()
                    .map(file -> {
                        try {
                            String url = saveImage(file);
                            return ProductImage.builder()
                                    .url(url)
                                    .product(product)
                                    .build();
                        } catch (IOException e) {
                            e.printStackTrace();
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .toList();

            product.getImages().clear();
            product.getImages().addAll(newImages);
        }

        return mapToDTO(productRepository.save(product));
    }

    // ✅ Listar todos los productos
    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    // ✅ Listar productos por categoría
    public List<ProductDTO> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    // ✅ Buscar producto por ID
    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));
        return mapToDTO(product);
    }

    // ✅ Eliminar producto y sus imágenes
    public boolean deleteProduct(Long id) {
        Optional<Product> productOpt = productRepository.findById(id);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            deleteOldImages(product);
            productRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // ✅ Buscar productos por nombre o descripción
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

    // ✅ Guardar imagen en carpeta local
    private String saveImage(MultipartFile image) throws IOException {
        Files.createDirectories(Paths.get(UPLOAD_DIR));
        String fileName = System.currentTimeMillis() + "_" + Paths.get(image.getOriginalFilename()).getFileName();
        Path filePath = Paths.get(UPLOAD_DIR, fileName.toString());
        Files.write(filePath, image.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        return "/uploads/products/" + fileName;
    }

    // ✅ Eliminar todas las imágenes asociadas al producto
    private void deleteOldImages(Product product) {
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            product.getImages().forEach(img -> {
                if (img.getUrl() != null && img.getUrl().startsWith("/uploads/")) {
                    Path oldImagePath = Paths.get(img.getUrl().replaceFirst("^/", ""));
                    try {
                        Files.deleteIfExists(oldImagePath);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            product.getImages().clear();
        }
    }

    // ✅ Convertir modelo → DTO
    private ProductDTO mapToDTO(Product product) {
        return ProductDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .oldPrice(product.getOldPrice())
                .discount(product.getDiscount())
                .description(product.getDescription())
                .imageUrls(
                        product.getImages() != null
                                ? product.getImages().stream().map(ProductImage::getUrl).toList()
                                : List.of()
                )
                .categoryId(product.getCategory().getId())
                .categoryName(product.getCategory().getName())
                .build();
    }
}
