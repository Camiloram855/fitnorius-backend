package com.camilo.fitnorius.repository;

import com.camilo.fitnorius.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoRepository extends JpaRepository<Producto, Long> { }
