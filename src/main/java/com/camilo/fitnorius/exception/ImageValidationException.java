package com.camilo.fitnorius.exception;

/**
 * El archivo enviado no cumple los requisitos de imagen. Se distingue del
 * IllegalArgumentException genérico para poder devolver el motivo concreto
 * ("formato no válido", "supera el tamaño máximo") en vez de un 400 genérico.
 */
public class ImageValidationException extends RuntimeException {

    public ImageValidationException(String message) {
        super(message);
    }

    public ImageValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}