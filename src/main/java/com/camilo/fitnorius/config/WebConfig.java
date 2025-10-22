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
