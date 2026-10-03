package com.camilo.fitnorius.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Banner de la sección de promociones. Es una única fila: el título, el texto,
 * la imagen (alojada en Cloudinary como el resto del proyecto) y si está
 * visible en la tienda.
 */
@Entity
@Table(name = "promo_banner")
public class PromoBanner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String title = "¡Ofertas especiales!";

    @Column(length = 255)
    private String subtitle;

    /** URL de la imagen del banner. Opcional: el banner puede ser solo texto. */
    @Column(length = 500)
    private String imageUrl;

    /** publicId de Cloudinary, necesario para borrar el asset al reemplazarlo. */
    @Column(length = 255)
    private String publicId;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "updated_at")
    private Instant updatedAt;

    public PromoBanner() {
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getPublicId() {
        return publicId;
    }

    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    @PrePersist
    @PreUpdate
    void touch() {
        this.updatedAt = Instant.now();
    }
}