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
                // 🚫 Desactiva CSRF (necesario para APIs REST)
                .csrf(csrf -> csrf.disable())

                // ✅ Habilita CORS con configuración explícita
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // ✅ Permite todas las rutas (ideal mientras pruebas)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/**", "/api/**", "/uploads/**", "/**").permitAll()
                        .anyRequest().permitAll()
                )

                // 🔧 Permite consola H2 o frames si se usan
                .headers(headers -> headers.frameOptions(frame -> frame.disable()));

        return http.build();
    }

    // 🌍 Configuración CORS global para permitir tu frontend de Vercel y local
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "http://localhost:3000",
                "https://fitnorius.vercel.app",
                "https://fitnorius-my46wlpur-juan-ks-projects-b6132ea5.vercel.app"
        ));

        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
