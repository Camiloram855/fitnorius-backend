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

    // 🌩️ URL pública de la imagen almacenada en Cloudinary
    @Column(name = "image_url")
    private String imageUrl;

    // 🆔 ID único de Cloudinary (necesario para eliminar o actualizar)
    @Column(name = "cloudinary_public_id")
    private String cloudinaryPublicId;

    // 🧩 Relación con productos
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore // evita recursión infinita al devolver categorías
    private List<Product> products = new ArrayList<>();
}
