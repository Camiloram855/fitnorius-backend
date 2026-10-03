package com.camilo.fitnorius.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;

@Entity
@Table(name = "refresh_tokens", indexes = {
        @Index(name = "idx_refresh_token_hash", columnList = "token_hash"),
        @Index(name = "idx_refresh_token_family", columnList = "family_id"),
        @Index(name = "idx_refresh_token_expiry", columnList = "expires_at")
})
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "family_id", nullable = false, length = 36)
    private String familyId;

    @Column(nullable = false, length = 64)
    private String username;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "replacement_token_hash", length = 64)
    private String replacementTokenHash;

    @Version
    @Column(name = "token_version", nullable = false)
    private long version;

    protected RefreshToken() {
        // JPA
    }

    public RefreshToken(String tokenHash, String familyId, String username,
                        Instant expiresAt, Instant createdAt) {
        this.tokenHash = tokenHash;
        this.familyId = familyId;
        this.username = username;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public String getFamilyId() {
        return familyId;
    }

    public String getUsername() {
        return username;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }

    public String getReplacementTokenHash() {
        return replacementTokenHash;
    }

    public void markReplacedBy(String replacementTokenHash) {
        this.replacementTokenHash = replacementTokenHash;
    }

    public void revoke(Instant when) {
        if (revokedAt == null) {
            revokedAt = when;
        }
    }

    public boolean isExpired(Instant now) {
        return !expiresAt.isAfter(now);
    }
}
