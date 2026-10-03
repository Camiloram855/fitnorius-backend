package com.camilo.fitnorius.controller;

import com.camilo.fitnorius.model.PromotionPopup;
import com.camilo.fitnorius.service.PromotionPopupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/promotion-popup")
public class PromotionPopupController {

    @Autowired
    private PromotionPopupService promotionPopupService;

    @GetMapping
    public ResponseEntity<PromotionPopup> getCurrentPopup() {
        return ResponseEntity.ok(promotionPopupService.getCurrentPopup());
    }

    @PostMapping("/save")
    public ResponseEntity<PromotionPopup> savePopup(
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(defaultValue = "false") boolean active
    ) {
        return ResponseEntity.ok(promotionPopupService.savePopup(file, active));
    }

    @PatchMapping("/visibility")
    public ResponseEntity<PromotionPopup> updateVisibility(@RequestParam boolean active) {
        return ResponseEntity.ok(promotionPopupService.updateVisibility(active));
    }

    @DeleteMapping
    public ResponseEntity<String> deletePopup() {
        promotionPopupService.deletePopup();
        return ResponseEntity.ok("Popup promocional eliminado correctamente");
    }
}
