package com.shreek.resumebuilderapi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailService {

    @Value("${brevo.api.key}")
    private String brevoApiKey;

    @Value("${brevo.from.email}")
    private String fromEmail;

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String BREVO_URL = "https://api.brevo.com/v3/smtp/email";
    private void send(Map<String, Object> body) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.set("api-key", brevoApiKey);

        HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response =
                    restTemplate.postForEntity(BREVO_URL, request, String.class);

            System.out.println("✅ Email sent | Status: " + response.getStatusCode());

        } catch (HttpStatusCodeException ex) {
            System.err.println("❌ Brevo API Error: " + ex.getStatusCode());
            System.err.println("❌ Brevo Response: " + ex.getResponseBodyAsString());
            throw new RuntimeException("Brevo email failed", ex);
        }
    }

    public void sendEmail(String to, String subject, String htmlContent) {

        Map<String, Object> body = new HashMap<>();
        body.put("sender", Map.of("email", fromEmail));
        body.put("to", List.of(Map.of("email", to)));
        body.put("subject", subject);
        body.put("htmlContent", htmlContent);

        send(body);
    }

    public void sendEmailWithAttachment(
            String to,
            String subject,
            String htmlContent,
            byte[] fileBytes,
            String fileName
    ) {

        String base64File = Base64.getEncoder().encodeToString(fileBytes);

        Map<String, Object> body = new HashMap<>();
        body.put("sender", Map.of("email", fromEmail));
        body.put("to", List.of(Map.of("email", to)));
        body.put("subject", subject);
        body.put("htmlContent", htmlContent);

        body.put("attachment", List.of(
                Map.of(
                        "content", base64File,
                        "name", fileName
                )
        ));

        send(body);
    }
}
