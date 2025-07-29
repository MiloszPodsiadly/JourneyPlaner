package com.milosz.podsiadly.routeservice.service;

import com.milosz.podsiadly.routeservice.dto.AddressDto;
import com.milosz.podsiadly.routeservice.dto.LocationDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;

@Service
public class RouteService {

    private final RestTemplate restTemplate;
    private final String nominatimBaseUrl;

    public RouteService(RestTemplate restTemplate,
                        @Value("${nominatim.url:https://nominatim.openstreetmap.org}") String nominatimBaseUrl) {
        this.restTemplate = restTemplate;
        this.nominatimBaseUrl = nominatimBaseUrl;
    }

    public LocationDto searchPlace(String query) {
        try {
            String url = String.format("%s/search?format=json&q=%s&limit=1", nominatimBaseUrl, query);
            System.out.println("🌍 Calling Nominatim with URL = " + url);
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "travel-app");
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<List<LocationDto>> response = restTemplate.exchange(
                    URI.create(url),
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<>() {}
            );

            return response.getBody() != null && !response.getBody().isEmpty()
                    ? response.getBody().get(0)
                    : null;

        } catch (Exception ex) {
            ex.printStackTrace(); // Optional: Replace with logger.error(...)
            return null;
        }
    }

}
