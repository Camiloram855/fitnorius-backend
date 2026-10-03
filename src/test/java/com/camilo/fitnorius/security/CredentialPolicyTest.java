package com.camilo.fitnorius.security;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CredentialPolicyTest {

    private static final String EMAIL = "persona@fitnorius.co";

    @Test
    void acceptsOnlyWellFormedEmails() {
        // Los espacios alrededor se ignoran a propósito: se pega desde el portapapeles.
        assertTrue(CredentialPolicy.isValidEmail("persona@fitnorius.co"));
        assertTrue(CredentialPolicy.isValidEmail("  Persona@Fitnorius.CO "));
        assertTrue(CredentialPolicy.isValidEmail("nombre.apellido+tag@sub.fitnorius.co"));

        assertFalse(CredentialPolicy.isValidEmail("persona@fitnorius"));
        assertFalse(CredentialPolicy.isValidEmail("persona.fitnorius.co"));
        assertFalse(CredentialPolicy.isValidEmail("persona@@fitnorius.co"));
        assertFalse(CredentialPolicy.isValidEmail("persona @fitnorius.co"));
        assertFalse(CredentialPolicy.isValidEmail("persona@fit norius.co"));
        assertFalse(CredentialPolicy.isValidEmail("@fitnorius.co"));
        assertFalse(CredentialPolicy.isValidEmail("persona@"));
        assertFalse(CredentialPolicy.isValidEmail(""));
        assertFalse(CredentialPolicy.isValidEmail(null));
    }

    @Test
    void acceptsAPasswordWithEveryRequiredCharacterClass() {
        assertTrue(CredentialPolicy.passwordFailures("Passw0rd!Segura", EMAIL).isEmpty());
        assertTrue(CredentialPolicy.passwordFailures("B4nda#Resist3nte", EMAIL).isEmpty());
    }

    @Test
    void rejectsAPasswordWithoutAnUppercaseLetter() {
        assertEquals(List.of("uppercase"),
                CredentialPolicy.passwordFailures("passw0rd!segura", EMAIL));
    }

    @Test
    void rejectsAPasswordWithoutASpecialCharacter() {
        assertEquals(List.of("special"),
                CredentialPolicy.passwordFailures("Passw0rdSegura1", EMAIL));
    }

    @Test
    void rejectsAPasswordWithoutADigit() {
        assertEquals(List.of("digit"),
                CredentialPolicy.passwordFailures("Password!segura", EMAIL));
    }

    @Test
    void rejectsAPasswordWithoutALowercaseLetter() {
        assertEquals(List.of("lowercase"),
                CredentialPolicy.passwordFailures("PASSW0RD!SEGURA", EMAIL));
    }

    @Test
    void rejectsATooShortPassword() {
        assertEquals(List.of("length"),
                CredentialPolicy.passwordFailures("Aa1!aaaa", EMAIL));
    }

    @Test
    void rejectsSpacesAndTheOwnEmail() {
        assertEquals(List.of("noSpaces"),
                CredentialPolicy.passwordFailures("Passw0rd! Segura", EMAIL));
        assertEquals(List.of("notEmail"),
                CredentialPolicy.passwordFailures("Persona@fit1.co!", EMAIL));
    }

    @Test
    void normalizesTheIdentifierToLowercase() {
        assertEquals("persona@fitnorius.co",
                CredentialPolicy.normalizeIdentifier("  Persona@Fitnorius.CO "));
    }
}
