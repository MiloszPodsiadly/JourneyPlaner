package com.milosz.podsiadly.uiservice.service;


import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class UserClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String userServiceUrl = "http://user-service:8081";

    public void createUserIfNotExists(String spotifyId, String name, String email) {
        String url = userServiceUrl + "/api/users/create";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = Map.of(
                "spotifyId", spotifyId,
                "name", name != null ? name : "Unknown",
                "email", email != null ? email : "unknown@example.com"
        );

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        try {
            restTemplate.postForEntity(url, request, Void.class);
        } catch (Exception e) {
            System.err.println("❌ Nie udało się zarejestrować użytkownika: " + e.getMessage());
        }
    }
}
