package com.camilo.fitnorius.exception;

/** La contraseña no cumple la política de credenciales. */
public class WeakPasswordException extends RuntimeException {

    private final String failedRules;

    public WeakPasswordException(String message, String failedRules) {
        super(message);
        this.failedRules = failedRules;
    }

    public String getFailedRules() {
        return failedRules;
    }
}
