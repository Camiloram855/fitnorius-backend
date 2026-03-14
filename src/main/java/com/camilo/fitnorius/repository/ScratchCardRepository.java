package com.camilo.fitnorius.repository;

import com.camilo.fitnorius.model.ScratchCardResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio JPA para ScratchCardResult.
 * Spring Data genera la implementación automáticamente.
 */
@Repository
public interface ScratchCardRepository extends JpaRepository<ScratchCardResult, Long> {

    /**
     * Busca el primer resultado por dirección IP.
     * Se usa para verificar si un usuario ya participó.
     */
    Optional<ScratchCardResult> findFirstByIpAddress(String ipAddress);

    /**
     * Comprueba si existe algún resultado para una IP dada.
     * Más eficiente que cargar el objeto completo cuando solo necesitas saber si existe.
     */
    boolean existsByIpAddress(String ipAddress);
}
