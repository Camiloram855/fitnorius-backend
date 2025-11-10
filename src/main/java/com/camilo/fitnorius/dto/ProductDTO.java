package com.camilo.fitnorius.dto;

import com.camilo.fitnorius.model.Image;
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

    // 💰 Evita errores de redondeo
    private BigDecimal price;
    private BigDecimal oldPrice;
    private BigDecimal discount;

    private String imageUrl;
    private Long categoryId;
    private String categoryName;

    // 📝 Descripción opcional
    private String description;

    // 🖼️ Lista de imágenes adicionales (miniaturas)
    private List<Image> images;
}
