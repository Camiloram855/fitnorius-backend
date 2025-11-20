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

    private BigDecimal price;
    private BigDecimal oldPrice;
    private BigDecimal discount;

    private String imageUrl;
    private Long categoryId;
    private String categoryName;

    private String description;

    private boolean agotado;  // ← FALTABA ESTO

    private List<Image> images;
}
