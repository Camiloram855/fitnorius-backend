package com.camilo.fitnorius.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ordenes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Orden {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_orden")
    private Long idOrden;

    @ManyToOne
    @JoinColumn(name = "id_cliente")
    private Cliente cliente;

    private LocalDateTime fecha = LocalDateTime.now();

    @Column(precision = 10, scale = 2)
    private BigDecimal total;

    private String estado = "pendiente";

    @OneToMany(mappedBy = "orden", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrdenProducto> items = new ArrayList<>();
}
