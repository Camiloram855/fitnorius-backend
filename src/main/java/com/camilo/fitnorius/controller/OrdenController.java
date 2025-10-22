package com.camilo.fitnorius.controller;

import com.camilo.fitnorius.dto.OrderRequest;
import com.camilo.fitnorius.dto.OrderItemDTO;
import com.camilo.fitnorius.model.*;
import com.camilo.fitnorius.repository.*;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.*;


@RestController
@RequestMapping("/api/ordenes")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "https://fitnorius.vercel.app"})
public class OrdenController {

    private final ProductoRepository productoRepo;
    private final ClienteRepository clienteRepo;
    private final OrdenRepository ordenRepo;

    public OrdenController(ProductoRepository productoRepo, ClienteRepository clienteRepo, OrdenRepository ordenRepo) {
        this.productoRepo = productoRepo;
        this.clienteRepo = clienteRepo;
        this.ordenRepo = ordenRepo;
    }

    @PostMapping
    @Transactional
    public ResponseEntity<Map<String, Object>> crearOrden(@Valid @RequestBody OrderRequest req) {
        // guardar cliente
        Cliente cliente = Cliente.builder()
                .nombre(req.getNombre())
                .apellido(req.getApellido())
                .telefono(req.getTelefono())
                .correo(req.getCorreo())
                .departamento(req.getDepartamento())
                .ciudad(req.getCiudad())
                .direccion(req.getDireccion())
                .barrio(req.getBarrio())
                .torreApto(req.getTorreApto())
                .comentario(req.getComentario())
                .build();
        cliente = clienteRepo.save(cliente);

        // crear orden
        Orden orden = new Orden();
        orden.setCliente(cliente);
        orden.setEstado("pendiente");

        List<OrdenProducto> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemDTO it : req.getItems()) {
            Producto p = productoRepo.findById(it.getProductoId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Producto no encontrado: " + it.getProductoId()));

            BigDecimal subtotal = p.getPrecio().multiply(BigDecimal.valueOf(it.getCantidad()));
            OrdenProducto op = OrdenProducto.builder()
                    .orden(orden)
                    .producto(p)
                    .cantidad(it.getCantidad())
                    .subtotal(subtotal)
                    .build();

            items.add(op);
            total = total.add(subtotal);
        }

        orden.setItems(items);
        orden.setTotal(total);

        Orden ordenGuardada = ordenRepo.save(orden);

        Map<String, Object> resp = new HashMap<>();
        resp.put("idOrden", ordenGuardada.getIdOrden());
        resp.put("total", ordenGuardada.getTotal());
        resp.put("estado", ordenGuardada.getEstado());

        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }
}
