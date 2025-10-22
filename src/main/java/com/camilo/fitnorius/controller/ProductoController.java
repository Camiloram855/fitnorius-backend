package com.camilo.fitnorius.controller;

import com.camilo.fitnorius.model.Producto;
import com.camilo.fitnorius.repository.ProductoRepository;
import com.camilo.fitnorius.service.WhatsappService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173" , "https://fitnorius.vercel.app"})
public class ProductoController {

    private final ProductoRepository productoRepository;
    private final WhatsappService whatsappService;

    public ProductoController(ProductoRepository productoRepository, WhatsappService whatsappService) {
        this.productoRepository = productoRepository;
        this.whatsappService = whatsappService;
    }

    @GetMapping
    public ResponseEntity<List<Producto>> listar() {
        List<Producto> productos = productoRepository.findAll();

        // 🟢 Construir mensaje con productos
        StringBuilder mensaje = new StringBuilder("📦 *Lista de productos disponibles* \n\n");
        for (Producto p : productos) {
            mensaje.append("🛒 ")
                    .append(p.getNombre())
                    .append(" - 💲")
                    .append(p.getPrecio())
                    .append("\n");
        }

        // Enviar a WhatsApp
        whatsappService.enviarMensaje(mensaje.toString());

        return ResponseEntity.ok(productos);
    }
}