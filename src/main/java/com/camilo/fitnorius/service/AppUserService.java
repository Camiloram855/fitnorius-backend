package com.camilo.fitnorius.service;

import com.camilo.fitnorius.exception.EmailAlreadyRegisteredException;
import com.camilo.fitnorius.exception.WeakPasswordException;
import com.camilo.fitnorius.model.AppUser;
import com.camilo.fitnorius.repository.AppUserRepository;
import com.camilo.fitnorius.security.CredentialPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Alta y autenticación de usuarios del panel. La contraseña en claro solo
 * existe dentro de la petición: aquí se transforma en un hash BCrypt y
 * nunca se persiste ni se registra.
 */
@Service
@RequiredArgsConstructor
public class AppUserService {

    private final AppUserRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AppUser createUser(String rawEmail, String rawPassword, String createdBy) {
        String email = CredentialPolicy.normalizeEmail(rawEmail);
        if (!CredentialPolicy.isValidEmail(email)) {
            throw new IllegalArgumentException("El correo no es válido");
        }

        List<String> failures = CredentialPolicy.passwordFailures(rawPassword, email);
        if (!failures.isEmpty()) {
            String description = String.join(", ", CredentialPolicy.describeFailures(failures));
            throw new WeakPasswordException(
                    "La contraseña no cumple la política: " + description,
                    String.join(",", failures)
            );
        }

        if (repository.existsByEmail(email)) {
            throw new EmailAlreadyRegisteredException();
        }

        AppUser user = AppUser.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .role(AppUser.ROLE_ADMIN)
                .enabled(true)
                .createdBy(CredentialPolicy.normalizeIdentifier(createdBy))
                .build();

        try {
            return repository.saveAndFlush(user);
        } catch (DataIntegrityViolationException exception) {
            // Carrera entre dos altas simultáneas con el mismo correo.
            throw new EmailAlreadyRegisteredException();
        }
    }

    @Transactional(readOnly = true)
    public Optional<AppUser> findByEmail(String rawEmail) {
        String email = CredentialPolicy.normalizeEmail(rawEmail);
        if (email.isEmpty()) {
            return Optional.empty();
        }
        return repository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public List<AppUser> listUsers() {
        return repository.findAllByOrderByCreatedAtAsc();
    }

    @Transactional
    public void recordLogin(AppUser user) {
        user.recordLogin(Instant.now());
        repository.save(user);
    }

    /**
     * Compara la contraseña con el hash guardado. Se ejecuta siempre, exista o
     * no el usuario, para no revelar por tiempo de respuesta qué correos están
     * registrados.
     */
    public boolean matches(AppUser user, String rawPassword) {
        if (user == null) {
            return false;
        }
        return passwordEncoder.matches(rawPassword == null ? "" : rawPassword, user.getPasswordHash());
    }
}
