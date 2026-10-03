package com.camilo.fitnorius.security;

import com.camilo.fitnorius.config.SecurityProperties;
import com.camilo.fitnorius.exception.TooManyLoginAttemptsException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Limitador de intentos en memoria. Evita que un atacante haga
 * fuerza bruta contra el único administrador. En un despliegue con varias
 * réplicas debe reemplazarse por un límite compartido (Redis/gateway).
 */
@Service
public class LoginAttemptService {

    private final SecurityProperties.Login properties;
    private final Map<String, Attempt> attempts = new ConcurrentHashMap<>();
    private final AtomicInteger operations = new AtomicInteger();

    public LoginAttemptService(SecurityProperties properties) {
        this.properties = properties.getLogin();
        if (this.properties.getMaxAttempts() < 1 || this.properties.getIpMaxAttempts() < 1) {
            throw new IllegalStateException("Los límites de intentos deben ser mayores que cero");
        }
        if (this.properties.getWindow() == null || this.properties.getWindow().isNegative()
                || this.properties.getWindow().isZero()) {
            throw new IllegalStateException("La ventana de intentos debe ser positiva");
        }
        if (this.properties.getLockDuration() == null || this.properties.getLockDuration().isNegative()
                || this.properties.getLockDuration().isZero()) {
            throw new IllegalStateException("La duración del bloqueo debe ser positiva");
        }
    }

    public void check(String username, String ipAddress) {
        Instant now = Instant.now();
        cleanup(now);
        checkKey(userKey(username), now);
        checkKey(ipKey(ipAddress), now);
    }

    public void recordFailure(String username, String ipAddress) {
        Instant now = Instant.now();
        recordFailure(userKey(username), this.properties.getMaxAttempts(), now);
        recordFailure(ipKey(ipAddress), this.properties.getIpMaxAttempts(), now);
    }

    public void recordSuccess(String username, String ipAddress) {
        attempts.remove(userKey(username));
        attempts.remove(ipKey(ipAddress));
    }

    private void checkKey(String key, Instant now) {
        Attempt attempt = attempts.get(key);
        if (attempt == null) {
            return;
        }
        if (attempt.lockedUntil != null && attempt.lockedUntil.isAfter(now)) {
            long seconds = Duration.between(now, attempt.lockedUntil).getSeconds();
            throw new TooManyLoginAttemptsException(Math.max(1, seconds));
        }
        if (attempt.lockedUntil != null) {
            attempts.remove(key, attempt);
        }
    }

    private void recordFailure(String key, int maxAttempts, Instant now) {
        attempts.compute(key, (ignored, current) -> {
            if (current == null
                    || current.windowStartedAt.plus(properties.getWindow()).isBefore(now)
                    || (current.lockedUntil != null && !current.lockedUntil.isAfter(now))) {
                return new Attempt(now, 1, null);
            }
            if (current.lockedUntil != null && current.lockedUntil.isAfter(now)) {
                return current;
            }

            int failures = current.failures + 1;
            Instant lockedUntil = failures >= maxAttempts
                    ? now.plus(properties.getLockDuration())
                    : null;
            return new Attempt(current.windowStartedAt, failures, lockedUntil);
        });
    }

    private void cleanup(Instant now) {
        if (operations.incrementAndGet() % 128 != 0) {
            return;
        }
        attempts.entrySet().removeIf(entry -> {
            Attempt attempt = entry.getValue();
            return (attempt.lockedUntil != null && !attempt.lockedUntil.isAfter(now))
                    || attempt.windowStartedAt.plus(properties.getWindow())
                    .plus(properties.getLockDuration()).isBefore(now);
        });
    }

    private static String userKey(String username) {
        return "user:" + (username == null ? "" : username.trim().toLowerCase(java.util.Locale.ROOT));
    }

    private static String ipKey(String ipAddress) {
        return "ip:" + (ipAddress == null || ipAddress.isBlank() ? "unknown" : ipAddress);
    }

    private static final class Attempt {
        private final Instant windowStartedAt;
        private final int failures;
        private final Instant lockedUntil;

        private Attempt(Instant windowStartedAt, int failures, Instant lockedUntil) {
            this.windowStartedAt = windowStartedAt;
            this.failures = failures;
            this.lockedUntil = lockedUntil;
        }
    }
}
