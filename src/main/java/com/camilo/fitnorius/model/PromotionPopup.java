package com.camilo.fitnorius.model;

import jakarta.persistence.*;

@Entity
@Table(name = "promotion_popup")
public class PromotionPopup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String imageUrl;

    @Column(nullable = false)
    private String publicId;

    @Column(nullable = false)
    private boolean active = false;

    public PromotionPopup() {
    }

    public PromotionPopup(String imageUrl, String publicId, boolean active) {
        this.imageUrl = imageUrl;
        this.publicId = publicId;
        this.active = active;
    }

    public Long getId() {
        return id;
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
}
