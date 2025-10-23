package com.camilo.fitnorius.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    // 💰 Precio actual con precisión de hasta 15 dígitos, 2 decimales
    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal price = BigDecimal.ZERO;

    // 💰 Precio anterior (opcional)
    @Column(name = "old_price", precision = 15, scale = 2)
    private BigDecimal oldPrice;

    // 📉 Descuento (porcentaje, opcional)
    @Column(precision = 5, scale = 2)
    private BigDecimal discount;

    // 🖼️ Imagen del producto (URL o ruta)
    private String imageUrl;

    // 📝 Descripción larga del producto
    @Column(columnDefinition = "TEXT")
    private String description;

    // 🔗 Relación con la categoría
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @ToString.Exclude
    private Category category;

    // ✅ Método para asegurar que los valores nulos no rompan la conversión
    @PrePersist
    @PreUpdate
    private void prePersist() {
        if (price == null) price = BigDecimal.ZERO;
        if (discount == null) discount = BigDecimal.ZERO;
    }
}
