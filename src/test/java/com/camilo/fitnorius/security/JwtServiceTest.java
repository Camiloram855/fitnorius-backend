package com.camilo.fitnorius.security;

import com.camilo.fitnorius.config.SecurityProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    @Test
    void createsAndValidatesAccessTokenWithRequiredClaims() {
        JwtService service = service("issuer-a", "audience-a");

        Claims claims = service.parseAccessToken(service.createAccessToken("admin", "ADMIN").value());

        assertEquals("admin", claims.getSubject());
        assertEquals("issuer-a", claims.getIssuer());
        assertTrue(claims.getAudience().contains("audience-a"));
        assertEquals("access", claims.get("typ", String.class));
        assertTrue(claims.getId() != null && !claims.getId().isBlank());
    }

    @Test
    void rejectsTamperedToken() {
        JwtService service = service("issuer-a", "audience-a");
        String token = service.createAccessToken("admin", "ADMIN").value();
        int index = token.length() / 2;
        char replacement = token.charAt(index) == 'a' ? 'b' : 'a';
        String tampered = token.substring(0, index) + replacement + token.substring(index + 1);

        assertThrows(JwtException.class, () -> service.parseAccessToken(tampered));
    }

    @Test
    void rejectsTokenForDifferentAudience() {
        JwtService service = service("issuer-a", "audience-a");
        String token = service.createAccessToken("admin", "ADMIN").value();
        JwtService otherService = service("issuer-a", "audience-b");

        assertThrows(JwtException.class, () -> otherService.parseAccessToken(token));
    }

    @Test
    void issuesTokensForEachRegisteredUserWithItsOwnSubject() {
        JwtService service = service("issuer-a", "audience-a");

        Claims claims = service.parseAccessToken(
                service.createAccessToken("persona@fitnorius.co", "ADMIN").value());

        assertEquals("persona@fitnorius.co", claims.getSubject());
        assertTrue(service.extractRoles(claims).contains("ADMIN"));
    }

    @Test
    void refusesToIssueATokenWithAnUnknownRole() {
        JwtService service = service("issuer-a", "audience-a");

        assertThrows(IllegalArgumentException.class, () -> service.createAccessToken("admin", "SUPER"));
    }

    private static JwtService service(String issuer, String audience) {
        SecurityProperties properties = new SecurityProperties();
        properties.getJwt().setSecret(Base64.getEncoder().encodeToString(
                "0123456789abcdef0123456789abcdef".getBytes()));
        properties.getJwt().setIssuer(issuer);
        properties.getJwt().setAudience(audience);
        properties.getJwt().setAccessTokenTtl(Duration.ofMinutes(10));
        return new JwtService(properties);
    }
}
