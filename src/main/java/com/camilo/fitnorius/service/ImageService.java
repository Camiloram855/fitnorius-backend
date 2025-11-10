package com.camilo.fitnorius.service;

import com.camilo.fitnorius.model.Image;
import com.camilo.fitnorius.model.Product;
import com.camilo.fitnorius.repository.ImageRepository;
import com.camilo.fitnorius.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageRepository imageRepository;
    private final ProductRepository productRepository;

    @Value("${upload.dir:uploads/products}")
    private String uploadDir;

    public List<Image> findByProductId(Long productId) {
        return imageRepository.findByProductId(productId);
    }

    @Transactional
    public List<Image> saveImages(List<MultipartFile> files, Long productId) {
        Product product = null;

        if (productId != null) {
            product = productRepository.findById(productId).orElse(null);
        }

        Path basePath = Paths.get(System.getProperty("user.dir"), uploadDir);
        File folder = basePath.toFile();

        if (!folder.exists() && !folder.mkdirs()) {
            throw new RuntimeException("❌ No se pudo crear la carpeta: " + folder.getAbsolutePath());
        }

        List<Image> savedImages = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
                File destination = new File(folder, filename);
                file.transferTo(destination);

                String fileUrl = "/uploads/products/" + filename;

                Image image = Image.builder()
                        .url(fileUrl)
                        .product(product)
                        .build();

                Image saved = imageRepository.saveAndFlush(image); // ✅ Fuerza persistencia inmediata
                savedImages.add(saved);

                System.out.println("📸 Guardada imagen " + fileUrl + " (ID: " + saved.getId() + ")");

            } catch (IOException e) {
                throw new RuntimeException("❌ Error guardando archivo: " + file.getOriginalFilename(), e);
            }
        }

        return savedImages;
    }

    @Transactional
    public boolean deleteImage(Long id) {
        System.out.println("🗑️ Intentando eliminar imagen con ID: " + id);

        return imageRepository.findById(id).map(img -> {
            try {
                // Archivo físico
                String filename = new File(img.getUrl()).getName();
                Path filePath = Paths.get(System.getProperty("user.dir"), uploadDir, filename);
                File file = filePath.toFile();

                if (file.exists() && file.delete()) {
                    System.out.println("✅ Archivo físico eliminado: " + file.getAbsolutePath());
                } else {
                    System.out.println("⚠️ Archivo físico no encontrado o ya eliminado: " + file.getAbsolutePath());
                }

                // Registro en base de datos
                imageRepository.delete(img);
                imageRepository.flush();

                System.out.println("✅ Registro eliminado de la base de datos (ID: " + id + ")");
                return true;
            } catch (Exception e) {
                System.err.println("❌ Error eliminando imagen ID " + id + ": " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Error eliminando imagen con ID: " + id, e);
            }
        }).orElseGet(() -> {
            System.err.println("⚠️ Imagen no encontrada en BD con ID: " + id);
            return false;
        });
    }

}
