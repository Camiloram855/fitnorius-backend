package com.camilo.fitnorius.service;

import com.camilo.fitnorius.model.Image;
import com.camilo.fitnorius.model.Product;
import com.camilo.fitnorius.repository.ImageRepository;
import com.camilo.fitnorius.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    public List<Image> saveImages(List<MultipartFile> files, Long productId) {
        final Product product = (productId != null)
                ? productRepository.findById(productId).orElse(null)
                : null;

        Path basePath = Paths.get(System.getProperty("user.dir"), uploadDir);
        File folder = basePath.toFile();

        if (!folder.exists() && !folder.mkdirs()) {
            throw new RuntimeException("❌ No se pudo crear la carpeta de subida: " + folder.getAbsolutePath());
        }

        return files.stream().map(file -> {
            try {
                String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
                File destination = new File(folder, filename);
                file.transferTo(destination);

                String fileUrl = "/uploads/products/" + filename;

                Image image = Image.builder()
                        .url(fileUrl)
                        .product(product)
                        .build();

                return imageRepository.save(image);

            } catch (IOException e) {
                throw new RuntimeException("❌ Error guardando archivo: " + file.getOriginalFilename(), e);
            }
        }).toList();
    }

    // ✅ Ahora devuelve booleano y elimina archivo + registro
    public boolean deleteImage(Long id) {
        Image img = imageRepository.findById(id)
                .orElse(null);

        if (img == null) {
            System.err.println("⚠️ Imagen no encontrada con ID: " + id);
            return false;
        }

        try {
            // 📁 Obtener nombre de archivo
            String filename = new File(img.getUrl()).getName();
            Path filePath = Paths.get(System.getProperty("user.dir"), uploadDir, filename);
            File file = filePath.toFile();

            // 🧹 Eliminar archivo físico si existe
            if (file.exists() && file.delete()) {
                System.out.println("🗑️ Archivo eliminado: " + file.getAbsolutePath());
            } else {
                System.err.println("⚠️ No se encontró el archivo o no se pudo eliminar: " + file.getAbsolutePath());
            }

            // 🧹 Eliminar registro de la base de datos
            imageRepository.delete(img);
            System.out.println("✅ Registro de imagen eliminado de la base de datos (ID: " + id + ")");

            return true;

        } catch (Exception e) {
            System.err.println("❌ Error eliminando imagen ID " + id + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
