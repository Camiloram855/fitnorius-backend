package com.camilo.fitnorius.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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

    // 💰 Campos de tipo BigDecimal para precios exactos
    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal price;

    @Column(name = "old_price", precision = 15, scale = 2)
    private BigDecimal oldPrice;   // Precio tachado (opcional)

    @Column(precision = 5, scale = 2)
    private BigDecimal discount;   // Descuento en %

    // 🌩️ URL principal alojada en Cloudinary
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    // 📝 Descripción del producto
    @Column(columnDefinition = "TEXT")
    private String description;

    // 🔗 Relación con Category
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @ToString.Exclude
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Category category;

    // 🧩 Relación con imágenes miniatura (también en Cloudinary)
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Image> images = new ArrayList<>();

    // ✅ Métodos de ayuda para Cloudinary
    public void addImage(Image image) {
        images.add(image);
        image.setProduct(this);
    }

    public void removeImage(Image image) {
        images.remove(image);
        image.setProduct(null);
    }
}
