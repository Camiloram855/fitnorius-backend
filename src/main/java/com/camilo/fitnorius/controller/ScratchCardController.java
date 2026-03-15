package com.camilo.fitnorius.controller;

import com.camilo.fitnorius.model.ScratchCardResult;
import com.camilo.fitnorius.model.ScratchPrize;
import com.camilo.fitnorius.repository.ScratchCardRepository;
import com.camilo.fitnorius.repository.ScratchPrizeRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@CrossOrigin(origins = "${FRONTEND_URL:http://localhost:5173}", allowCredentials = "true")
public class ScratchCardController {

    private final ScratchCardRepository cardRepo;
    private final ScratchPrizeRepository prizeRepo;

    public ScratchCardController(ScratchCardRepository cardRepo, ScratchPrizeRepository prizeRepo) {
        this.cardRepo = cardRepo;
        this.prizeRepo = prizeRepo;
    }

    // ════════════════════════════════════════════════════════════════════════
    // PÚBLICOS — usados por el cliente en el checkout
    // ════════════════════════════════════════════════════════════════════════

    @GetMapping("/api/scratch/check")
    public ResponseEntity<Map<String, Object>> check(HttpServletRequest request) {
        String ip = getClientIP(request);
        Optional<ScratchCardResult> existing = cardRepo.findFirstByIpAddress(ip);
        Map<String, Object> response = new HashMap<>();
        if (existing.isPresent()) {
            response.put("alreadyPlayed", true);
            response.put("prize", buildPrizeMap(existing.get()));
        } else {
            response.put("alreadyPlayed", false);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/scratch/play")
    public ResponseEntity<Map<String, Object>> play(
            @RequestBody(required = false) Map<String, Object> body,
            HttpServletRequest request) {

        String ip = getClientIP(request);
        Optional<ScratchCardResult> existing = cardRepo.findFirstByIpAddress(ip);
        if (existing.isPresent()) {
            Map<String, Object> response = new HashMap<>();
            response.put("alreadyPlayed", true);
            response.put("prize", buildPrizeMap(existing.get()));
            return ResponseEntity.ok(response);
        }

        ScratchPrize prize = drawPrize();

        ScratchCardResult result = new ScratchCardResult();
        result.setIpAddress(ip);
        result.setPrizeType(prize.getType());
        result.setPrizeValue(prize.getValue());
        result.setPrizeLabel(prize.getLabel());
        result.setPrizeEmoji(prize.getEmoji());
        result.setPlayedAt(LocalDateTime.now());
        if (body != null && body.get("userId") != null)
            result.setUserId(body.get("userId").toString());
        cardRepo.save(result);

        Map<String, Object> response = new HashMap<>();
        response.put("alreadyPlayed", false);
        response.put("prize", Map.of(
                "type",  prize.getType(),
                "value", prize.getValue(),
                "label", prize.getLabel(),
                "emoji", prize.getEmoji()
        ));
        return ResponseEntity.ok(response);
    }

    // ════════════════════════════════════════════════════════════════════════
    // ADMIN — gestión de premios y participaciones
    // ════════════════════════════════════════════════════════════════════════

    @GetMapping("/api/admin/scratch/prizes")
    public ResponseEntity<List<ScratchPrize>> getAllPrizes() {
        return ResponseEntity.ok(prizeRepo.findAll());
    }

    @PostMapping("/api/admin/scratch/prizes")
    public ResponseEntity<ScratchPrize> createPrize(@RequestBody ScratchPrize prize) {
        prize.setActive(true);
        return ResponseEntity.ok(prizeRepo.save(prize));
    }

    @PutMapping("/api/admin/scratch/prizes/{id}")
    public ResponseEntity<ScratchPrize> updatePrize(@PathVariable Long id,
                                                     @RequestBody ScratchPrize data) {
        return prizeRepo.findById(id).map(p -> {
            p.setLabel(data.getLabel());
            p.setEmoji(data.getEmoji());
            p.setType(data.getType());
            p.setValue(data.getValue());
            p.setWeight(data.getWeight());
            p.setActive(data.isActive());
            return ResponseEntity.ok(prizeRepo.save(p));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/api/admin/scratch/prizes/{id}")
    public ResponseEntity<Void> deletePrize(@PathVariable Long id) {
        if (!prizeRepo.existsById(id)) return ResponseEntity.notFound().build();
        prizeRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/admin/scratch/results")
    public ResponseEntity<List<ScratchCardResult>> getResults() {
        return ResponseEntity.ok(cardRepo.findAll());
    }

    @DeleteMapping("/api/admin/scratch/results/ip")
    public ResponseEntity<Map<String, String>> resetByIP(@RequestParam String ip) {
        Optional<ScratchCardResult> result = cardRepo.findFirstByIpAddress(ip);
        if (result.isEmpty())
            return ResponseEntity.ok(Map.of("message", "No se encontró participación para esa IP."));
        cardRepo.delete(result.get());
        return ResponseEntity.ok(Map.of("message", "Participación de " + ip + " eliminada."));
    }

    // ════════════════════════════════════════════════════════════════════════
    // HELPERS
    // ════════════════════════════════════════════════════════════════════════

    private ScratchPrize drawPrize() {
        List<ScratchPrize> prizes = prizeRepo.findByActiveTrue();
        if (prizes.isEmpty()) {
            ScratchPrize fb = new ScratchPrize();
            fb.setType("none"); fb.setValue(0);
            fb.setLabel("Sin premio"); fb.setEmoji("🍀");
            return fb;
        }
        int total = prizes.stream().mapToInt(ScratchPrize::getWeight).sum();
        int roll  = new Random().nextInt(total);
        int cum   = 0;
        for (ScratchPrize p : prizes) {
            cum += p.getWeight();
            if (roll < cum) return p;
        }
        return prizes.get(prizes.size() - 1);
    }

    private String getClientIP(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isEmpty()) return xff.split(",")[0].trim();
        String xri = request.getHeader("X-Real-IP");
        if (xri != null && !xri.isEmpty()) return xri;
        return request.getRemoteAddr();
    }

    private Map<String, Object> buildPrizeMap(ScratchCardResult r) {
        return Map.of("type", r.getPrizeType(), "value", r.getPrizeValue(),
                      "label", r.getPrizeLabel(), "emoji", r.getPrizeEmoji());
    }
}
