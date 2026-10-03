package com.camilo.fitnorius.controller;

import com.camilo.fitnorius.model.PromoBanner;
import com.camilo.fitnorius.dto.PromoBannerTextRequest;
import com.camilo.fitnorius.service.PromoBannerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Banner de promociones. El GET es público (lo consume la tienda); el resto
 * queda bajo /api/**, que exige ROLE_ADMIN en SecurityConfig.
 */
@RestController
@RequestMapping("/api/promo-banner")
@RequiredArgsConstructor
public class PromoBannerController {

    private final PromoBannerService promoBannerService;

    @GetMapping
    public ResponseEntity<PromoBanner> getBanner() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noCache())
                .body(promoBannerService.getCurrentBanner());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PromoBanner> saveBanner(
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "subtitle", required = false) String subtitle,
            @RequestParam(value = "active", required = false) Boolean active
    ) {
        PromoBanner saved = promoBannerService.saveBanner(file, title, subtitle, active);
        return ResponseEntity.status(HttpStatus.OK)
                .cacheControl(CacheControl.noStore())
                .body(saved);
    }

    /** Permite editar título y texto sin volver a subir la imagen. */
    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PromoBanner> updateText(@Valid @RequestBody PromoBannerTextRequest request) {
        PromoBanner saved = promoBannerService.updateText(request.title(), request.subtitle());
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(saved);
    }

    @PatchMapping("/visibility")
    public ResponseEntity<PromoBanner> updateVisibility(@RequestParam boolean active) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(promoBannerService.updateVisibility(active));
    }

    @DeleteMapping
    public ResponseEntity<String> deleteBanner() {
        promoBannerService.deleteBanner();
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body("Banner de promociones eliminado");
    }
}