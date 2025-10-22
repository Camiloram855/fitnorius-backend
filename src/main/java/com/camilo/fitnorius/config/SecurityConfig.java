package com.camilo.fitnorius.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/**", "/api/**", "/uploads/**", "/**").permitAll()
                        .anyRequest().permitAll()
                )
                .headers(headers -> headers.frameOptions(frame -> frame.disable()));

        return http.build();
    }

    // 🌍 Configuración CORS central (solo aquí)
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "http://localhost:3000",
                "https://fitnorius-gym.vercel.app",
                "https://fitnorius-gym-git-main-juan-ks-projects-b6132ea5.vercel.app",
                "https://fitnorius-aghr9tnpz-juan-ks-projects-b6132ea5.vercel.app"
        ));

        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        System.out.println("✅ CORS activo desde SecurityConfig: " + configuration.getAllowedOrigins());
        return source;
    }
}
