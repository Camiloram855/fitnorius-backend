package com.camilo.fitnorius.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    // 🌩️ URL de la imagen almacenada en Cloudinary
    @Column(length = 500)
    private String imageUrl;

    // 🆔 ID público en Cloudinary (para eliminar/actualizar)
    @Column(name = "cloudinary_public_id", length = 255)
    private String cloudinaryPublicId;

    // 🔗 Relación con productos
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Product> products = new ArrayList<>();
}
