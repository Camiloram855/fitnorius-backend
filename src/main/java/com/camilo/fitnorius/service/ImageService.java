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
        Image img = imageRepository.findById(id).orElse(null);

        if (img == null) {
            System.err.println("⚠️ Imagen no encontrada con ID: " + id);
            return false;
        }

        try {
            // 🧩 Nombre del archivo
            String filename = new File(img.getUrl()).getName();
            Path filePath = Paths.get(System.getProperty("user.dir"), uploadDir, filename);
            File file = filePath.toFile();

            // 🧹 Eliminar archivo físico
            if (file.exists() && file.delete()) {
                System.out.println("🗑️ Archivo físico eliminado: " + file.getAbsolutePath());
            } else {
                System.err.println("⚠️ Archivo no encontrado: " + file.getAbsolutePath());
            }

            // 💾 Eliminar registro de la base
            imageRepository.delete(img);
            imageRepository.flush(); // ✅ Forzar commit inmediato

            System.out.println("✅ Registro eliminado de la base de datos (ID: " + id + ")");
            return true;

        } catch (Exception e) {
            System.err.println("❌ Error eliminando imagen ID " + id + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error eliminando imagen con ID: " + id, e);
        }
    }
}
