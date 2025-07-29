package com.milosz.podsiadly.uiservice.security;

import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

@Component
public class SpotifyTokenCache {

    private final RestTemplate restTemplate = new RestTemplate();

    private String accessToken;
    private String refreshToken;
    private Instant expiresAt;

    private final String clientId = "d9a786a96ea44f9bbfa5228457b11589";
    private final String clientSecret = "1c7be3850109498f953b58e9cde315bc";

    public synchronized void update(String accessToken, String refreshToken, int expiresInSeconds) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresAt = Instant.now().plusSeconds(expiresInSeconds - 60); // bufor bezpieczeństwa
    }

    public synchronized String getAccessToken() {
        if (isExpired()) {
            refreshAccessToken();
        }
        return accessToken;
    }

    private boolean isExpired() {
        return accessToken == null || expiresAt == null || Instant.now().isAfter(expiresAt);
    }

    public synchronized void refreshAccessToken() {
        if (refreshToken == null) {
            throw new IllegalStateException("Brak refresh_token — użytkownik musi się ponownie zalogować.");
        }

        String tokenUrl = "https://accounts.spotify.com/api/token";

        // Buduj nagłówki
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        String authValue = clientId + ":" + clientSecret;
        String encodedAuth = Base64.getEncoder().encodeToString(authValue.getBytes(StandardCharsets.UTF_8));
        headers.set("Authorization", "Basic " + encodedAuth);

        // Buduj ciało żądania
        String body = "grant_type=refresh_token&refresh_token=" + refreshToken;
        HttpEntity<String> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    tokenUrl, HttpMethod.POST, request, Map.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> bodyMap = response.getBody();

                this.accessToken = (String) bodyMap.get("access_token");
                Integer expiresIn = (Integer) bodyMap.get("expires_in");
                this.expiresAt = Instant.now().plusSeconds(expiresIn != null ? expiresIn : 3600);

                // Spotify czasem zwraca nowy refresh_token – aktualizuj, jeśli jest
                if (bodyMap.containsKey("refresh_token")) {
                    this.refreshToken = (String) bodyMap.get("refresh_token");
                }

                System.out.println("✅ Access token refreshed.");
            } else {
                throw new RuntimeException("❌ Nie udało się odświeżyć tokenu: " + response.getStatusCode());
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            this.accessToken = null;
            this.expiresAt = null;
            throw new RuntimeException("❌ Błąd podczas odświeżania tokenu: " + ex.getMessage());
        }
    }
}

