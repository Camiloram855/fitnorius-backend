package com.camilo.fitnorius.security;

import com.camilo.fitnorius.config.SecurityProperties;
import com.camilo.fitnorius.model.RefreshToken;
import com.camilo.fitnorius.repository.RefreshTokenRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

/**
 * Gestiona refresh tokens opacos. En DB solo se guarda su hash SHA-256 y
 * cada uso rota el token. Si se reutiliza uno ya revocado fuera de la ventana
 * de gracia se revoca toda la familia para invalidar la sesión.
 */
@Service
public class RefreshTokenService {

    private static final int TOKEN_BYTES = 32;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final RefreshTokenRepository repository;
    private final SecurityProperties.Jwt properties;

    public RefreshTokenService(RefreshTokenRepository repository, SecurityProperties securityProperties) {
        this.repository = repository;
        this.properties = securityProperties.getJwt();
    }

    @Transactional
    public IssuedRefreshToken issue(String username) {
        Instant now = Instant.now();
        repository.deleteExpiredBefore(now);
        return saveNewToken(username, UUID.randomUUID().toString(), null, now);
    }

    /**
     * Rota el refresh token. El token es la credencial: es opaco, se busca por
     * su hash SHA-256 y es el propio registro el que identifica al usuario
     * dueño de la sesión. El navegador nunca envía un identificador de usuario
     * que haya que contrastar.
     */
    @Transactional(noRollbackFor = BadCredentialsException.class)
    public RotatedRefreshToken rotate(String rawToken) {
        if (rawToken == null || rawToken.length() > 128) {
            throw new BadCredentialsException("Refresh token inválido");
        }

        RefreshToken current = repository.findByTokenHashForUpdate(hash(rawToken))
                .orElseThrow(() -> new BadCredentialsException("Refresh token inválido"));
        Instant now = Instant.now();

        if (current.getRevokedAt() != null) {
            // Una ventana muy corta permite que dos pestañas abran sesión al
            // mismo tiempo sin que una invalide a la otra.
            if (canReuseDuringGrace(current, now)) {
                return new RotatedRefreshToken(
                        saveNewToken(current.getUsername(), current.getFamilyId(), null, now),
                        current.getUsername(),
                        current.getFamilyId()
                );
            }
            repository.revokeFamily(current.getFamilyId(), now);
            throw new BadCredentialsException("Refresh token inválido");
        }

        if (current.isExpired(now)) {
            current.revoke(now);
            repository.save(current);
            repository.revokeFamily(current.getFamilyId(), now);
            throw new BadCredentialsException("Refresh token inválido");
        }

        current.revoke(now);
        return new RotatedRefreshToken(
                saveNewToken(current.getUsername(), current.getFamilyId(), current, now),
                current.getUsername(),
                current.getFamilyId()
        );
    }

    /** Invalida todas las sesiones activas de un usuario. */
    @Transactional
    public void revokeFamily(String familyId) {
        if (familyId == null || familyId.isBlank()) {
            return;
        }
        repository.revokeFamily(familyId, Instant.now());
    }

    @Transactional
    public void revoke(String rawToken) {
        if (rawToken == null || rawToken.length() > 128) {
            return;
        }
        repository.findByTokenHashForUpdate(hash(rawToken)).ifPresent(token ->
                repository.revokeFamily(token.getFamilyId(), Instant.now())
        );
    }

    private boolean canReuseDuringGrace(RefreshToken token, Instant now) {
        Duration grace = properties.getRefreshReuseGrace();
        return StringUtils.hasText(token.getReplacementTokenHash())
                && grace != null
                && !grace.isNegative()
                && token.getRevokedAt() != null
                && token.getRevokedAt().plus(grace).isAfter(now)
                && repository.existsByFamilyIdAndRevokedAtIsNull(token.getFamilyId());
    }

    private IssuedRefreshToken saveNewToken(
            String username,
            String familyId,
            RefreshToken replacedToken,
            Instant now
    ) {
        String rawToken = randomToken();
        String tokenHash = hash(rawToken);
        RefreshToken token = new RefreshToken(
                tokenHash,
                familyId,
                username,
                now.plus(properties.getRefreshTokenTtl()),
                now
        );
        repository.save(token);
        if (replacedToken != null) {
            replacedToken.markReplacedBy(tokenHash);
            repository.save(replacedToken);
        }
        return new IssuedRefreshToken(rawToken, token.getExpiresAt());
    }

    private static String randomToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String hash(String rawToken) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(rawToken.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 no está disponible", ex);
        }
    }

    public record IssuedRefreshToken(String value, Instant expiresAt) {
    }

    /** Token rotado junto con la identidad y la familia de la sesión. */
    public record RotatedRefreshToken(IssuedRefreshToken refreshToken,
                                      String subject,
                                      String familyId) {
    }
}
