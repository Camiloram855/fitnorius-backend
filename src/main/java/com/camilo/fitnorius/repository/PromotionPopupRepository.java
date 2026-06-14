package com.camilo.fitnorius.repository;

import com.camilo.fitnorius.model.PromotionPopup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PromotionPopupRepository extends JpaRepository<PromotionPopup, Long> {
    Optional<PromotionPopup> findFirstByOrderByIdAsc();
}
