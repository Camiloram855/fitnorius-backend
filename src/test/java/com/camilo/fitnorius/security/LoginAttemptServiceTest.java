package com.camilo.fitnorius.security;

import com.camilo.fitnorius.config.SecurityProperties;
import com.camilo.fitnorius.exception.TooManyLoginAttemptsException;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LoginAttemptServiceTest {

    @Test
    void blocksAfterConfiguredUsernameFailures() {
        SecurityProperties properties = new SecurityProperties();
        properties.getLogin().setMaxAttempts(2);
        properties.getLogin().setIpMaxAttempts(10);
        properties.getLogin().setWindow(Duration.ofMinutes(15));
        properties.getLogin().setLockDuration(Duration.ofMinutes(15));
        LoginAttemptService service = new LoginAttemptService(properties);

        service.recordFailure("admin", "127.0.0.1");
        service.recordFailure("admin", "127.0.0.1");

        assertThrows(TooManyLoginAttemptsException.class,
                () -> service.check("admin", "127.0.0.1"));
    }

    @Test
    void successfulLoginClearsAttempts() {
        SecurityProperties properties = new SecurityProperties();
        properties.getLogin().setMaxAttempts(2);
        properties.getLogin().setIpMaxAttempts(10);
        LoginAttemptService service = new LoginAttemptService(properties);

        service.recordFailure("admin", "127.0.0.1");
        service.recordSuccess("admin", "127.0.0.1");

        assertDoesNotThrow(() -> service.check("admin", "127.0.0.1"));
    }
}
