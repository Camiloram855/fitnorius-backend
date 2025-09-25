package com.camilo.fitnorius.dto;

import lombok.*;
import java.util.List;
import jakarta.validation.constraints.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {
    @NotBlank private String nombre;
    @NotBlank private String apellido;
    @NotBlank private String telefono;
    @Email @NotBlank private String correo;
    @NotBlank private String departamento;
    @NotBlank private String ciudad;
    @NotBlank private String direccion;
    @NotBlank private String barrio;
    private String torreApto;
    private String comentario;

    @NotEmpty
    private List<OrderItemDTO> items;
}
