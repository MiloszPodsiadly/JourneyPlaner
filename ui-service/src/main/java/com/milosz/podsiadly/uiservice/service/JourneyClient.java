package com.milosz.podsiadly.uiservice.service;

import com.milosz.podsiadly.uiservice.dto.RouteResponse;
import com.milosz.podsiadly.uiservice.dto.MultiModeRouteResponse;
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
        HttpEntity<Void> entity = new HttpEntity<>(buildHeaders(jwt));

        ResponseEntity<RouteResponse> response = restTemplate.exchange(
                URI.create(url),
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<>() {}
        );
        return response.getBody();
    }

    public RouteResponse routeByMode(Long tripPlanId, String mode, String jwt) {
        String m = (mode == null) ? "" : mode.trim().toLowerCase();
        if (!m.equals("driving") && !m.equals("walking") && !m.equals("cycling")) {
            throw new IllegalArgumentException("Unsupported mode: " + mode);
        }
        String url = BASE_URL + "/api/osrm/route/" + m + "?tripPlanId=" + tripPlanId;
        HttpEntity<Void> entity = new HttpEntity<>(buildHeaders(jwt));

        ResponseEntity<RouteResponse> response = restTemplate.exchange(
                URI.create(url),
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<>() {}
        );
        return response.getBody();
    }

    public MultiModeRouteResponse routeAllModes(Long tripPlanId, String jwt) {
        String url = BASE_URL + "/api/osrm/route/modes?tripPlanId=" + tripPlanId;
        HttpEntity<Void> entity = new HttpEntity<>(buildHeaders(jwt));

        ResponseEntity<MultiModeRouteResponse> response = restTemplate.exchange(
                URI.create(url),
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<>() {}
        );
        return response.getBody();
    }

    private HttpHeaders buildHeaders(String jwt) {
        HttpHeaders headers = new HttpHeaders();
        if (jwt != null && !jwt.isBlank()) {
            headers.setBearerAuth(jwt);
        }
        headers.set("User-Agent", "travel-app-ui");
        return headers;
    }
}
