package com.camilo.fitnorius.service;

import com.camilo.fitnorius.exception.EmailAlreadyRegisteredException;
import com.camilo.fitnorius.exception.WeakPasswordException;
import com.camilo.fitnorius.model.AppUser;
import com.camilo.fitnorius.repository.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AppUserServiceTest {

    private static final String EMAIL = "persona@fitnorius.co";
    private static final String STRONG_PASSWORD = "Passw0rd!Segura";

    private AppUserRepository repository;
    private AppUserService service;

    @BeforeEach
    void setUp() {
        repository = mock(AppUserRepository.class);
        // Coste bajo solo para el test: BCrypt 12 tardaría cientos de ms por
        // llamada. El coste real lo fija SecurityConfig.
        PasswordEncoder encoder = new BCryptPasswordEncoder(4);
        service = new AppUserService(repository, encoder);
        when(repository.saveAndFlush(any(AppUser.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void storesOnlyTheBcryptHashNeverThePlainPassword() {
        when(repository.existsByEmail(EMAIL)).thenReturn(false);

        AppUser created = service.createUser(EMAIL, STRONG_PASSWORD, "admin");

        assertNotEquals(STRONG_PASSWORD, created.getPasswordHash());
        assertTrue(created.getPasswordHash().startsWith("$2"));
        assertTrue(service.matches(created, STRONG_PASSWORD));
        assertFalse(service.matches(created, "OtraClave!123"));
    }

    @Test
    void normalizesTheEmailAndGrantsTheAdminRole() {
        when(repository.existsByEmail(EMAIL)).thenReturn(false);

        AppUser created = service.createUser("  Persona@Fitnorius.CO ", STRONG_PASSWORD, "admin");

        assertEquals(EMAIL, created.getEmail());
        assertEquals(AppUser.ROLE_ADMIN, created.getRole());
        assertTrue(created.isEnabled());
    }

    @Test
    void rejectsAWeakPasswordBeforeTouchingTheDatabase() {
        assertThrows(WeakPasswordException.class,
                () -> service.createUser(EMAIL, "corta1!A", "admin"));
    }

    @Test
    void rejectsAMalformedEmail() {
        assertThrows(IllegalArgumentException.class,
                () -> service.createUser("no-es-correo", STRONG_PASSWORD, "admin"));
    }

    @Test
    void rejectsADuplicatedEmail() {
        when(repository.existsByEmail(EMAIL)).thenReturn(true);

        assertThrows(EmailAlreadyRegisteredException.class,
                () -> service.createUser(EMAIL, STRONG_PASSWORD, "admin"));
    }

    @Test
    void neverMatchesWhenThereIsNoUser() {
        assertFalse(service.matches(null, STRONG_PASSWORD));
    }

    @Test
    void looksUpUsersByNormalizedEmail() {
        when(repository.findByEmail(EMAIL)).thenReturn(Optional.of(AppUser.builder().email(EMAIL).build()));

        assertTrue(service.findByEmail("PERSONA@Fitnorius.CO").isPresent());
        assertTrue(service.findByEmail("  ").isEmpty());
    }
}
