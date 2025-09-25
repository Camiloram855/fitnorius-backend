package com.camilo.fitnorius.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
public class WhatsappService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${whatsapp.api.url}")
    private String whatsappApiUrl;

    @Value("${whatsapp.api.token}")
    private String whatsappApiToken;

    @Value("${whatsapp.phone.number}")
    private String whatsappPhoneNumber;

    public void enviarMensaje(String mensaje) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));
            headers.setBearerAuth(whatsappApiToken);

            Map<String, Object> body = Map.of(
                    "messaging_product", "whatsapp",
                    "to", whatsappPhoneNumber,
                    "type", "text",
                    "text", Map.of("body", mensaje)
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    whatsappApiUrl, request, String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("✅ Mensaje enviado a WhatsApp: " + response.getBody());
            } else {
                System.err.println("❌ Error en WhatsApp: " + response.getStatusCode() + " - " + response.getBody());
            }

        } catch (Exception e) {
            System.err.println("❌ Excepción enviando mensaje a WhatsApp: " + e.getMessage());
        }
    }
}
