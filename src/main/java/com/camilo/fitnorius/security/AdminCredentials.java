package com.camilo.fitnorius.security;

import com.camilo.fitnorius.config.SecurityProperties;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Credenciales del administrador configuradas por el entorno. El proyecto no
 * mantiene usuarios ni contraseñas en el código fuente.
 */
@Component
public class AdminCredentials {

    private final String username;
    private final String passwordHash;
    private final PasswordEncoder passwordEncoder;

    public AdminCredentials(SecurityProperties properties, PasswordEncoder passwordEncoder) {
        SecurityProperties.Admin admin = properties.getAdmin();
        String configuredUsername = normalize(admin.getUsername());
        String configuredHash = trim(admin.getPasswordHash());
        String configuredPassword = admin.getPassword();

        if (!StringUtils.hasText(configuredUsername)) {
            throw new IllegalStateException("Falta ADMIN_USERNAME para la autenticación administrativa");
        }
        if (configuredUsername.length() < 3
                || configuredUsername.length() > 64
                || !configuredUsername.matches("[a-z0-9._-]+")) {
            throw new IllegalStateException("ADMIN_USERNAME debe tener entre 3 y 64 caracteres válidos");
        }
        if (StringUtils.hasText(configuredHash) && StringUtils.hasText(configuredPassword)) {
            throw new IllegalStateException("Configura solo ADMIN_PASSWORD_HASH o ADMIN_PASSWORD, no ambos");
        }
        if (!StringUtils.hasText(configuredHash) && !StringUtils.hasText(configuredPassword)) {
            throw new IllegalStateException("Falta ADMIN_PASSWORD_HASH (preferido) o ADMIN_PASSWORD");
        }

        this.username = configuredUsername;
        this.passwordEncoder = passwordEncoder;

        if (StringUtils.hasText(configuredHash)) {
            String normalizedHash = configuredHash.startsWith("{bcrypt}")
                    ? configuredHash.substring("{bcrypt}".length())
                    : configuredHash;
            if (!normalizedHash.startsWith("$2")) {
                throw new IllegalStateException("ADMIN_PASSWORD_HASH debe ser un hash BCrypt válido");
            }
            try {
                // Valida el formato sin revelar la contraseña en logs.
                passwordEncoder.matches("invalid-validation-value", normalizedHash);
            } catch (IllegalArgumentException ex) {
                throw new IllegalStateException("ADMIN_PASSWORD_HASH no es un hash BCrypt válido", ex);
            }
            this.passwordHash = normalizedHash;
        } else {
            if (configuredPassword.length() < 12 || configuredPassword.length() > 256) {
                throw new IllegalStateException("ADMIN_PASSWORD debe tener entre 12 y 256 caracteres");
            }
            this.passwordHash = passwordEncoder.encode(configuredPassword);
            // No conservar la contraseña en texto plano dentro del bean de configuración.
            admin.setPassword("");
        }
    }

    public String getUsername() {
        return username;
    }

    /**
     * Always performs the BCrypt comparison, even for an unknown username. This
     * avoids making account enumeration easier through response timing.
     */
    public boolean matches(String candidateUsername, String candidatePassword) {
        String normalizedCandidate = normalize(candidateUsername);
        boolean usernameMatches = MessageDigest.isEqual(
                username.getBytes(StandardCharsets.UTF_8),
                normalizedCandidate.getBytes(StandardCharsets.UTF_8)
        );
        String safePassword = candidatePassword == null ? "" : candidatePassword;
        boolean passwordMatches = passwordEncoder.matches(safePassword, passwordHash);
        return usernameMatches && passwordMatches;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(java.util.Locale.ROOT);
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
