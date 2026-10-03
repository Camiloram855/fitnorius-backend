package com.camilo.fitnorius.repository;

import com.camilo.fitnorius.model.PromoBanner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PromoBannerRepository extends JpaRepository<PromoBanner, Long> {

    /** Solo existe un banner: se toma el más antiguo. */
    Optional<PromoBanner> findFirstByOrderByIdAsc();
}