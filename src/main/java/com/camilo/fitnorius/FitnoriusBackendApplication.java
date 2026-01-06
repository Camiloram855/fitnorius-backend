package com.camilo.fitnorius;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication(scanBasePackages = "com.camilo.fitnorius")
@EnableCaching // ✅ ACTIVA EL CACHE
public class FitnoriusBackendApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(FitnoriusBackendApplication.class, args);
        System.out.println("🚀 Fitnorius Backend iniciado correctamente...");
    }

    @Value("${cloudinary.cloud_name}")
    private String cloudName;

    @Value("${cloudinary.api_key}")
    private String apiKey;

    @Value("${cloudinary.api_secret}")
    private String apiSecret;

    @PostConstruct
    public void verificarVariablesCloudinary() {
        System.out.println("🌩️ Verificando configuración de Cloudinary (Spring)...");
        System.out.println("CLOUDINARY_CLOUD_NAME: " + cloudName);
        System.out.println("CLOUDINARY_API_KEY: " + apiKey);
        System.out.println("CLOUDINARY_API_SECRET: " + (apiSecret != null ? "✅ Detectado" : "❌ No detectado"));
    }
}
