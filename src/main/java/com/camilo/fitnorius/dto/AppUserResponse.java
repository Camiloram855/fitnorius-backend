package com.camilo.fitnorius.dto;

import com.camilo.fitnorius.model.AppUser;

import java.time.Instant;

/** Vista pública de un usuario. Nunca incluye el hash de la contraseña. */
public record AppUserResponse(
        Long id,
        String email,
        String role,
        boolean enabled,
        Instant createdAt,
        Instant lastLoginAt
) {
    public static AppUserResponse from(AppUser user) {
        return new AppUserResponse(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.isEnabled(),
                user.getCreatedAt(),
                user.getLastLoginAt()
        );
    }
}
