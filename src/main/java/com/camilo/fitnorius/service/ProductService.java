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
                .price(safeBigDecimal(request.getPrice()))
                .oldPrice(safeBigDecimal(request.getOldPrice()))
                .discount(safeBigDecimal(request.getDiscount()))
                .description(request.getDescription())
                .category(category)
                .build();

        // Guardar imagen si existe
        if (image != null && !image.isEmpty()) {
            try {
                product.setImageUrl(saveImage(image));
            } catch (IOException e) {
                e.printStackTrace(); // Log del error sin romper la app
            }
        }

        return mapToDTO(productRepository.save(product));
    }

    // ✅ Actualizar producto existente
    public ProductDTO updateProduct(Long id, ProductDTO request, MultipartFile image) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));

        // Actualizar campos básicos
        product.setName(request.getName());
        product.setPrice(safeBigDecimal(request.getPrice()));
        product.setOldPrice(safeBigDecimal(request.getOldPrice()));
        product.setDiscount(safeBigDecimal(request.getDiscount()));
        product.setDescription(request.getDescription());

        // Actualizar categoría si es necesario
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + request.getCategoryId()));
            product.setCategory(category);
        }

        // Si llega nueva imagen → reemplazar
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
        return productRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    // ✅ Listar por categoría
    public List<ProductDTO> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    // ✅ Buscar por ID
    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));
        return mapToDTO(product);
    }

    // ✅ Eliminar producto con borrado de imagen
    public boolean deleteProduct(Long id) {
        Optional<Product> productOpt = productRepository.findById(id);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            deleteOldImage(product.getImageUrl());
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

    // ✅ Nuevo filtro por rango de precios (útil para frontend)
    public List<ProductDTO> filterByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return productRepository.findAll().stream()
                .filter(p -> {
                    BigDecimal price = p.getPrice() != null ? p.getPrice() : BigDecimal.ZERO;
                    return price.compareTo(minPrice) >= 0 && price.compareTo(maxPrice) <= 0;
                })
                .map(this::mapToDTO)
                .toList();
    }

    // ✅ Guardar imagen en disco y devolver una URL accesible públicamente
    private String saveImage(MultipartFile image) throws IOException {
        Files.createDirectories(Paths.get(UPLOAD_DIR));
        String fileName = System.currentTimeMillis() + "_" + Paths.get(image.getOriginalFilename()).getFileName();
        Path filePath = Paths.get(UPLOAD_DIR, fileName.toString());
        Files.write(filePath, image.getBytes(), StandardOpenOption.CREATE);

        // Devuelve una ruta web accesible desde el frontend
        return "/uploads/products/" + fileName;
    }

    // ✅ Eliminar imagen vieja de forma segura
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
    private BigDecimal safeBigDecimal(Double value) {
        return value != null ? BigDecimal.valueOf(value) : BigDecimal.ZERO;
    }

    // ✅ Conversión segura de BigDecimal a Double (para el DTO)
    private Double toDouble(BigDecimal value) {
        return value != null ? value.doubleValue() : null;
    }
}
