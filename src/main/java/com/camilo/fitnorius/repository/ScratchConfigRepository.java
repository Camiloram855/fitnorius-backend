package com.camilo.fitnorius.repository;

import com.camilo.fitnorius.model.ScratchConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScratchConfigRepository extends JpaRepository<ScratchConfig, Long> {
}
