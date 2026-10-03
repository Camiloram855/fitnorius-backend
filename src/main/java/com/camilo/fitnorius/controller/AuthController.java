package com.camilo.fitnorius.controller;

import com.camilo.fitnorius.dto.AuthResponse;
import com.camilo.fitnorius.dto.AuthUserResponse;
import com.camilo.fitnorius.dto.LoginRequest;
import com.camilo.fitnorius.dto.PasswordPolicyResponse;
import com.camilo.fitnorius.dto.PasswordPolicyResponse.PasswordRuleResponse;
import com.camilo.fitnorius.security.AuthService;
import com.camilo.fitnorius.security.CredentialPolicy;
import com.camilo.fitnorius.security.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final String REFRESH_HEADER = "X-Refresh-Request";
    private static final String REFRESH_HEADER_VALUE = "1";

    private final AuthService authService;
    private final JwtService jwtService;

    public AuthController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request,
                                               HttpServletRequest httpRequest) {
        AuthService.AuthTokens tokens = authService.login(request, httpRequest.getRemoteAddr());
        return authResponse(tokens);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @RequestHeader(value = REFRESH_HEADER, required = false) String refreshHeader,
            HttpServletRequest request) {
        requireRefreshHeader(refreshHeader);
        String rawRefreshToken = readRefreshCookie(request);
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            return ResponseEntity.noContent()
                    .cacheControl(CacheControl.noStore())
                    .build();
        }

        try {
            AuthService.AuthTokens tokens = authService.refresh(rawRefreshToken);
            return authResponse(tokens);
        } catch (BadCredentialsException exception) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .cacheControl(CacheControl.noStore())
                    .header(HttpHeaders.SET_COOKIE, clearRefreshCookie().toString())
                    .build();
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader(value = REFRESH_HEADER, required = false) String refreshHeader,
            HttpServletRequest request) {
        requireRefreshHeader(refreshHeader);
        authService.logout(readRefreshCookie(request));
        return ResponseEntity.noContent()
                .cacheControl(CacheControl.noStore())
                .header(HttpHeaders.SET_COOKIE, clearRefreshCookie().toString())
                .build();
    }

    @GetMapping("/me")
    public ResponseEntity<AuthUserResponse> me(Authentication authentication) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(authService.currentUser(authentication.getName()));
    }

    /**
     * Reglas de contraseña publicadas para el formulario de alta de usuarios.
     * No expone ningún secreto: son las mismas comprobaciones que aplica el
     * backend al crear la cuenta.
     */
    @GetMapping("/password-policy")
    public ResponseEntity<PasswordPolicyResponse> passwordPolicy() {
        List<PasswordRuleResponse> rules = CredentialPolicy.RULES.stream()
                .map(rule -> new PasswordRuleResponse(rule.id(), rule.description()))
                .toList();
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(new PasswordPolicyResponse(
                        CredentialPolicy.MIN_PASSWORD_LENGTH,
                        CredentialPolicy.MAX_PASSWORD_LENGTH,
                        rules
                ));
    }

    private ResponseEntity<AuthResponse> authResponse(AuthService.AuthTokens tokens) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .header(HttpHeaders.SET_COOKIE, refreshCookie(tokens.refreshToken().value()).toString())
                .body(authService.toResponse(tokens));
    }

    private ResponseCookie refreshCookie(String value) {
        boolean secure = jwtService.isCookieSecure();
        String sameSite = jwtService.getCookieSameSite();
        if (!secure && "None".equalsIgnoreCase(sameSite)) {
            // SameSite=None exige Secure; en desarrollo HTTP se usa Lax.
            sameSite = "Lax";
        }
        return ResponseCookie.from(jwtService.getRefreshCookieName(), value)
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path("/api/auth")
                .maxAge(Duration.ofSeconds(jwtService.getRefreshTokenTtlSeconds()))
                .build();
    }

    private ResponseCookie clearRefreshCookie() {
        boolean secure = jwtService.isCookieSecure();
        String sameSite = jwtService.getCookieSameSite();
        if (!secure && "None".equalsIgnoreCase(sameSite)) {
            sameSite = "Lax";
        }
        return ResponseCookie.from(jwtService.getRefreshCookieName(), "")
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path("/api/auth")
                .maxAge(Duration.ZERO)
                .build();
    }

    private String readRefreshCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (jwtService.getRefreshCookieName().equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private static void requireRefreshHeader(String value) {
        if (!REFRESH_HEADER_VALUE.equals(value)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solicitud de sesión no válida");
        }
    }
}
