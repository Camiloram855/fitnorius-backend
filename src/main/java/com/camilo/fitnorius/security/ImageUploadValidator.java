package com.camilo.fitnorius.security;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Set;

/** Valida archivos de imagen antes de enviarlos a almacenamiento externo. */
public final class ImageUploadValidator {

    private static final long MAX_BYTES = 10L * 1024 * 1024;
    private static final Set<String> CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif", "image/avif"
    );
    private static final Set<String> EXTENSIONS = Set.of(
            ".jpg", ".jpeg", ".png", ".webp", ".gif", ".avif"
    );

    private ImageUploadValidator() {
    }

    public static void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("El archivo de imagen es obligatorio");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new IllegalArgumentException("La imagen supera el tamaño máximo permitido");
        }

        String contentType = file.getContentType() == null
                ? ""
                : file.getContentType().toLowerCase(Locale.ROOT);
        String filename = file.getOriginalFilename() == null
                ? ""
                : file.getOriginalFilename().toLowerCase(Locale.ROOT);
        boolean validExtension = EXTENSIONS.stream().anyMatch(filename::endsWith);
        if (!CONTENT_TYPES.contains(contentType) || !validExtension) {
            throw new IllegalArgumentException("El archivo debe ser una imagen compatible");
        }

        try (InputStream inputStream = file.getInputStream()) {
            byte[] header = inputStream.readNBytes(12);
            if (!hasImageSignature(header)) {
                throw new IllegalArgumentException("El contenido del archivo no es una imagen válida");
            }
        } catch (IOException exception) {
            throw new IllegalArgumentException("No se pudo validar el archivo de imagen", exception);
        }
    }

    private static boolean hasImageSignature(byte[] bytes) {
        if (bytes.length >= 3
                && (bytes[0] & 0xff) == 0xff
                && (bytes[1] & 0xff) == 0xd8
                && (bytes[2] & 0xff) == 0xff) {
            return true; // JPEG
        }
        if (bytes.length >= 8
                && (bytes[0] & 0xff) == 0x89
                && bytes[1] == 'P' && bytes[2] == 'N' && bytes[3] == 'G'
                && bytes[4] == 0x0d && bytes[5] == 0x0a
                && bytes[6] == 0x1a && bytes[7] == 0x0a) {
            return true; // PNG
        }
        if (bytes.length >= 6
                && bytes[0] == 'G' && bytes[1] == 'I' && bytes[2] == 'F'
                && bytes[3] == '8') {
            return true; // GIF
        }
        if (bytes.length >= 12
                && bytes[0] == 'R' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == 'F'
                && bytes[8] == 'W' && bytes[9] == 'E' && bytes[10] == 'B' && bytes[11] == 'P') {
            return true; // WEBP
        }
        if (bytes.length >= 12
                && bytes[4] == 'f' && bytes[5] == 't' && bytes[6] == 'y' && bytes[7] == 'p') {
            String brand = new String(bytes, 8, 4, java.nio.charset.StandardCharsets.US_ASCII);
            return "avif".equals(brand) || "avis".equals(brand);
        }
        return false;
    }
}
