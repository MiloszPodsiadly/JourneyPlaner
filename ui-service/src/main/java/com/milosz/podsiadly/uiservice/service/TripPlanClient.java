package com.milosz.podsiadly.uiservice.service;

import com.milosz.podsiadly.uiservice.dto.TripPlanDto;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Component
public class TripPlanClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String userServiceUrl = "http://user-service:8081";

    public List<TripPlanDto> getUserPlans(String spotifyId, String token) {
        String url = userServiceUrl + "/api/trip-plans/user?spotifyId=" + encode(spotifyId);

        HttpHeaders headers = headersWithAuth(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<TripPlanDto[]> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, TripPlanDto[].class);

        return List.of(response.getBody());
    }

    public TripPlanDto createPlan(String spotifyId, String name, String desc, String token) {
        String url = userServiceUrl + "/api/trip-plans/create";

        HttpHeaders headers = headersWithAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = Map.of(
                "spotifyId", spotifyId,
                "name", name,
                "description", desc
        );

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

        return restTemplate.exchange(url, HttpMethod.POST, entity, TripPlanDto.class).getBody();
    }

    public void addPlace(Long planId, String displayName, double lat, double lon, String token) {
        String url = userServiceUrl + "/api/trip-plans/" + planId + "/add-place";

        HttpHeaders headers = headersWithAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "displayName", displayName,
                "lat", lat,
                "lon", lon
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);
    }

    public void addPlaylist(Long planId, String playlistId, String name, String token) {
        String url = userServiceUrl + "/api/trip-plans/" + planId + "/add-playlist";

        HttpHeaders headers = headersWithAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = Map.of(
                "playlistId", playlistId,
                "name", name
        );

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);
        restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);
    }

    public void deletePlan(Long planId, String token) {
        String url = userServiceUrl + "/api/trip-plans/" + planId;
        HttpHeaders headers = headersWithAuth(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);
    }

    public void deletePlace(Long placeId, String token) {
        String url = userServiceUrl + "/api/trip-plans/place/" + placeId;
        HttpHeaders headers = headersWithAuth(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);
    }

    public void deletePlaylist(Long playlistId, String token) {
        String url = userServiceUrl + "/api/trip-plans/playlist/" + playlistId;
        HttpHeaders headers = headersWithAuth(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);
    }

    public void updatePlan(Long planId, String name, String desc, String token) {
        String url = userServiceUrl + "/api/trip-plans/" + planId + "/update";

        HttpHeaders headers = headersWithAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = Map.of("name", name, "description", desc);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

        restTemplate.exchange(url, HttpMethod.PUT, entity, Void.class);
    }

    private HttpHeaders headersWithAuth(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "travel-app");
        headers.setBearerAuth(token);
        return headers;
    }

    private String encode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }
}
