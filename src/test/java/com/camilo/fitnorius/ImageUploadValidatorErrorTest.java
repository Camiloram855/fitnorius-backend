package com.camilo.fitnorius;

import com.camilo.fitnorius.exception.ImageValidationException;
import com.camilo.fitnorius.security.ImageUploadValidator;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * El motivo del rechazo debe llegar al administrador, no un 400 genérico.
 */
class ImageUploadValidatorErrorTest {

    private static final byte[] PNG_HEADER = {
            (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
    };

    private static MultipartFile file(String name, String contentType, byte[] content) {
        return new MockMultipartFile("file", name, contentType, content);
    }

    @Test
    void acceptsAPngWithAValidSignature() {
        assertDoesNotThrow(() -> ImageUploadValidator.validate(
                file("banner.png", "image/png", PNG_HEADER)));
    }

    @Test
    void explainsWhenTheExtensionIsNotAllowed() {
        ImageValidationException failure = assertThrows(ImageValidationException.class,
                () -> ImageUploadValidator.validate(file("logo.svg", "image/svg+xml", PNG_HEADER)));

        assertEquals("El archivo debe ser una imagen compatible", failure.getMessage());
    }

    @Test
    void explainsWhenTheContentIsNotReallyAnImage() {
        // Extensión y content-type correctos, pero el archivo no es una imagen.
        ImageValidationException failure = assertThrows(ImageValidationException.class,
                () -> ImageUploadValidator.validate(
                        file("trampa.png", "image/png", "texto plano".getBytes(StandardCharsets.UTF_8))));

        assertEquals("El contenido del archivo no es una imagen válida", failure.getMessage());
    }

    @Test
    void explainsWhenTheFileIsTooLarge() {
        byte[] large = new byte[10 * 1024 * 1024 + 1];
        System.arraycopy(PNG_HEADER, 0, large, 0, PNG_HEADER.length);

        ImageValidationException failure = assertThrows(ImageValidationException.class,
                () -> ImageUploadValidator.validate(file("grande.png", "image/png", large)));

        assertEquals("La imagen supera el tamaño máximo permitido", failure.getMessage());
    }

    @Test
    void rejectsAnOctetStreamContentTypeEvenWithAPngExtension() {
        // Así llega el archivo cuando el navegador no reconoce el tipo.
        ImageValidationException failure = assertThrows(ImageValidationException.class,
                () -> ImageUploadValidator.validate(
                        file("imagen.png", "application/octet-stream", PNG_HEADER)));

        assertEquals("El archivo debe ser una imagen compatible", failure.getMessage());
    }
}