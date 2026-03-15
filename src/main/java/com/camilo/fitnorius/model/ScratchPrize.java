package com.camilo.fitnorius.model;

import jakarta.persistence.*;

/**
 * Entidad que representa un premio configurable del Raspa y Gana.
 * Los admins pueden crear, editar y eliminar premios desde el dashboard.
 * Hibernate crea la tabla "scratch_prizes" automáticamente (ddl-auto=update).
 */
@Entity
@Table(name = "scratch_prizes")
public class ScratchPrize {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Descripción legible, ej: "10% de descuento" */
    @Column(nullable = false, length = 100)
    private String label;

    /** Emoji visual, ej: "🎉" */
    @Column(nullable = false, length = 10)
    private String emoji;

    /**
     * Tipo de descuento:
     *   "percent" → porcentaje sobre el total
     *   "fixed"   → valor fijo en pesos colombianos
     *   "none"    → sin premio
     */
    @Column(nullable = false, length = 20)
    private String type;

    /** Valor numérico: 10 para 10%, 5000 para $5.000 */
    @Column(nullable = false)
    private double value;

    /**
     * Peso de probabilidad relativa (no tiene que sumar 100).
     * Ej: si hay 3 premios con pesos 50, 30, 20 → probabilidades 50%, 30%, 20%.
     * Esto hace que sea más intuitivo para el admin que usar porcentajes exactos.
     */
    @Column(nullable = false)
    private int weight;

    /** Permite desactivar un premio sin eliminarlo */
    @Column(nullable = false)
    private boolean active = true;

    // ── Getters & Setters ─────────────────────────────────────────────────────
    public Long getId() { return id; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getEmoji() { return emoji; }
    public void setEmoji(String emoji) { this.emoji = emoji; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }

    public int getWeight() { return weight; }
    public void setWeight(int weight) { this.weight = weight; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
