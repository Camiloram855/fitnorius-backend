package com.camilo.fitnorius.security;

import com.camilo.fitnorius.config.SecurityProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Emisión y validación estricta de tokens de acceso JWT. */
@Service
public class JwtService {

    /** Únicos roles que el backend sabe emitir. */
    private static final Set<String> KNOWN_ROLES = Set.of("ADMIN", "USER");

    private final SecurityProperties.Jwt properties;
    private final SecretKey signingKey;

    public JwtService(SecurityProperties securityProperties) {
        this.properties = securityProperties.getJwt();
        this.signingKey = createSigningKey(this.properties.getSecret());

        if (!StringUtils.hasText(properties.getIssuer())
                || !StringUtils.hasText(properties.getAudience())) {
            throw new IllegalStateException("JWT_ISSUER y JWT_AUDIENCE son obligatorios");
        }
        if (properties.getAccessTokenTtl() == null
                || properties.getAccessTokenTtl().isZero()
                || properties.getAccessTokenTtl().isNegative()) {
            throw new IllegalStateException("JWT_ACCESS_TOKEN_TTL debe ser positivo");
        }
        if (properties.getAccessTokenTtl().compareTo(Duration.ofHours(1)) > 0) {
            throw new IllegalStateException("JWT_ACCESS_TOKEN_TTL no debe superar una hora");
        }
        if (properties.getRefreshTokenTtl() == null
                || properties.getRefreshTokenTtl().isZero()
                || properties.getRefreshTokenTtl().isNegative()) {
            throw new IllegalStateException("JWT_REFRESH_TOKEN_TTL debe ser positivo");
        }
        if (properties.getRefreshTokenTtl().compareTo(Duration.ofDays(30)) > 0) {
            throw new IllegalStateException("JWT_REFRESH_TOKEN_TTL no debe superar 30 días");
        }
        if (properties.getRefreshReuseGrace() == null
                || properties.getRefreshReuseGrace().isNegative()
                || properties.getRefreshReuseGrace().compareTo(Duration.ofMinutes(1)) > 0) {
            throw new IllegalStateException("JWT_REFRESH_REUSE_GRACE debe estar entre 0 y 1 minuto");
        }
        if (!StringUtils.hasText(properties.getRefreshCookieName())
                || !properties.getRefreshCookieName().matches("[A-Za-z0-9_-]{1,64}")) {
            throw new IllegalStateException("JWT_REFRESH_COOKIE_NAME no es válido");
        }
        String sameSite = properties.getCookieSameSite();
        if (!"Lax".equalsIgnoreCase(sameSite)
                && !"Strict".equalsIgnoreCase(sameSite)
                && !"None".equalsIgnoreCase(sameSite)) {
            throw new IllegalStateException("JWT_COOKIE_SAME_SITE debe ser Lax, Strict o None");
        }
    }

    public AccessToken createAccessToken(String subject, String role) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(properties.getAccessTokenTtl());
        String token = Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(subject)
                .issuer(properties.getIssuer())
                .audience().add(properties.getAudience()).and()
                .claim("typ", "access")
                .claim("roles", List.of(requireKnownRole(role)))
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
        return new AccessToken(token, expiresAt);
    }

    public Claims parseAccessToken(String token) {
        if (!StringUtils.hasText(token) || token.length() > 4096) {
            throw new JwtException("Token JWT inválido");
        }

        Jws<Claims> parsed = Jwts.parser()
                .verifyWith(signingKey)
                .requireIssuer(properties.getIssuer())
                .requireAudience(properties.getAudience())
                .clockSkewSeconds(30)
                .build()
                .parseSignedClaims(token);

        Claims claims = parsed.getPayload();
        if (!"access".equals(claims.get("typ", String.class))
                || !StringUtils.hasText(claims.getSubject())
                || !StringUtils.hasText(claims.getId())
                || claims.getIssuedAt() == null
                || claims.getIssuedAt().toInstant().isAfter(Instant.now().plusSeconds(60))
                || claims.getExpiration() == null
                || !claims.getExpiration().toInstant().isAfter(Instant.now())
                || extractRoles(claims).isEmpty()) {
            throw new JwtException("Claims JWT inválidos");
        }
        return claims;
    }

    /**
     * Roles del token. Un token sin roles conocidos se rechaza: así un token
     * firmado correctamente pero emitido con un rol inexistente no concede
     * acceso.
     */
    public List<String> extractRoles(Claims claims) {
        return roles(claims).stream()
                .filter(KNOWN_ROLES::contains)
                .distinct()
                .toList();
    }

    private static String requireKnownRole(String role) {
        if (!StringUtils.hasText(role) || !KNOWN_ROLES.contains(role)) {
            throw new IllegalArgumentException("Rol desconocido");
        }
        return role;
    }

    public long getAccessTokenTtlSeconds() {
        return properties.getAccessTokenTtl().toSeconds();
    }

    public long getRefreshTokenTtlSeconds() {
        return properties.getRefreshTokenTtl().toSeconds();
    }

    public long getRefreshReuseGraceSeconds() {
        return properties.getRefreshReuseGrace().toSeconds();
    }

    public String getRefreshCookieName() {
        return properties.getRefreshCookieName();
    }

    public boolean isCookieSecure() {
        return properties.isCookieSecure();
    }

    public String getCookieSameSite() {
        return properties.getCookieSameSite();
    }

    private static SecretKey createSigningKey(String encodedSecret) {
        if (!StringUtils.hasText(encodedSecret)) {
            throw new IllegalStateException("Falta JWT_SECRET (Base64 de al menos 32 bytes)");
        }
        try {
            byte[] decoded = Decoders.BASE64.decode(encodedSecret.trim());
            if (decoded.length < 32) {
                throw new IllegalStateException("JWT_SECRET debe tener al menos 32 bytes decodificados");
            }
            return Keys.hmacShaKeyFor(decoded);
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("JWT_SECRET debe ser un valor Base64 válido", ex);
        }
    }

    @SuppressWarnings("unchecked")
    private static List<String> roles(Claims claims) {
        Object value = claims.get("roles");
        if (!(value instanceof List<?> values)) {
            return List.of();
        }
        return values.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .toList();
    }

    public record AccessToken(String value, Instant expiresAt) {
    }
}
