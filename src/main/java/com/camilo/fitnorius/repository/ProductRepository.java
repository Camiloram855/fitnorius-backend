package com.camilo.fitnorius.repository;

import com.camilo.fitnorius.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // Buscar productos por categoría
    List<Product> findByCategoryId(Long categoryId);

    // Eliminar todos los productos por categoría
    void deleteByCategoryId(Long categoryId);
}
