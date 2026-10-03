package com.camilo.fitnorius.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Usuario administrativo del panel. El correo es el identificador de acceso y
 * la contraseña se almacena únicamente como hash BCrypt.
 */
@Entity
@Table(name = "app_users", uniqueConstraints = {
        @UniqueConstraint(name = "uk_app_user_email", columnNames = "email")
}, indexes = {
        @Index(name = "idx_app_user_email", columnList = "email")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppUser {

    public static final String ROLE_ADMIN = "ADMIN";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Correo normalizado en minúsculas. */
    @Column(nullable = false, length = 254)
    private String email;

    /** Hash BCrypt. Nunca se serializa ni se devuelve en la API. */
    @JsonIgnore
    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(nullable = false, length = 32)
    private String role;

    @Column(nullable = false)
    private boolean enabled;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "created_by", length = 254)
    private String createdBy;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (role == null || role.isBlank()) {
            role = ROLE_ADMIN;
        }
    }

    public void recordLogin(Instant when) {
        this.lastLoginAt = when;
    }
}
