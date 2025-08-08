package com.milosz.podsiadly.uiservice.service;


import com.milosz.podsiadly.uiservice.dto.RouteResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

@Component
public class JourneyClient {

    private final RestTemplate restTemplate;
    private static final String BASE_URL = "http://route-service:8083";

    public JourneyClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public RouteResponse createRoute(Long tripPlanId, boolean optimize, String jwt) {
        String url = BASE_URL + "/api/osrm/route?tripPlanId=" + tripPlanId + "&optimize=" + optimize;

        HttpHeaders headers = new HttpHeaders();
        if (jwt != null && !jwt.isBlank()) {
            headers.setBearerAuth(jwt);
        }

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<RouteResponse> response = restTemplate.exchange(
                URI.create(url),
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<>() {}
        );

        return response.getBody();
    }
}

