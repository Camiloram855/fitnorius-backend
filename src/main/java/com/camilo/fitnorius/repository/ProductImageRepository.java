package com.camilo.fitnorius.repository;

import com.camilo.fitnorius.model.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    // Buscar imágenes por producto
    List<ProductImage> findByProductId(Long productId);

    // Eliminar todas las imágenes asociadas a un producto
    void deleteByProductId(Long productId);
}
