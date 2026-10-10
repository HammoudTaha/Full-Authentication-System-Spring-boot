package com.example.first_project.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.http.MediaType;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class SMSService {
    private final RestClient restClient;
    private static final Logger log = LoggerFactory.getLogger(SMSService.class);

    @Value("${textbee.api-key}")
    private String apiKey;

    @Value("${textbee.base-url}")
    private String apiUrl;

    @Value("${textbee.device-id}")
    private String deviceId;

    public SMSService(RestClient restClient) {
        this.restClient = restClient;
    }

    public void sendMessage(String phone, String message) {
        try {
            String finalUrl = apiUrl + deviceId + "/send-sms";
            Map<String, Object> requestBody = Map.of(
                    "recipients", List.of(phone),
                    "message", message);
            restClient.post()
                    .uri(finalUrl)
                    .header("x-api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .toBodilessEntity();
            log.info("SMS sent successfully in background to: {}", phone);
        } catch (Exception e) {
            log.error("Failed to send OTP code to {}. Error: {}", phone, e.getMessage());
        }
    }
}
