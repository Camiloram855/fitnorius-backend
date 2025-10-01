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
        // 📂 Carpeta donde guardamos imágenes
        Path uploadDir = Paths.get("uploads/products");
        String uploadPath = uploadDir.toFile().getAbsolutePath();

        // 🔗 Acceso a las imágenes en: http://localhost:8080/uploads/products/imagen.jpg
        registry.addResourceHandler("/uploads/products/**")
                .addResourceLocations("file:" + uploadPath + "/");
    }
}
