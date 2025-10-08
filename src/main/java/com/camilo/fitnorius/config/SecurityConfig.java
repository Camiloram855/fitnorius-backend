package com.camilo.fitnorius.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // 🚫 Desactiva CSRF (útil para APIs)
                .cors(cors -> cors.disable()) // 🚫 Desactiva CORS si ya lo manejas en el frontend
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() // ✅ Permite TODO
                )
                .headers(headers -> headers.frameOptions(frame -> frame.disable())); // 🔧 Permite consola H2 si la usas

        return http.build();
    }
}