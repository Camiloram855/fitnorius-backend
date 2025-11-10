package com.camilo.fitnorius.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_imagenes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImagen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔗 Relación con el producto
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // 🖼️ URL o ruta de la imagen
    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    // 📝 Campo opcional para descripción (si quisieras agregar más adelante)
    private String descripcion;
}
