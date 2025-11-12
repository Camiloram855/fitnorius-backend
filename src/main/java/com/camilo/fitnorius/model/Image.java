package com.camilo.fitnorius.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 🌐 URL segura entregada por Cloudinary (campo principal para mostrar imagen)
     */
    @Column(nullable = false, length = 500)
    private String url;

    /**
     * 🆔 Identificador público en Cloudinary (necesario para eliminar o reemplazar imágenes)
     */
    @Column(name = "public_id", nullable = false, length = 255)
    private String publicId;

    /**
     * 🖼️ Tipo opcional (ej: 'thumbnail', 'banner', 'gallery')
     * Facilita agrupar o filtrar imágenes por tipo
     */
    @Column(length = 50)
    private String type;

    /**
     * 🔗 Relación opcional con un producto
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    @JsonIgnore
    private Product product;

    /**
     * 🔗 Relación opcional con una categoría
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @JsonIgnore
    private Category category;
}
