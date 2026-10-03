package com.camilo.fitnorius.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
public class CloudinaryConfig {

    private static final Logger log = LoggerFactory.getLogger(CloudinaryConfig.class);

    @Value("${cloudinary.cloud_name}")
    private String cloudName;

    @Value("${cloudinary.api_key}")
    private String apiKey;

    @Value("${cloudinary.api_secret}")
    private String apiSecret;

    /**
     * Antes de arrancar se avisa si falta configuración. No se detiene el
     * servicio porque el catálogo sigue funcionando sin subir imágenes, pero
     * avisar evita que el primer fallo aparezca como un 500 sin explicación
     * cuando un administrador intenta subir una imagen.
     */
    @PostConstruct
    void warnIfIncomplete() {
        if (isConfigured()) {
            log.info("Cloudinary configurado (cloud_name: {})", cloudName);
            return;
        }

        log.warn("Cloudinary sin configurar: las subidas de imágenes fallarán hasta "
                + "definir CLOUDINARY_CLOUD_NAME, CLOUDINARY_API_KEY y "
                + "CLOUDINARY_API_SECRET en el entorno. Faltan: {}", missingVariables());
    }

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", nullIfBlank(cloudName),
                "api_key", nullIfBlank(apiKey),
                "api_secret", nullIfBlank(apiSecret),
                "secure", true
        ));
    }

    private boolean isConfigured() {
        return StringUtils.hasText(cloudName)
                && StringUtils.hasText(apiKey)
                && StringUtils.hasText(apiSecret);
    }

    /** Nombres de las variables que faltan, nunca sus valores. */
    private String missingVariables() {
        StringBuilder missing = new StringBuilder();
        if (!StringUtils.hasText(cloudName)) {
            missing.append("CLOUDINARY_CLOUD_NAME ");
        }
        if (!StringUtils.hasText(apiKey)) {
            missing.append("CLOUDINARY_API_KEY ");
        }
        if (!StringUtils.hasText(apiSecret)) {
            missing.append("CLOUDINARY_API_SECRET");
        }
        return missing.toString().trim();
    }

    private static String nullIfBlank(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}