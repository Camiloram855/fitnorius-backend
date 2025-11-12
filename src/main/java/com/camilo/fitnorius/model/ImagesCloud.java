package com.camilo.fitnorius.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "images_cloud")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImagesCloud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nombre público que Cloudinary asigna (public_id)
    @Column(nullable = false, unique = true)
    private String publicId;

    // URL segura (para mostrar en la web)
    @Column(nullable = false, columnDefinition = "TEXT")
    private String secureUrl;

    // Formato de la imagen (jpg, png, etc.)
    private String format;

    // Ancho y alto (opcional)
    private Integer width;
    private Integer height;

    // Tamaño en bytes (opcional)
    private Long bytes;

    // Fecha de carga (automática)
    @Column(name = "created_at")
    private String createdAt;
}
