package com.camilo.fitnorius.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    // 🖼️ URL de imagen subida a Cloudinary
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    // 🧩 Relación con productos
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore // evita recursión infinita al devolver categorías
    @Builder.Default
    private List<Product> productList = new ArrayList<>();

    // ✅ Método auxiliar para mantener sincronía bidireccional
    public void addProduct(Product product) {
        productList.add(product);
        product.setCategory(this);
    }

    public void removeProduct(Product product) {
        productList.remove(product);
        product.setCategory(null);
    }
}
