package com.camilo.fitnorius.repository;

import com.camilo.fitnorius.model.ScratchCardResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ScratchCardRepository extends JpaRepository<ScratchCardResult, Long> {
    Optional<ScratchCardResult> findFirstByIpAddress(String ipAddress);
    boolean existsByIpAddress(String ipAddress);
}
