package com.camilo.fitnorius.exception;

/** Ya existe un usuario registrado con ese correo. */
public class EmailAlreadyRegisteredException extends RuntimeException {

    public EmailAlreadyRegisteredException() {
        super("Ya existe un usuario registrado con ese correo");
    }
}
