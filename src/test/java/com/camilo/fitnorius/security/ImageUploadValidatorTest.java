package com.camilo.fitnorius.security;

import com.camilo.fitnorius.exception.ImageValidationException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ImageUploadValidatorTest {

    @Test
    void acceptsARealImageSignature() {
        byte[] pngHeader = new byte[]{
                (byte) 0x89, 'P', 'N', 'G', 0x0d, 0x0a, 0x1a, 0x0a,
                0, 0, 0, 0
        };
        MockMultipartFile file = new MockMultipartFile(
                "file", "photo.png", "image/png", pngHeader
        );

        assertDoesNotThrow(() -> ImageUploadValidator.validate(file));
    }

    @Test
    void rejectsAFileThatOnlyClaimsToBeAnImage() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "photo.png", "image/png", "<script>alert(1)</script>".getBytes()
        );

        assertThrows(ImageValidationException.class, () -> ImageUploadValidator.validate(file));
    }
}
