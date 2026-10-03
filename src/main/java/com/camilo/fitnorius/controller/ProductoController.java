package com.camilo.fitnorius.controller;

import com.camilo.fitnorius.model.Producto;
import com.camilo.fitnorius.repository.ProductoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoRepository productoRepository;

    public ProductoController(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    @GetMapping
    public ResponseEntity<List<Producto>> listar() {
        // Un GET público nunca debe disparar un envío externo ni consumir cuota.
        return ResponseEntity.ok(productoRepository.findAll());
    }
}