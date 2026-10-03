package com.camilo.fitnorius.security;

import com.camilo.fitnorius.config.SecurityProperties;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AdminCredentialsTest {

    @Test
    void authenticatesOnlyConfiguredCredentials() {
        SecurityProperties properties = propertiesWithPassword("a-long-test-password");
        AdminCredentials credentials = new AdminCredentials(
                properties, new BCryptPasswordEncoder(4));

        assertTrue(credentials.matches("admin", "a-long-test-password"));
        assertTrue(credentials.matches(" ADMIN ", "a-long-test-password"));
        assertFalse(credentials.matches("admin", "wrong-password"));
        assertFalse(credentials.matches("other", "a-long-test-password"));
    }

    @Test
    void acceptsAConfigurationWithBcryptHash() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(4);
        SecurityProperties properties = new SecurityProperties();
        properties.getAdmin().setUsername("admin");
        properties.getAdmin().setPasswordHash(encoder.encode("a-long-test-password"));

        AdminCredentials credentials = new AdminCredentials(properties, encoder);

        assertTrue(credentials.matches("admin", "a-long-test-password"));
        assertFalse(credentials.matches("admin", "wrong-password"));
    }

    @Test
    void failsClosedWhenNoPasswordIsConfigured() {
        SecurityProperties properties = new SecurityProperties();
        properties.getAdmin().setUsername("admin");

        assertThrows(IllegalStateException.class,
                () -> new AdminCredentials(properties, new BCryptPasswordEncoder(4)));
    }

    private static SecurityProperties propertiesWithPassword(String password) {
        SecurityProperties properties = new SecurityProperties();
        properties.getAdmin().setUsername("admin");
        properties.getAdmin().setPassword(password);
        return properties;
    }
}
