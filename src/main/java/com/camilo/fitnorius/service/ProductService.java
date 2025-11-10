package com.camilo.fitnorius.service;

import com.camilo.fitnorius.dto.ProductDTO;
import com.camilo.fitnorius.model.Category;
import com.camilo.fitnorius.model.Product;
import com.camilo.fitnorius.repository.CategoryRepository;
import com.camilo.fitnorius.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.camilo.fitnorius.model.ProductImage;
import java.util.Objects;


import java.io.IOException;
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
            List<ProductImage> productImages = images.stream().map(file -> {
                try {
                    String url = saveImage(file);
                    return ProductImage.builder().url(url).product(product).build();
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }).filter(Objects::nonNull).toList();

            product.setImages(productImages);
        }

        return mapToDTO(productRepository.save(product));
    }


    // ✅ Actualizar producto
    public ProductDTO updateProduct(Long id, ProductDTO request, MultipartFile image) {
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

    // ✅ Eliminar producto
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

    // ✅ Guardar imagen en carpeta
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
