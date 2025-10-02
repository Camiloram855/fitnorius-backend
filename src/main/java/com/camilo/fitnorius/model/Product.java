package com.camilo.fitnorius.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Double price;
    private Double oldPrice;   // precio tachado (opcional)
    private Double discount;   // descuento en %
    private String imageUrl;   // ruta donde guardas la imagen

    // Relación con Category
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @ToString.Exclude
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) // 🔑 evita errores al serializar con LAZY
    private Category category;
}
