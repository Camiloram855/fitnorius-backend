package com.camilo.fitnorius.service;

import com.camilo.fitnorius.exception.ImageStorageException;
import com.camilo.fitnorius.model.Category;
import com.camilo.fitnorius.repository.CategoryRepository;
import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * El mensaje de Cloudinary es lo único que explica el fallo, así que la cadena
 * de causas no puede perderse por el camino.
 */
class CategoryServiceImageFailureTest {

    private static final byte[] PNG_HEADER = {
            (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
    };

    @Test
    void keepsTheCloudinaryMessageInTheRootCause() throws IOException {
        Cloudinary cloudinary = mock(Cloudinary.class);
        Uploader uploader = mock(Uploader.class);
        when(cloudinary.uploader()).thenReturn(uploader);

        RuntimeException cloudinaryError = new RuntimeException("cloud_name mismatch");
        when(uploader.upload(any(byte[].class), any())).thenThrow(cloudinaryError);

        CategoryService service = new CategoryService(
                mock(CategoryRepository.class),
                mock(com.camilo.fitnorius.repository.ProductRepository.class),
                cloudinary
        );

        MultipartFile image = new MockMultipartFile(
                "file", "categoria.png", "image/png", PNG_HEADER);

        ImageStorageException thrown = assertThrows(ImageStorageException.class,
                () -> service.createCategory("lkjh", image));

        // La causa raiz debe seguir siendo el error original de Cloudinary.
        assertSame(cloudinaryError, thrown.getCause());
        assertEquals("cloud_name mismatch", thrown.getCause().getMessage());
    }

    @Test
    void createsTheCategoryWhenCloudinaryAcceptsTheImage() throws IOException {
        Cloudinary cloudinary = mock(Cloudinary.class);
        Uploader uploader = mock(Uploader.class);
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), any())).thenReturn(
                java.util.Map.of(
                        "secure_url", "https://res.cloudinary.com/demo/image/upload/x.png",
                        "public_id", "fitnorius/categories/x"
                ));

        CategoryRepository repository = mock(CategoryRepository.class);
        when(repository.save(any(Category.class))).thenAnswer(i -> i.getArgument(0));

        CategoryService service = new CategoryService(
                repository,
                mock(com.camilo.fitnorius.repository.ProductRepository.class),
                cloudinary
        );

        MultipartFile image = new MockMultipartFile(
                "file", "categoria.png", "image/png", PNG_HEADER);

        Category created = service.createCategory("Bandas", image);

        assertEquals("Bandas", created.getName());
    }
}