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
@ToString(exclude = "products")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    // 🌩️ URL completa de la imagen almacenada en Cloudinary
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    // 🆔 ID público en Cloudinary (para eliminar/actualizar desde el backend)
    @Column(name = "cloudinary_public_id", length = 255)
    private String cloudinaryPublicId;

    // 🔗 Relación con productos
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Product> products = new ArrayList<>();

    // ✅ Método auxiliar: asignar nueva imagen de Cloudinary
    public void setCloudinaryData(String secureUrl, String publicId) {
        this.imageUrl = secureUrl;
        this.cloudinaryPublicId = publicId;
    }
}
