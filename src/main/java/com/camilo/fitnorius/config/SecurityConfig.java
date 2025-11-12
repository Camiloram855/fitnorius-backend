package com.camilo.fitnorius.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 🔒 Desactiva CSRF y habilita CORS
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        // ✅ Permite acceso libre a tus endpoints públicos
                        .requestMatchers(
                                "/actuator/**",
                                "/api/**",
                                "/uploads/**",
                                "/**"
                        ).permitAll()
                        .anyRequest().permitAll()
                )
                // 🔧 Permite iframes (para H2-console u otros)
                .headers( headers -> headers.frameOptions(frame -> frame.disable()));

        return http.build();
    }

    // 🌍 Configuración CORS global
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // ✅ Dominios permitidos (solo los que realmente usas)
        configuration.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "http://localhost:3000",
                "https://fitnorius-gym.vercel.app",
                "https://fitnorius-gym-git-main-juan-ks-projects-b6132ea5.vercel.app"
        ));

        // ✅ Métodos HTTP permitidos
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // ✅ Cabeceras permitidas
        configuration.setAllowedHeaders(List.of("*"));

        // ✅ Cabeceras expuestas al cliente
        configuration.setExposedHeaders(List.of("Authorization", "Content-Type"));

        // ✅ Permitir envío de cookies o headers de autenticación
        configuration.setAllowCredentials(true);

        // ✅ Aplica a todas las rutas
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        // 🧠 Log informativo (opcional)
        System.out.println("✅ CORS habilitado para: " + configuration.getAllowedOrigins());
        return source;
    }
}
