package com.camilo.fitnorius.service;

import com.camilo.fitnorius.dto.ProductDTO;
import com.camilo.fitnorius.model.Category;
import com.camilo.fitnorius.model.Product;
import com.camilo.fitnorius.model.ProductImage;
import com.camilo.fitnorius.repository.CategoryRepository;
import com.camilo.fitnorius.repository.ProductRepository;
import com.camilo.fitnorius.repository.ProductImageRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductImageRepository productImageRepository; // 🔗 nuevo repositorio

    private static final String UPLOAD_DIR = "uploads/products/";

    // ✅ Crear producto
    public ProductDTO saveProduct(ProductDTO request, MultipartFile image) {
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

        // Guardar imagen principal
        if (image != null && !image.isEmpty()) {
            try {
                product.setImageUrl(saveImage(image));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return mapToDTO(productRepository.save(product));
    }

    // ✅ Actualizar producto (imagen principal)
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

        // Si hay nueva imagen principal
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

    // ✅ NUEVO: Actualizar producto con imágenes miniatura (sin romper lo anterior)
    public ProductDTO updateProductWithImages(
            Long id,
            ProductDTO request,
            MultipartFile mainImage,
            List<MultipartFile> newImages,
            String deleteImagesJson
    ) throws IOException {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));

        // Actualizar campos básicos
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

        // ✅ Imagen principal
        if (mainImage != null && !mainImage.isEmpty()) {
            String newImageUrl = saveImage(mainImage);
            deleteOldImage(product.getImageUrl());
            product.setImageUrl(newImageUrl);
        }

        // ✅ Eliminar imágenes miniatura si vienen en el JSON
        if (deleteImagesJson != null && !deleteImagesJson.isEmpty()) {
            ObjectMapper mapper = new ObjectMapper();
            List<String> deletePaths = mapper.readValue(deleteImagesJson, new TypeReference<List<String>>() {});
            List<ProductImage> imagesToDelete = productImageRepository.findByProductId(id).stream()
                    .filter(img -> deletePaths.contains(img.getImageUrl()))
                    .toList();

            for (ProductImage img : imagesToDelete) {
                deleteOldImage(img.getImageUrl());
                productImageRepository.delete(img);
            }
        }

        // ✅ Guardar nuevas imágenes miniatura
        if (newImages != null && !newImages.isEmpty()) {
            Files.createDirectories(Paths.get(UPLOAD_DIR));
            for (MultipartFile image : newImages) {
                if (!image.isEmpty()) {
                    String imageUrl = saveImage(image);
                    ProductImage productImage = ProductImage.builder()
                            .imageUrl(imageUrl)
                            .product(product)
                            .build();
                    productImageRepository.save(productImage);
                }
            }
        }

        // ✅ Guardar producto y devolver DTO completo
        Product updated = productRepository.save(product);
        return mapToDTOWithImages(updated);
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
        return mapToDTOWithImages(product);
    }

    // ✅ Eliminar producto
    public boolean deleteProduct(Long id) {
        Optional<Product> productOpt = productRepository.findById(id);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            deleteOldImage(product.getImageUrl());

            // Eliminar miniaturas también
            List<ProductImage> images = productImageRepository.findByProductId(id);
            for (ProductImage img : images) {
                deleteOldImage(img.getImageUrl());
                productImageRepository.delete(img);
            }

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

    // ✅ Convertir modelo → DTO (sin miniaturas)
    private ProductDTO mapToDTO(Product product) {
        return ProductDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .oldPrice(product.getOldPrice())
                .discount(product.getDiscount())
                .description(product.getDescription())
                .imageUrl(product.getImageUrl())
                .categoryId(product.getCategory().getId())
                .categoryName(product.getCategory().getName())
                .build();
    }

    // ✅ Convertir modelo → DTO (con miniaturas)
    private ProductDTO mapToDTOWithImages(Product product) {
        List<String> imageUrls = productImageRepository.findByProductId(product.getId()).stream()
                .map(ProductImage::getImageUrl)
                .collect(Collectors.toList());

        ProductDTO dto = mapToDTO(product);
        dto.setImages(imageUrls);
        return dto;
    }
}
