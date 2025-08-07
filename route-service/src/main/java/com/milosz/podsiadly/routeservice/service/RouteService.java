package com.milosz.podsiadly.routeservice.service;

import com.milosz.podsiadly.routeservice.dto.AddressDto;
import com.milosz.podsiadly.routeservice.dto.LocationDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
            String url = String.format("%s/search?format=json&q=%s&limit=3", nominatimBaseUrl, query);
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
            ex.printStackTrace();
            return null;
        }
    }
    public List<LocationDto> searchTopPlaces(String city) {
        try {
            String query = "points of interest in " + city;
            String url = String.format("%s/search?format=json&q=%s&limit=5",
                    nominatimBaseUrl,
                    URLEncoder.encode(query, StandardCharsets.UTF_8));

            System.out.println("🌍 Fetching top 5 places from Nominatim with URL = " + url);

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "travel-app");
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<List<LocationDto>> response = restTemplate.exchange(
                    URI.create(url),
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<>() {}
            );

            return response.getBody() != null ? response.getBody() : List.of();

        } catch (Exception ex) {
            ex.printStackTrace();
            return List.of();
        }
    }
    public LocationDto[] searchPlacesByCategory(String city, String category) {
        try {
            String query = String.format("%s in %s", category, city);
            String url = String.format("%s/search?format=json&q=%s&limit=5&addressdetails=1",
                    nominatimBaseUrl,
                    URLEncoder.encode(query, StandardCharsets.UTF_8));

            System.out.println("🔍 Nominatim category search: " + url);

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "travel-app");
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<LocationDto[]> response = restTemplate.exchange(
                    URI.create(url),
                    HttpMethod.GET,
                    entity,
                    LocationDto[].class
            );

            return response.getBody() != null ? response.getBody() : new LocationDto[0];

        } catch (Exception ex) {
            ex.printStackTrace();
            return new LocationDto[0];
        }
    }
}
