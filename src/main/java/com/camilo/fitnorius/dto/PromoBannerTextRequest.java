package com.camilo.fitnorius.dto;

import jakarta.validation.constraints.Size;

/**
 * Edición del texto del banner de promociones sin volver a subir la imagen.
 */
public record PromoBannerTextRequest(
        @Size(max = 120, message = "El título es demasiado largo")
        String title,

        @Size(max = 255, message = "El texto del banner es demasiado largo")
        String subtitle
) {
}