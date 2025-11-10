package com.camilo.fitnorius.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDTO {

    private Long id;
    private String name;

    // 💰 Usamos BigDecimal para evitar errores de redondeo con decimales
    private BigDecimal price;
    private BigDecimal oldPrice;
    private BigDecimal discount;

    private String imageUrl; // Imagen principal
    private List<String> imageUrls; // Lista de todas las imágenes del producto

    private Long categoryId;
    private String categoryName;

    // 📝 Descripción opcional del producto
    private String description;
}
