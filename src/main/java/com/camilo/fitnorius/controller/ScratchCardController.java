package com.camilo.fitnorius.controller;

import com.camilo.fitnorius.model.ScratchCardResult;
import com.camilo.fitnorius.repository.ScratchCardRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * ScratchCardController
 *
 * Endpoints:
 *   GET  /api/scratch/check  → verifica si la IP ya jugó
 *   POST /api/scratch/play   → registra la jugada y devuelve el premio
 */
@RestController
@RequestMapping("/api/scratch")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true") // Ajusta el origen de tu frontend
public class ScratchCardController {

    private final ScratchCardRepository repository;

    public ScratchCardController(ScratchCardRepository repository) {
        this.repository = repository;
    }

    // ── GET /api/scratch/check ────────────────────────────────────────────────
    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> check(HttpServletRequest request) {
        String ip = getClientIP(request);
        Optional<ScratchCardResult> existing = repository.findFirstByIpAddress(ip);

        Map<String, Object> response = new HashMap<>();
        if (existing.isPresent()) {
            response.put("alreadyPlayed", true);
            response.put("prize", buildPrizeMap(existing.get()));
        } else {
            response.put("alreadyPlayed", false);
        }
        return ResponseEntity.ok(response);
    }

    // ── POST /api/scratch/play ────────────────────────────────────────────────
    @PostMapping("/play")
    public ResponseEntity<Map<String, Object>> play(
            @RequestBody(required = false) Map<String, Object> body,
            HttpServletRequest request) {

        String ip = getClientIP(request);

        // Verificar si ya jugó (protección doble, incluso si el frontend falla)
        Optional<ScratchCardResult> existing = repository.findFirstByIpAddress(ip);
        if (existing.isPresent()) {
            Map<String, Object> response = new HashMap<>();
            response.put("alreadyPlayed", true);
            response.put("prize", buildPrizeMap(existing.get()));
            return ResponseEntity.ok(response);
        }

        // Generar premio en el backend (nunca en el frontend)
        Prize prize = generatePrize();

        // Guardar en base de datos
        ScratchCardResult result = new ScratchCardResult();
        result.setIpAddress(ip);
        result.setPrizeType(prize.type());
        result.setPrizeValue(prize.value());
        result.setPrizeLabel(prize.label());
        result.setPrizeEmoji(prize.emoji());
        result.setPlayedAt(LocalDateTime.now());

        // userId opcional si tienes autenticación
        if (body != null && body.containsKey("userId")) {
            result.setUserId(body.get("userId").toString());
        }

        repository.save(result);

        Map<String, Object> response = new HashMap<>();
        response.put("alreadyPlayed", false);
        response.put("prize", Map.of(
                "type",  prize.type(),
                "value", prize.value(),
                "label", prize.label(),
                "emoji", prize.emoji()
        ));
        return ResponseEntity.ok(response);
    }

    // ─── Lógica de premios ────────────────────────────────────────────────────
    private Prize generatePrize() {
        // Tabla de premios con probabilidades (deben sumar 100)
        // Ajusta los premios según tu negocio
        List<PrizeEntry> prizes = List.of(
                new PrizeEntry("percent", 10, "10% de descuento",   "🎉", 30),  // 30% prob
                new PrizeEntry("percent", 15, "15% de descuento",   "🌟", 25),  // 25% prob
                new PrizeEntry("fixed",  5000, "$5.000 de descuento","💰", 20),  // 20% prob
                new PrizeEntry("percent",  5, "5% de descuento",    "🛍️", 15),  // 15% prob
                new PrizeEntry("none",     0, "Sigue intentando",   "🍀", 10)   // 10% prob
        );

        int roll = new Random().nextInt(100);  // número entre 0 y 99
        int cumulative = 0;
        for (PrizeEntry entry : prizes) {
            cumulative += entry.probability();
            if (roll < cumulative) {
                return new Prize(entry.type(), entry.value(), entry.label(), entry.emoji());
            }
        }

        // Fallback (nunca debería llegar aquí si las probabilidades suman 100)
        return new Prize("none", 0, "Sigue intentando", "🍀");
    }

    // ─── IP del cliente ───────────────────────────────────────────────────────
    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty()) return xRealIP;
        return request.getRemoteAddr();
    }

    // ─── Helper para construir el mapa de respuesta del premio ────────────────
    private Map<String, Object> buildPrizeMap(ScratchCardResult r) {
        return Map.of(
                "type",  r.getPrizeType(),
                "value", r.getPrizeValue(),
                "label", r.getPrizeLabel(),
                "emoji", r.getPrizeEmoji()
        );
    }

    // ─── Records internos ─────────────────────────────────────────────────────
    record Prize(String type, double value, String label, String emoji) {}
    record PrizeEntry(String type, double value, String label, String emoji, int probability) {}
}
