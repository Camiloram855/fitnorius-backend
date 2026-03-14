package com.camilo.fitnorius.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad JPA que mapea la tabla scratch_card_results  en MySQL.
 */
@Entity
@Table(
    name = "scratch_card_results",
    indexes = {
        @Index(name = "idx_ip_address", columnList = "ip_address")
    }
)
public class ScratchCardResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** IP del cliente (usada como identificador para limitar participaciones) */
    @Column(name = "ip_address", nullable = false, length = 45)
    private String ipAddress;

    /** ID de usuario registrado (opcional, null si no hay autenticación) */
    @Column(name = "user_id", length = 100)
    private String userId;

    /**
     * Tipo de premio:
     *   "percent" → porcentaje de descuento
     *   "fixed"   → valor fijo de descuento en pesos
     *   "none"    → sin premio
     */
    @Column(name = "prize_type", nullable = false, length = 20)
    private String prizeType;

    /** Valor numérico del premio (ej. 10 para 10%, 5000 para $5.000) */
    @Column(name = "prize_value", nullable = false)
    private double prizeValue;

    /** Descripción legible del premio (ej. "10% de descuento") */
    @Column(name = "prize_label", nullable = false, length = 100)
    private String prizeLabel;

    /** Emoji del premio para el frontend (ej. "🎉") */
    @Column(name = "prize_emoji", length = 10)
    private String prizeEmoji;

    /** Momento exacto en que se jugó */
    @Column(name = "played_at", nullable = false)
    private LocalDateTime playedAt;

    // ── Getters & Setters ──────────────────────────────────────────────────────

    public Long getId() { return id; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getPrizeType() { return prizeType; }
    public void setPrizeType(String prizeType) { this.prizeType = prizeType; }

    public double getPrizeValue() { return prizeValue; }
    public void setPrizeValue(double prizeValue) { this.prizeValue = prizeValue; }

    public String getPrizeLabel() { return prizeLabel; }
    public void setPrizeLabel(String prizeLabel) { this.prizeLabel = prizeLabel; }

    public String getPrizeEmoji() { return prizeEmoji; }
    public void setPrizeEmoji(String prizeEmoji) { this.prizeEmoji = prizeEmoji; }

    public LocalDateTime getPlayedAt() { return playedAt; }
    public void setPlayedAt(LocalDateTime playedAt) { this.playedAt = playedAt; }
}
