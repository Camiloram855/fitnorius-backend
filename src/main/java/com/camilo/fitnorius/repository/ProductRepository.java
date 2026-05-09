package com.camilo.fitnorius.repository;

import com.camilo.fitnorius.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // ✅ Buscar productos por categoría
    List<Product> findByCategoryIdOrderByDisplayOrderAscIdAsc(Long categoryId);

    // ✅ Eliminar todos los productos por categoría
    void deleteByCategoryId(Long categoryId);

    // ✅ Buscar productos por nombre (para el buscador)
    List<Product> findByNameContainingIgnoreCase(String name);

    // ✅ Buscar por nombre o descripción (más potente para buscadores)
    List<Product> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String description);

    // 🔍 Nuevo: Buscar productos dentro de un rango de precios
    @Query("SELECT p FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice")
    List<Product> findByPriceBetween(
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice
    );

    List<Product> findAllByOrderByDisplayOrderAscIdAsc();

    @Query("SELECT COALESCE(MAX(p.displayOrder), -1) FROM Product p")
    Integer findMaxDisplayOrder();
}
