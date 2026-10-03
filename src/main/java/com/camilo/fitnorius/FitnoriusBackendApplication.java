package com.camilo.fitnorius;

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

    // Las credenciales de servicios externos se configuran mediante variables de entorno.
    // Nunca se imprimen en logs ni se almacenan en el repositorio.
}
