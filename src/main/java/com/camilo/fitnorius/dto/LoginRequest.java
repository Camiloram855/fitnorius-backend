package com.camilo.fitnorius.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * @param username correo del usuario registrado, o el nombre de la cuenta de
 *                 rescate definida por variables de entorno. El campo conserva
 *                 el nombre histórico "username" para no romper clientes.
 */
public record LoginRequest(
        @NotBlank(message = "El usuario es obligatorio")
        @Size(max = 254, message = "El usuario es demasiado largo")
        @Pattern(
                regexp = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$|^[A-Za-z0-9._-]{3,64}$",
                message = "Escribe un correo válido"
        )
        String username,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(max = 256, message = "La contraseña es demasiado larga")
        String password
) {
}
