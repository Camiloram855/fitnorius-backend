package com.camilo.fitnorius.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 📁 Carpeta principal de imágenes de productos
        Path productUploadDir = Paths.get(System.getProperty("user.dir"), "uploads", "products");
        String productUploadPath = productUploadDir.toFile().getAbsolutePath();

        // ✅ Servir imágenes de productos
        registry.addResourceHandler("/uploads/products/**")
                .addResourceLocations("file:" + productUploadPath + "/")
                .setCachePeriod(3600); // (Opcional) cache de 1 hora para mejorar rendimiento

        // ⚙️ (Opcional) En caso de que más adelante agregues imágenes de categorías, usuarios, etc.
        Path uploadBaseDir = Paths.get("uploads");
        String uploadBasePath = uploadBaseDir.toFile().getAbsolutePath();

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadBasePath + "/")
                .setCachePeriod(3600);
    }
}