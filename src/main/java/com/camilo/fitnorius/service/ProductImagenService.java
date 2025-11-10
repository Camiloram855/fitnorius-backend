package com.camilo.fitnorius.service;

import com.camilo.fitnorius.model.Product;
import com.camilo.fitnorius.model.ProductImagen;
import com.camilo.fitnorius.repository.ProductRepository;
import com.camilo.fitnorius.repository.ProductImagenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductImagenService {

    private final ProductImagenRepository productImagenRepository;
    private final ProductRepository productRepository;

    // 📂 Carpeta donde se guardarán las imágenes (Railway: /app/uploads)
    private static final String UPLOAD_DIR = "uploads/product_images/";

    // 🟢 Guardar varias imágenes asociadas a un producto
    public List<ProductImagen> saveImages(Long productId, List<MultipartFile> images) throws IOException {
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isEmpty()) {
            throw new IllegalArgumentException("Producto no encontrado con ID: " + productId);
        }

        Product product = productOpt.get();
        List<ProductImagen> savedImages = new ArrayList<>();

        // 🔁 Recorremos todas las imágenes
        for (MultipartFile image : images) {
            if (image.isEmpty()) continue;

            // 🔒 Nombre único para cada archivo
            String fileName = UUID.randomUUID() + "_" + image.getOriginalFilename();
            Path uploadPath = Paths.get(UPLOAD_DIR);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(fileName);
            Files.write(filePath, image.getBytes());

            // 🧩 Guardamos referencia en la base de datos
            ProductImagen productImage = ProductImagen.builder()
                    .imageUrl("/" + UPLOAD_DIR + fileName)
                    .product(product)
                    .build();

            savedImages.add(productImagenRepository.save(productImage));
        }

        return savedImages;
    }

    // 🟢 Obtener todas las imágenes de un producto
    public List<ProductImagen> getImagesByProduct(Long productId) {
        return productImagenRepository.findByProductId(productId);
    }

    // 🟢 Eliminar una imagen específica
    public boolean deleteImage(Long imageId) {
        Optional<ProductImagen> imageOpt = productImagenRepository.findById(imageId);
        if (imageOpt.isEmpty()) {
            return false;
        }

        ProductImagen image = imageOpt.get();
        try {
            // 🔥 Eliminamos el archivo físico también
            Path imagePath = Paths.get(image.getImageUrl().replaceFirst("/", ""));
            Files.deleteIfExists(imagePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        productImagenRepository.delete(image);
        return true;
    }

    // 🟢 Eliminar todas las imágenes de un producto (por si se elimina el producto)
    public void deleteAllByProduct(Long productId) {
        List<ProductImagen> images = productImagenRepository.findByProductId(productId);
        for (ProductImagen image : images) {
            try {
                Path imagePath = Paths.get(image.getImageUrl().replaceFirst("/", ""));
                Files.deleteIfExists(imagePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        productImagenRepository.deleteByProductId(productId);
    }
}
