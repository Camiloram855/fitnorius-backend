package com.camilo.fitnorius.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // 🌍 Permitir definir orígenes desde variable de entorno o usar los predeterminados
    @Value("${cors.allowed.origins:http://localhost:5173,http://localhost:3000,"
            + "https://fitnorius-gym.vercel.app,"
            + "https://fitnorius-gym-git-main-juan-ks-projects-b6132ea5.vercel.app,"
            + "https://fitnorius-aghr9tnpz-juan-ks-projects-b6132ea5.vercel.app,"
            + "https://fitnorius-n6hbsoj6m-juan-ks-projects-b6132ea5.vercel.app}")
    private String allowedOrigins;

    // 🌍 Configuración global de CORS
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                // Convertir la cadena de orígenes en array
                String[] origins = allowedOrigins.split(",");

                registry.addMapping("/**")
                        .allowedOrigins(origins)
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }

    // 🖼️ Configuración de acceso a imágenes estáticas
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 📁 Carpeta principal de imágenes de productos
        Path productUploadDir = Paths.get(System.getProperty("user.dir"), "uploads", "products");
        String productUploadPath = productUploadDir.toFile().getAbsolutePath();

        // ✅ Servir imágenes de productos
        registry.addResourceHandler("/uploads/products/**")
                .addResourceLocations("file:" + productUploadPath + "/")
                .setCachePeriod(3600);

        // ⚙️ Servir cualquier archivo dentro de /uploads
        Path uploadBaseDir = Paths.get("uploads");
        String uploadBasePath = uploadBaseDir.toFile().getAbsolutePath();

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadBasePath + "/")
                .setCachePeriod(3600);
    }
}
