package com.milosz.podsiadly.uiservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Component
public class UserProfileClient {

    private static final Logger log = LoggerFactory.getLogger(UserProfileClient.class);

    private final RestTemplate restTemplate;
    private final String userServiceUrl;

    public UserProfileClient(
            ObjectProvider<RestTemplate> restTemplateProvider,
            @Value("${user-service.url:http://user-service:8081}") String userServiceUrl
    ) {
        RestTemplate rt = restTemplateProvider.getIfAvailable();
        if (rt == null) {
            SimpleClientHttpRequestFactory f = new SimpleClientHttpRequestFactory();
            f.setConnectTimeout(5000);
            f.setReadTimeout(5000);
            rt = new RestTemplate(f);
        }
        this.restTemplate = rt;
        this.userServiceUrl = userServiceUrl;
    }

    public void createProfileIfAbsent(String spotifyId, String displayName, String bio, String avatarUrl) {
        String encodedId = URLEncoder.encode(spotifyId, StandardCharsets.UTF_8);
        String url = userServiceUrl + "/api/user-profiles/" + encodedId + "/ensure";

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));

        Map<String, Object> body = new HashMap<>();
        if (displayName != null && !displayName.isBlank()) body.put("displayName", displayName);
        if (bio != null && !bio.isBlank())                 body.put("bio", bio);
        if (avatarUrl != null && !avatarUrl.isBlank())     body.put("avatarUrl", avatarUrl);

        HttpEntity<?> requestEntity = body.isEmpty()
                ? new HttpEntity<>(headers)
                : new HttpEntity<>(body, withJson(headers));

        try {
            ResponseEntity<Void> resp = restTemplate.postForEntity(url, requestEntity, Void.class);
            if (!resp.getStatusCode().is2xxSuccessful()) {
                log.warn("Profile ensure call returned non-2xx: {}", resp.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Failed to ensure user profile for spotifyId={}: {}", spotifyId, e.getMessage());
        }
    }

    private HttpHeaders withJson(HttpHeaders headers) {
        HttpHeaders h = new HttpHeaders();
        h.putAll(headers);
        h.setContentType(MediaType.APPLICATION_JSON);
        return h;
    }
}
