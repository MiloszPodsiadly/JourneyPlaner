package com.milosz.podsiadly.routeservice.service;

import com.milosz.podsiadly.routeservice.dto.TripPlaceView;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;

@Component
public class UserServiceClient {

    private final RestTemplate restTemplate;

    // Na sztywno — service discovery w Dockerze/Eurece i tak znajdzie po nazwie hosta
    private static final String BASE_URL = "http://user-service:8081";

    public UserServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<TripPlaceView> getPlacesForTripPlan(Long tripPlanId, String jwt) {
        String url = BASE_URL + "/api/trip-plans/" + tripPlanId + "/places";

        HttpHeaders headers = new HttpHeaders();
        if (jwt != null && !jwt.isBlank()) {
            headers.setBearerAuth(jwt);
        }

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<List<TripPlaceView>> response = restTemplate.exchange(
                URI.create(url),
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<>() {}
        );

        return response.getBody();
    }
}

