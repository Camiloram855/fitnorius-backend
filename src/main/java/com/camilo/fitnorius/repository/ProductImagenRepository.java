package com.camilo.fitnorius.repository;

import com.camilo.fitnorius.model.ProductImagen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductImagenRepository extends JpaRepository<ProductImagen, Long> {

    // 🔍 Buscar todas las imágenes por ID de producto
    List<ProductImagen> findByProductId(Long productId);

    // 🗑️ Eliminar todas las imágenes asociadas a un producto
    void deleteByProductId(Long productId);
}
