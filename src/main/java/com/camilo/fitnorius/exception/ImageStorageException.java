package com.camilo.fitnorius.exception;

/**
 * Fallo al guardar una imagen en el almacenamiento externo. Se distingue de
 * un error genérico para poder devolver un mensaje accionable al administrador
 * en vez de "ocurrió un error inesperado".
 */
public class ImageStorageException extends RuntimeException {

    public ImageStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}