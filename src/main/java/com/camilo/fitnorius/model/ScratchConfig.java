package com.camilo.fitnorius.model;

import jakarta.persistence.*;

/**
 * Tabla con UNA sola fila (id = 1).
 * Guarda si el Raspa y Gana está visible para los clientes.
 */
@Entity
@Table(name = "scratch_config")
public class ScratchConfig {

    @Id
    private Long id = 1L;  // siempre la misma fila

    @Column(nullable = false)
    private boolean visible = true;  // por defecto visible

    public Long getId() { return id; }

    public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) { this.visible = visible; }
}
