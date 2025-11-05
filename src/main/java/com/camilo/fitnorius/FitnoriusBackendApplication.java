package com.camilo.fitnorius;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication(scanBasePackages = "com.camilo.fitnorius")
public class FitnoriusBackendApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(FitnoriusBackendApplication.class, args);
        System.out.println("🚀 Fitnorius Backend iniciado correctamente...");
    }
}
