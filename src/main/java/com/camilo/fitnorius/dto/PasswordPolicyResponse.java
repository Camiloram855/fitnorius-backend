package com.camilo.fitnorius.dto;

import java.util.List;

/**
 * Reglas de contraseña publicadas por el backend para que el formulario de
 * creación de usuario no duplique la política.
 */
public record PasswordPolicyResponse(
        int minLength,
        int maxLength,
        List<PasswordRuleResponse> rules
) {
    public record PasswordRuleResponse(String id, String description) {
    }
}
