package com.camilo.fitnorius.repository;

import com.camilo.fitnorius.model.ProductImages;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductImagesRepository extends JpaRepository<ProductImages, Long> {

    List<ProductImages> findByProductId(Long productId);

    void deleteByProductId(Long productId);
}
