package com.camilo.fitnorius.repository;

import com.camilo.fitnorius.model.ScratchPrize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScratchPrizeRepository extends JpaRepository<ScratchPrize, Long> {

    /** Solo los premios activos (usados al sortear) */
    List<ScratchPrize> findByActiveTrue();
}
