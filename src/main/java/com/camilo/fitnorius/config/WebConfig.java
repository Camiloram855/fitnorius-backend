package com.camilo.fitnorius.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 🔹 Carpeta principal de subidas
        Path uploadBaseDir = Paths.get(System.getProperty("user.dir"), "uploads");
        String uploadBasePath = uploadBaseDir.toFile().getAbsolutePath();

        // 🔹 Carpeta específica para productos (opcional, para organización)
        Path productUploadDir = Paths.get(uploadBasePath, "products");
        File productFolder = productUploadDir.toFile();
        if (!productFolder.exists()) {
            productFolder.mkdirs(); // Crea carpeta si no existe
        }

        // 🔹 Servir archivos /uploads/** → file:/app/uploads/
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadBasePath + "/")
                .setCachePeriod(3600)
                .resourceChain(true);

        // 🔹 Servir archivos /uploads/products/** → file:/app/uploads/products/
        registry.addResourceHandler("/uploads/products/**")
                .addResourceLocations("file:" + productUploadDir.toFile().getAbsolutePath() + "/")
                .setCachePeriod(3600)
                .resourceChain(true);

        System.out.println("📁 Archivos estáticos servidos desde: " + uploadBasePath);
    }
}
