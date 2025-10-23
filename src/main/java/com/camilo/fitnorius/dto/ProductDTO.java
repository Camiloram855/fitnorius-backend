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

    // 💰 Usamos BigDecimal para precios exactos
    private BigDecimal price;
    private BigDecimal oldPrice;
    private BigDecimal discount;

    private String imageUrl;
    private Long categoryId;
    private String categoryName;

    // 📝 Descripción opcional del producto
    private String description;

    // ✅ Métodos auxiliares para construir desde Double (evita errores de tipo)
    public void setPrice(Double price) {
        this.price = price != null ? BigDecimal.valueOf(price) : null;
    }

    public void setOldPrice(Double oldPrice) {
        this.oldPrice = oldPrice != null ? BigDecimal.valueOf(oldPrice) : null;
    }

    public void setDiscount(Double discount) {
        this.discount = discount != null ? BigDecimal.valueOf(discount) : null;
    }

    // ✅ Sobrecarga para el builder (por si usas .builder().price(100.0).build())
    public static class ProductDTOBuilder {
        public ProductDTOBuilder price(Double price) {
            this.price = price != null ? BigDecimal.valueOf(price) : null;
            return this;
        }

        public ProductDTOBuilder oldPrice(Double oldPrice) {
            this.oldPrice = oldPrice != null ? BigDecimal.valueOf(oldPrice) : null;
            return this;
        }

        public ProductDTOBuilder discount(Double discount) {
            this.discount = discount != null ? BigDecimal.valueOf(discount) : null;
            return this;
        }
    }
}
