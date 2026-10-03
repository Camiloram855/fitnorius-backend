package com.camilo.fitnorius.security;

import com.camilo.fitnorius.dto.AuthResponse;
import com.camilo.fitnorius.dto.AuthUserResponse;
import com.camilo.fitnorius.dto.LoginRequest;
import com.camilo.fitnorius.model.AppUser;
import com.camilo.fitnorius.service.AppUserService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Autenticación del panel. Hay dos fuentes válidas:
 *
 * 1. Usuarios registrados en la base de datos (correo + hash BCrypt), creados
 *    por un administrador desde /api/admin/users.
 * 2. La cuenta de rescate definida por variables de entorno
 *    (ADMIN_USERNAME/ADMIN_PASSWORD), por si la tabla de usuarios llegara a
 *    quedar inaccesible.
 *
 * Ambas emiten el mismo tipo de token y tienen el mismo rol.
 */
@Service
public class AuthService {

    private final AdminCredentials adminCredentials;
    private final AppUserService appUserService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final LoginAttemptService loginAttemptService;

    public AuthService(AdminCredentials adminCredentials,
                       AppUserService appUserService,
                       JwtService jwtService,
                       RefreshTokenService refreshTokenService,
                       LoginAttemptService loginAttemptService) {
        this.adminCredentials = adminCredentials;
        this.appUserService = appUserService;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.loginAttemptService = loginAttemptService;
    }

    public AuthTokens login(LoginRequest request, String ipAddress) {
        String identifier = CredentialPolicy.normalizeIdentifier(request == null ? null : request.username());
        String password = request == null || request.password() == null ? "" : request.password();
        loginAttemptService.check(identifier, ipAddress);

        Optional<AppUser> stored = appUserService.findByEmail(identifier);

        // Se compara siempre una contraseña, exista o no el usuario, para que el
        // tiempo de respuesta no revele qué correos están registrados.
        boolean passwordMatches = stored
                .map(user -> appUserService.matches(user, password))
                .orElseGet(() -> adminCredentials.matches(identifier, password));
        boolean authenticated = passwordMatches
                && (stored.isEmpty() || stored.get().isEnabled());

        if (!authenticated) {
            loginAttemptService.recordFailure(identifier, ipAddress);
            throw new BadCredentialsException("Credenciales inválidas");
        }

        loginAttemptService.recordSuccess(identifier, ipAddress);
        stored.ifPresent(appUserService::recordLogin);

        return issueTokens(identifier);
    }

    public AuthTokens refresh(String rawRefreshToken) {
        RefreshTokenService.RotatedRefreshToken rotated = refreshTokenService.rotate(rawRefreshToken);
        AuthUserResponse user = resolveUser(rotated.subject());
        return new AuthTokens(
                rotated.subject(),
                jwtService.createAccessToken(rotated.subject(), user.role()),
                rotated.refreshToken()
        );
    }

    public void logout(String rawRefreshToken) {
        refreshTokenService.revoke(rawRefreshToken);
    }

    public AuthUserResponse currentUser(String subject) {
        return resolveUser(subject);
    }

    public AuthResponse toResponse(AuthTokens tokens) {
        return new AuthResponse(
                tokens.accessToken().value(),
                "Bearer",
                jwtService.getAccessTokenTtlSeconds(),
                resolveUser(tokens.subject())
        );
    }

    private AuthTokens issueTokens(String subject) {
        AuthUserResponse user = resolveUser(subject);
        return new AuthTokens(
                subject,
                jwtService.createAccessToken(subject, user.role()),
                refreshTokenService.issue(subject)
        );
    }

    /**
     * Resuelve la identidad de un sujeto (sub del JWT o dueño de un refresh
     * token). Falla si el usuario fue desactivado o ya no existe, de modo que
     * desactivar una cuenta corta también sus sesiones.
     */
    private AuthUserResponse resolveUser(String subject) {
        if (subject == null || subject.isBlank()) {
            throw new BadCredentialsException("Sesión inválida");
        }

        Optional<AppUser> stored = appUserService.findByEmail(subject);
        if (stored.isPresent()) {
            AppUser user = stored.get();
            if (!user.isEnabled()) {
                throw new BadCredentialsException("La cuenta está desactivada");
            }
            return new AuthUserResponse(user.getEmail(), user.getRole());
        }

        if (!adminCredentials.getUsername().equalsIgnoreCase(subject)) {
            throw new BadCredentialsException("Sesión inválida");
        }
        return new AuthUserResponse(adminCredentials.getUsername(), AppUser.ROLE_ADMIN);
    }

    public record AuthTokens(String subject,
                             JwtService.AccessToken accessToken,
                             RefreshTokenService.IssuedRefreshToken refreshToken) {
    }
}
