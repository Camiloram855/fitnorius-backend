package com.camilo.fitnorius.service;

import com.camilo.fitnorius.model.ImagesCloud;
import com.camilo.fitnorius.repository.ImagesCloudRepository;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ImagesCloudService {

    private final Cloudinary cloudinary;
    private final ImagesCloudRepository repository;

    public ImagesCloud upload(MultipartFile file) throws IOException {
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());

        ImagesCloud image = ImagesCloud.builder()
                .publicId((String) uploadResult.get("public_id"))
                .secureUrl((String) uploadResult.get("secure_url"))
                .format((String) uploadResult.get("format"))
                .width((Integer) uploadResult.get("width"))
                .height((Integer) uploadResult.get("height"))
                .bytes(Long.valueOf(uploadResult.get("bytes").toString()))
                .createdAt((String) uploadResult.get("created_at"))
                .build();

        return repository.save(image);
    }

    public void delete(String publicId) throws IOException {
        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        repository.findByPublicId(publicId).ifPresent(repository::delete);
    }

    public java.util.List<ImagesCloud> findAll() {
        return repository.findAll();
    }
}
