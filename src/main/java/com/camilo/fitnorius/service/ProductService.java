package com.camilo.fitnorius.service;

import com.camilo.fitnorius.dto.ProductDTO;
import com.camilo.fitnorius.model.Category;
import com.camilo.fitnorius.model.Image;
import com.camilo.fitnorius.model.Product;
import com.camilo.fitnorius.repository.CategoryRepository;
import com.camilo.fitnorius.repository.ProductRepository;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ImageService imageService;

    @Value("${cloudinary.cloud_name}")
    private String cloudName;

    @Value("${cloudinary.api_key}")
    private String apiKey;

    @Value("${cloudinary.api_secret}")
    private String apiSecret;

    public ProductDTO saveProduct(ProductDTO request, MultipartFile image) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Categoria no encontrada con ID: " + request.getCategoryId()));

        Product product = Product.builder()
                .name(request.getName())
                .price(request.getPrice())
                .oldPrice(request.getOldPrice())
                .discount(request.getDiscount())
                .description(request.getDescription())
                .displayOrder(nextDisplayOrder())
                .category(category)
                .build();

        if (image != null && !image.isEmpty()) {
            try {
                product.setImageUrl(uploadToCloudinary(image, "fitnorius/products/"));
            } catch (IOException e) {
                throw new RuntimeException("Error subiendo imagen principal a Cloudinary", e);
            }
        }

        Product savedProduct = productRepository.save(product);
        return mapToDTO(savedProduct);
    }

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
                    .orElseThrow(() -> new RuntimeException("Categoria no encontrada con ID: " + request.getCategoryId()));
            product.setCategory(category);
        }

        product.setAgotado(request.isAgotado());

        if (image != null && !image.isEmpty()) {
            try {
                product.setImageUrl(uploadToCloudinary(image, "fitnorius/products/"));
            } catch (IOException e) {
                throw new RuntimeException("Error actualizando imagen principal en Cloudinary", e);
            }
        }

        Product updatedProduct = productRepository.save(product);
        return mapToDTO(updatedProduct);
    }

    public void saveAdditionalImages(Long productId, List<MultipartFile> files) {
        imageService.saveImages(files, productId);
    }

    public void deleteImagesFromJson(String deleteImagesJson) {
        try {
            List<Long> idsToDelete = new ObjectMapper()
                    .readValue(deleteImagesJson, new TypeReference<List<Long>>() {
                    });
            idsToDelete.forEach(imageService::deleteImage);
        } catch (IOException e) {
            throw new RuntimeException("Error procesando deleteImages JSON", e);
        }
    }

    public List<ProductDTO> getAllProducts() {
        return productRepository.findAllByOrderByDisplayOrderAscIdAsc()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    public List<ProductDTO> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryIdOrderByDisplayOrderAscIdAsc(categoryId)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));
        return mapToDTO(product);
    }

    public boolean deleteProduct(Long id) {
        Optional<Product> productOpt = productRepository.findById(id);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();

            List<Image> images = imageService.findByProductId(product.getId());
            images.forEach(img -> imageService.deleteImage(img.getId()));

            productRepository.deleteById(id);
            return true;
        }
        return false;
    }

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

    @Transactional
    public void reorderProducts(List<Long> orderedProductIds) {
        if (orderedProductIds == null || orderedProductIds.isEmpty()) {
            return;
        }

        List<Product> currentOrdered = productRepository.findAllByOrderByDisplayOrderAscIdAsc();
        List<Product> reordered = new ArrayList<>(currentOrdered.size());

        for (Long productId : orderedProductIds) {
            currentOrdered.stream()
                    .filter(p -> p.getId().equals(productId))
                    .findFirst()
                    .ifPresent(reordered::add);
        }

        for (Product product : currentOrdered) {
            boolean alreadyIncluded = reordered.stream().anyMatch(p -> p.getId().equals(product.getId()));
            if (!alreadyIncluded) {
                reordered.add(product);
            }
        }

        for (int i = 0; i < reordered.size(); i++) {
            reordered.get(i).setDisplayOrder(i);
        }

        productRepository.saveAll(reordered);
    }

    private String uploadToCloudinary(MultipartFile file, String folder) throws IOException {
        Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret
        ));

        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "folder", folder
        ));

        return uploadResult.get("secure_url").toString();
    }

    private ProductDTO mapToDTO(Product product) {
        List<Image> images = imageService.findByProductId(product.getId());
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
                .agotado(product.isAgotado())
                .displayOrder(product.getDisplayOrder())
                .images(images)
                .build();
    }

    public ProductDTO setAgotado(Long id, boolean estado) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        product.setAgotado(estado);
        Product saved = productRepository.save(product);
        return mapToDTO(saved);
    }

    private int nextDisplayOrder() {
        Integer max = productRepository.findMaxDisplayOrder();
        return (max == null ? -1 : max) + 1;
    }
}
