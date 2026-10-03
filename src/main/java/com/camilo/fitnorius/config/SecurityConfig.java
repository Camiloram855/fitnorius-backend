package com.camilo.fitnorius.config;

import com.camilo.fitnorius.security.JsonAccessDeniedHandler;
import com.camilo.fitnorius.security.JsonAuthenticationEntryPoint;
import com.camilo.fitnorius.security.JwtAuthenticationFilter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties(SecurityProperties.class)
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt con coste 12: resiste ataques offline sin bloquear el login.
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            CorsConfigurationSource corsConfigurationSource,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            JsonAuthenticationEntryPoint authenticationEntryPoint,
            JsonAccessDeniedHandler accessDeniedHandler) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .requestCache(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/error", "/actuator/health").permitAll()
                        .requestMatchers(HttpMethod.GET, "/uploads/**").permitAll()
                        .requestMatchers("/api/auth/login", "/api/auth/refresh", "/api/auth/logout",
                                "/api/auth/password-policy").permitAll()
                        .requestMatchers("/api/auth/me").authenticated()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/images-cloud/**").hasRole("ADMIN")
                        // Solo estas lecturas de catálogo son públicas. Un GET
                        // nuevo no se expone por olvidar una regla de seguridad.
                        .requestMatchers(HttpMethod.GET,
                                "/api/categories",
                                "/api/products",
                                "/api/products/category/**",
                                "/api/products/search",
                                "/api/products/{id}",
                                "/api/productos",
                                "/api/banner",
                                "/api/banner/all",
                                "/api/promotion-popup",
                                "/api/promo-banner",
                                "/api/images/product",
                                "/api/images/category",
                                "/api/scratch/visible",
                                "/api/scratch/check")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/ordenes", "/api/scratch/play").permitAll()
                        .requestMatchers(HttpMethod.GET, "/header-messages").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/header-messages").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/**", "/header-messages").hasRole("ADMIN")
                        .anyRequest().denyAll())
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp.policyDirectives(
                                "default-src 'none'; frame-ancestors 'none'; base-uri 'none'"))
                        .frameOptions(frame -> frame.deny())
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .maxAgeInSeconds(31536000))
                        .referrerPolicy(referrer -> referrer.policy(
                                org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter
                                        .ReferrerPolicy.NO_REFERRER)))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(SecurityProperties properties) {
        List<String> origins = properties.getCors().getAllowedOrigins() == null
                ? List.of()
                : properties.getCors().getAllowedOrigins().stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .toList();
        if (origins.isEmpty() || origins.stream().anyMatch(origin -> origin.contains("*"))) {
            throw new IllegalStateException(
                    "CORS_ALLOWED_ORIGINS debe contener dominios exactos, sin comodines"
            );
        }
        for (String origin : origins) {
            try {
                URI uri = URI.create(origin);
                boolean validScheme = "http".equalsIgnoreCase(uri.getScheme())
                        || "https".equalsIgnoreCase(uri.getScheme());
                boolean validPath = !StringUtils.hasText(uri.getPath()) || "/".equals(uri.getPath());
                if (!validScheme || !StringUtils.hasText(uri.getHost()) || !validPath
                        || uri.getUserInfo() != null || uri.getQuery() != null || uri.getFragment() != null) {
                    throw new IllegalArgumentException("Origen CORS inválido");
                }
            } catch (IllegalArgumentException exception) {
                throw new IllegalStateException("CORS_ALLOWED_ORIGINS contiene un origen inválido", exception);
            }
        }

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(origins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of(
                "Authorization", "Content-Type", "Accept", "Origin", "X-Requested-With",
                "X-Refresh-Request"
        ));
        configuration.setExposedHeaders(List.of());
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}
