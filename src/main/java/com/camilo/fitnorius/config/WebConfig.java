package com.camilo.fitnorius.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // 🌍 Configuración global de CORS
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins(
                                "http://localhost:5173",   // React local con Vite
                                "http://localhost:3000",   // React local con CRA
                                "https://fitnorius-4ju5x3nds-juan-ks-projects-b6132ea5.vercel.app" // tu dominio en Vercel
                        )
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

        // ⚙️ (Opcional) En caso de que agregues más carpetas de imágenes
        Path uploadBaseDir = Paths.get("uploads");
        String uploadBasePath = uploadBaseDir.toFile().getAbsolutePath();

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadBasePath + "/")
                .setCachePeriod(3600);
    }
}
