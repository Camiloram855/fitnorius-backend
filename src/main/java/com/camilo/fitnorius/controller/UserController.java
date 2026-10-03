package com.camilo.fitnorius.controller;

import com.camilo.fitnorius.dto.AppUserResponse;
import com.camilo.fitnorius.dto.CreateUserRequest;
import com.camilo.fitnorius.service.AppUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Gestión de usuarios del panel. Queda bajo /api/admin/**, así que Spring
 * exige ROLE_ADMIN antes de que estos métodos se ejecuten: solo un
 * administrador autenticado puede dar de alta cuentas.
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class UserController {

    private final AppUserService appUserService;

    @PostMapping
    public ResponseEntity<AppUserResponse> createUser(@Valid @RequestBody CreateUserRequest request,
                                                      Authentication authentication) {
        String createdBy = authentication == null ? null : authentication.getName();
        AppUserResponse created = AppUserResponse.from(
                appUserService.createUser(request.email(), request.password(), createdBy)
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .cacheControl(CacheControl.noStore())
                .body(created);
    }

    @GetMapping
    public ResponseEntity<List<AppUserResponse>> listUsers() {
        List<AppUserResponse> users = appUserService.listUsers().stream()
                .map(AppUserResponse::from)
                .toList();
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(users);
    }
}
