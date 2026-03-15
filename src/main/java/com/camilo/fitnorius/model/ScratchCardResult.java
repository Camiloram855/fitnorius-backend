package com.camilo.fitnorius.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "scratch_card_results",
       indexes = { @Index(name = "idx_ip_address", columnList = "ip_address") })
public class ScratchCardResult {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ip_address", nullable = false, length = 45)
    private String ipAddress;

    @Column(name = "user_id", length = 100)
    private String userId;

    @Column(name = "prize_type", nullable = false, length = 20)
    private String prizeType;

    @Column(name = "prize_value", nullable = false)
    private double prizeValue;

    @Column(name = "prize_label", nullable = false, length = 100)
    private String prizeLabel;

    @Column(name = "prize_emoji", length = 10)
    private String prizeEmoji;

    @Column(name = "played_at", nullable = false)
    private LocalDateTime playedAt;

    public Long getId() { return id; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String v) { this.ipAddress = v; }
    public String getUserId() { return userId; }
    public void setUserId(String v) { this.userId = v; }
    public String getPrizeType() { return prizeType; }
    public void setPrizeType(String v) { this.prizeType = v; }
    public double getPrizeValue() { return prizeValue; }
    public void setPrizeValue(double v) { this.prizeValue = v; }
    public String getPrizeLabel() { return prizeLabel; }
    public void setPrizeLabel(String v) { this.prizeLabel = v; }
    public String getPrizeEmoji() { return prizeEmoji; }
    public void setPrizeEmoji(String v) { this.prizeEmoji = v; }
    public LocalDateTime getPlayedAt() { return playedAt; }
    public void setPlayedAt(LocalDateTime v) { this.playedAt = v; }
}
