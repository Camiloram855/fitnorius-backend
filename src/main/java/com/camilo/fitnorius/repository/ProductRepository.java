package com.camilo.fitnorius.repository;

import com.camilo.fitnorius.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // ✅ Buscar productos por categoría
    List<Product> findByCategoryId(Long categoryId);

    // ✅ Eliminar todos los productos por categoría
    void deleteByCategoryId(Long categoryId);

    // ✅ Buscar productos por nombre (para el buscador)
    List<Product> findByNameContainingIgnoreCase(String name);

    // ✅ Buscar por nombre o descripción (opcional: más potente para buscadores)
    List<Product> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String description);
}
