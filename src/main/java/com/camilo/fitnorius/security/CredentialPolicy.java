package com.camilo.fitnorius.security;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Política de credenciales del panel: correo válido y contraseña con mezcla de
 * clases de caracteres. Es la única fuente de verdad; el endpoint
 * /api/auth/password-policy publica estas mismas reglas para que el formulario
 * no las duplique.
 */
public final class CredentialPolicy {

    public static final int MIN_PASSWORD_LENGTH = 12;
    public static final int MAX_PASSWORD_LENGTH = 256;
    public static final int MAX_EMAIL_LENGTH = 254;

    /**
     * Formato de correo deliberadamente estricto: un solo @, dominio con al
     * menos un punto y sin espacios. Una regexp laxa acabaría aceptando
     * entradas que luego no son direcciones válidas.
     */
    private static final Pattern EMAIL = Pattern.compile(
            "^[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+)*"
                    + "@"
                    + "[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?"
                    + "(?:\\.[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?)+$"
    );

    private static final Pattern LOWERCASE = Pattern.compile("[a-z]");
    private static final Pattern UPPERCASE = Pattern.compile("[A-Z]");
    private static final Pattern DIGIT = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL = Pattern.compile("[^A-Za-z0-9]");
    private static final Pattern WHITESPACE = Pattern.compile("\\s");

    /** Identificador de cada regla. El frontend los usa para pintar la lista. */
    public static final List<Rule> RULES = List.of(
            new Rule("length", "Entre " + MIN_PASSWORD_LENGTH + " y " + MAX_PASSWORD_LENGTH + " caracteres"),
            new Rule("lowercase", "Al menos una letra minúscula"),
            new Rule("uppercase", "Al menos una letra mayúscula"),
            new Rule("digit", "Al menos un número"),
            new Rule("special", "Al menos un carácter especial (!@#$%...)"),
            new Rule("noSpaces", "Sin espacios"),
            new Rule("notEmail", "No puede contener el correo ni su nombre de usuario")
    );

    private CredentialPolicy() {
    }

    /** Normaliza el identificador de acceso: el correo se guarda en minúsculas. */
    public static String normalizeIdentifier(String raw) {
        return raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT);
    }

    public static String normalizeEmail(String raw) {
        return normalizeIdentifier(raw);
    }

    public static boolean isValidEmail(String raw) {
        String email = normalizeEmail(raw);
        return email.length() >= 6
                && email.length() <= MAX_EMAIL_LENGTH
                && EMAIL.matcher(email).matches();
    }

    /** Ids de las reglas que la contraseña cumple. */
    public static Set<String> satisfiedPasswordRules(String rawPassword, String email) {
        String password = rawPassword == null ? "" : rawPassword;
        String normalizedEmail = normalizeEmail(email);
        String localPart = normalizedEmail.contains("@")
                ? normalizedEmail.substring(0, normalizedEmail.indexOf('@'))
                : normalizedEmail;

        Set<String> satisfied = new LinkedHashSet<>();
        if (password.length() >= MIN_PASSWORD_LENGTH && password.length() <= MAX_PASSWORD_LENGTH) {
            satisfied.add("length");
        }
        if (LOWERCASE.matcher(password).find()) {
            satisfied.add("lowercase");
        }
        if (UPPERCASE.matcher(password).find()) {
            satisfied.add("uppercase");
        }
        if (DIGIT.matcher(password).find()) {
            satisfied.add("digit");
        }
        if (SPECIAL.matcher(password).find()) {
            satisfied.add("special");
        }
        if (!WHITESPACE.matcher(password).find()) {
            satisfied.add("noSpaces");
        }
        if (!localPart.isEmpty()
                && !password.toLowerCase(Locale.ROOT).contains(localPart)
                && !password.toLowerCase(Locale.ROOT).contains(normalizedEmail)) {
            satisfied.add("notEmail");
        }
        return satisfied;
    }

    /** Ids de las reglas incumplidas, en el orden en que se muestran. */
    public static List<String> passwordFailures(String rawPassword, String email) {
        Set<String> satisfied = satisfiedPasswordRules(rawPassword, email);
        List<String> failures = new ArrayList<>();
        for (Rule rule : RULES) {
            if (!satisfied.contains(rule.id())) {
                failures.add(rule.id());
            }
        }
        return failures;
    }

    /** Descripciones legibles de las reglas incumplidas, para el mensaje 400. */
    public static List<String> describeFailures(List<String> failureIds) {
        List<String> descriptions = new ArrayList<>();
        for (Rule rule : RULES) {
            if (failureIds.contains(rule.id())) {
                descriptions.add(rule.description().toLowerCase(Locale.ROOT));
            }
        }
        return descriptions;
    }

    public record Rule(String id, String description) {
    }
}
