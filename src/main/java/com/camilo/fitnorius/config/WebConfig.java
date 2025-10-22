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

    // 🌍 Permitir varios orígenes, incluyendo los de Vercel
    @Value("${cors.allowed.origins:http://localhost:5173,http://localhost:3000,https://fitnorius.vercel.app,https://fitnorius-gym.vercel.app,https://fitnorius-gym-git-main-juan-ks-projects-b6132ea5.vercel.app,https://fitnorius-aghr9tnpz-juan-ks-projects-b6132ea5.vercel.app}")
    private String allowedOrigins;

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                String[] origins = allowedOrigins.split(",");
                System.out.println("✅ Allowed origins: " + allowedOrigins); // Para confirmar en logs

                registry.addMapping("/**")
                        .allowedOrigins(origins)
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path productUploadDir = Paths.get(System.getProperty("user.dir"), "uploads", "products");
        String productUploadPath = productUploadDir.toFile().getAbsolutePath();

        registry.addResourceHandler("/uploads/products/**")
                .addResourceLocations("file:" + productUploadPath + "/")
                .setCachePeriod(3600);

        Path uploadBaseDir = Paths.get("uploads");
        String uploadBasePath = uploadBaseDir.toFile().getAbsolutePath();

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadBasePath + "/")
                .setCachePeriod(3600);
    }
}
