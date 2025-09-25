package com.camilo.fitnorius.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "orden_productos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdenProducto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_orden_producto")
    private Long idOrdenProducto;

    @ManyToOne
    @JoinColumn(name = "id_orden")
    private Orden orden;

    @ManyToOne
    @JoinColumn(name = "id_producto")
    private Producto producto;

    private Integer cantidad;

    @Column(precision = 10, scale = 2)
    private BigDecimal subtotal;
}
