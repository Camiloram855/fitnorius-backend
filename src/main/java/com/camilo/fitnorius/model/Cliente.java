package com.camilo.fitnorius.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "clientes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cliente")
    private Long idCliente;

    private String nombre;
    private String apellido;
    private String telefono;
    private String correo;
    private String departamento;
    private String ciudad;
    private String direccion;
    private String barrio;
    @Column(name = "torre_apto")
    private String torreApto;
    @Column(columnDefinition = "TEXT")
    private String comentario;
}
