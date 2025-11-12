package com.camilo.fitnorius;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import jakarta.annotation.PostConstruct;

@SpringBootApplication(scanBasePackages = "com.camilo.fitnorius")
public class FitnoriusBackendApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(FitnoriusBackendApplication.class, args);
        System.out.println("🚀 Fitnorius Backend iniciado correctamente...");
    }

    // 🔍 Verifica que las variables de Cloudinary se carguen correctamente
    @PostConstruct
    public void verificarVariablesCloudinary() {
        String cloudName = System.getenv("CLOUDINARY_CLOUD_NAME");
        String apiKey = System.getenv("CLOUDINARY_API_KEY");
        String apiSecret = System.getenv("CLOUDINARY_API_SECRET");

        System.out.println("🌩️ Verificando configuración de Cloudinary...");
        System.out.println("CLOUDINARY_CLOUD_NAME: " + (cloudName != null ? cloudName : "❌ No detectado"));
        System.out.println("CLOUDINARY_API_KEY: " + (apiKey != null ? apiKey : "❌ No detectado"));
        System.out.println("CLOUDINARY_API_SECRET: " + (apiSecret != null ? "✅ Detectado" : "❌ No detectado"));
    }
}
