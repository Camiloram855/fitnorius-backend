package com.camilo.fitnorius.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDTO {

    private Long id;
    private String name;

    // 💰 Evita errores de redondeo en valores decimales
    private BigDecimal price;
    private BigDecimal oldPrice;
    private BigDecimal discount;

    // 📸 Ruta del archivo en el servidor
    private String imageUrl;

    // 🧬 Imagen codificada (opcional, si se guarda o muestra directamente desde BD)
    private String imageData;

    private Long categoryId;
    private String categoryName;

    // 📝 Descripción opcional del producto
    private String description;
}
