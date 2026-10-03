package com.camilo.fitnorius.dto;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        AuthUserResponse user
) {
}
