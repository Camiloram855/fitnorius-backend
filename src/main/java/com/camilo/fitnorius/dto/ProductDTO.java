package com.camilo.fitnorius.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDTO {
    private Long id;
    private String name;
    private Double price;
    private Double oldPrice;
    private Double discount;
    private String imageUrl;
    private Long categoryId;
    private String categoryName;
    private String description; // ✅ Nuevo campo para descripción del producto
}