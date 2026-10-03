package com.camilo.fitnorius.security;

import com.camilo.fitnorius.config.SecurityProperties;
import com.camilo.fitnorius.model.RefreshToken;
import com.camilo.fitnorius.repository.RefreshTokenRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RefreshTokenServiceTest {

    @Test
    void allowsAConcurrentReuseOnlyDuringTheConfiguredGraceWindow() {
        RefreshTokenRepository repository = mock(RefreshTokenRepository.class);
        SecurityProperties properties = new SecurityProperties();
        properties.getJwt().setRefreshTokenTtl(Duration.ofDays(1));
        properties.getJwt().setRefreshReuseGrace(Duration.ofSeconds(10));

        RefreshToken oldToken = new RefreshToken(
                "old-hash", "family-1", "admin",
                Instant.now().plusSeconds(60), Instant.now().minusSeconds(60)
        );
        oldToken.revoke(Instant.now().minusSeconds(1));
        oldToken.markReplacedBy("replacement-hash");
        when(repository.findByTokenHashForUpdate(anyString())).thenReturn(Optional.of(oldToken));
        when(repository.existsByFamilyIdAndRevokedAtIsNull("family-1")).thenReturn(true);
        when(repository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RefreshTokenService service = new RefreshTokenService(repository, properties);
        RefreshTokenService.RotatedRefreshToken result = service.rotate("old-raw");

        assertNotNull(result.refreshToken().value());
        assertEquals("admin", result.subject());
        assertEquals("family-1", result.familyId());
        verify(repository).save(any(RefreshToken.class));
    }

    @Test
    void rejectsReuseAfterTheGraceWindow() {
        RefreshTokenRepository repository = mock(RefreshTokenRepository.class);
        SecurityProperties properties = new SecurityProperties();
        properties.getJwt().setRefreshTokenTtl(Duration.ofDays(1));
        properties.getJwt().setRefreshReuseGrace(Duration.ZERO);

        RefreshToken oldToken = new RefreshToken(
                "old-hash", "family-1", "admin",
                Instant.now().plusSeconds(60), Instant.now().minusSeconds(60)
        );
        oldToken.revoke(Instant.now().minusSeconds(1));
        oldToken.markReplacedBy("replacement-hash");
        when(repository.findByTokenHashForUpdate(anyString())).thenReturn(Optional.of(oldToken));

        RefreshTokenService service = new RefreshTokenService(repository, properties);

        assertThrows(BadCredentialsException.class, () -> service.rotate("old-raw"));
        verify(repository).revokeFamily(anyString(), any(Instant.class));
    }

    @Test
    void resolvesTheOwnerOfTheSessionFromTheStoredToken() {
        RefreshTokenRepository repository = mock(RefreshTokenRepository.class);
        SecurityProperties properties = new SecurityProperties();
        properties.getJwt().setRefreshTokenTtl(Duration.ofDays(1));
        properties.getJwt().setRefreshReuseGrace(Duration.ofSeconds(10));

        RefreshToken active = new RefreshToken(
                "hash-1", "family-9", "persona@fitnorius.co",
                Instant.now().plusSeconds(600), Instant.now().minusSeconds(60)
        );
        when(repository.findByTokenHashForUpdate(anyString())).thenReturn(Optional.of(active));
        when(repository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RefreshTokenService service = new RefreshTokenService(repository, properties);
        RefreshTokenService.RotatedRefreshToken result = service.rotate("raw-value");

        assertEquals("persona@fitnorius.co", result.subject());
        assertEquals("family-9", result.familyId());
    }

    @Test
    void rejectsAnUnknownRefreshToken() {
        RefreshTokenRepository repository = mock(RefreshTokenRepository.class);
        SecurityProperties properties = new SecurityProperties();
        properties.getJwt().setRefreshTokenTtl(Duration.ofDays(1));
        when(repository.findByTokenHashForUpdate(anyString())).thenReturn(Optional.empty());

        RefreshTokenService service = new RefreshTokenService(repository, properties);

        assertThrows(BadCredentialsException.class, () -> service.rotate("no-existe"));
    }
}
