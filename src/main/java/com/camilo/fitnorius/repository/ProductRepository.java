package com.camilo.fitnorius.repository;

import com.camilo.fitnorius.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // 🔍 Buscar productos por categoría
    List<Product> findByCategoryId(Long categoryId);

    // 🗑️ Eliminar todos los productos de una categoría
    void deleteByCategoryId(Long categoryId);

    // 🔎 Buscar productos por nombre (para el buscador principal)
    List<Product> findByNameContainingIgnoreCase(String name);

    // 🧠 Buscar productos por nombre o descripción (ideal para buscadores más amplios)
    List<Product> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String description);

    // 💰 (Opcional) Buscar productos con un precio menor o igual a un valor
    // List<Product> findByPriceLessThanEqual(BigDecimal price);

    // 💰 (Opcional) Buscar productos en un rango de precios
    // List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);
}
