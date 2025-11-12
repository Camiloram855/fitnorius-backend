package com.camilo.fitnorius.repository;

import com.camilo.fitnorius.model.ImagesCloud;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ImagesCloudRepository extends JpaRepository<ImagesCloud, Long> {
    Optional<ImagesCloud> findByPublicId(String publicId);
}
