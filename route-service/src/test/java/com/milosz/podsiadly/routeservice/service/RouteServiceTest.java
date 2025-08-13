package com.milosz.podsiadly.routeservice.service;


import com.milosz.podsiadly.routeservice.dto.LocationDto;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("RouteService Unit Tests")
class RouteServiceTest {

    private RestTemplate restTemplate;
    private RouteService routeService;

    private final String baseUrl = "https://nominatim.openstreetmap.org";

    @BeforeAll
    static void beforeAll() {
        System.out.println("🔧 [BeforeAll] Starting RouteService tests...");
    }

    @AfterAll
    static void afterAll() {
        System.out.println("✅ [AfterAll] RouteService tests completed.");
    }

    @BeforeEach
    void setUp() {
        System.out.println("➡️ [BeforeEach] Setting up mocks...");
        restTemplate = mock(RestTemplate.class);
        routeService = new RouteService(restTemplate, baseUrl);
    }

    @AfterEach
    void tearDown() {
        System.out.println("⬅️ [AfterEach] Cleaning up...");
        restTemplate = null;
        routeService = null;
    }

    @Test
    @DisplayName("🟢 searchPlace: returns first result and sets User-Agent header")
    void searchPlace_returnsFirstResult() {
        var dto1 = mock(LocationDto.class);
        var body = List.of(dto1, mock(LocationDto.class));
        var response = new ResponseEntity<List<LocationDto>>(body, HttpStatus.OK);

        ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
        ArgumentCaptor<HttpEntity<Void>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);

        when(restTemplate.exchange(
                uriCaptor.capture(),
                eq(HttpMethod.GET),
                entityCaptor.capture(),
                any(ParameterizedTypeReference.class))
        ).thenReturn(response);

        var result = routeService.searchPlace("Warsaw");

        assertThat(result).isSameAs(dto1);

        assertThat(uriCaptor.getValue().toString())
                .isEqualTo(baseUrl + "/search?format=json&q=Warsaw&limit=3");

        HttpHeaders headers = entityCaptor.getValue().getHeaders();
        assertThat(headers.getFirst("User-Agent")).isEqualTo("travel-app");
    }

    @Test
    @DisplayName("🟡 searchPlace: returns null when body is empty")
    void searchPlace_returnsNullOnEmptyBody() {
        var response = new ResponseEntity<List<LocationDto>>(List.of(), HttpStatus.OK);

        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenReturn(response);

        var result = routeService.searchPlace("Nowhere");
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("🔴 searchPlace: returns null when exception occurs")
    void searchPlace_returnsNullOnException() {
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenThrow(new RuntimeException("boom"));

        var result = routeService.searchPlace("Crash");
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("🟢 searchTopPlaces: encodes query and returns list")
    void searchTopPlaces_encodesQueryAndReturnsList() {
        var dtoA = mock(LocationDto.class);
        var dtoB = mock(LocationDto.class);
        var response = new ResponseEntity<List<LocationDto>>(List.of(dtoA, dtoB), HttpStatus.OK);

        ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
        ArgumentCaptor<HttpEntity<Void>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);

        when(restTemplate.exchange(
                uriCaptor.capture(),
                eq(HttpMethod.GET),
                entityCaptor.capture(),
                any(ParameterizedTypeReference.class))
        ).thenReturn(response);

        var result = routeService.searchTopPlaces("Kraków"); // diakrytyki sprawdzą encoding

        assertThat(result).containsExactly(dtoA, dtoB);

        String uri = uriCaptor.getValue().toString();
        assertThat(uri).startsWith(baseUrl + "/search?format=json&q=");
        assertThat(uri).contains("points+of+interest+in+Krak%C3%B3w");
        assertThat(uri).endsWith("&limit=10");

        HttpHeaders headers = entityCaptor.getValue().getHeaders();
        assertThat(headers.getFirst("User-Agent")).isEqualTo("travel-app");
    }

    @Test
    @DisplayName("🟡 searchTopPlaces: returns empty list on exception")
    void searchTopPlaces_returnsEmptyOnException() {
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenThrow(new RuntimeException("boom"));

        var result = routeService.searchTopPlaces("City");
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("🟢 searchPlacesByCategory: encodes query and returns array")
    void searchPlacesByCategory_encodesQueryAndReturnsArray() {
        LocationDto[] body = new LocationDto[] { mock(LocationDto.class), mock(LocationDto.class) };
        var response = new ResponseEntity<>(body, HttpStatus.OK);

        ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
        ArgumentCaptor<HttpEntity<Void>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);

        when(restTemplate.exchange(
                uriCaptor.capture(),
                eq(HttpMethod.GET),
                entityCaptor.capture(),
                eq(LocationDto[].class))
        ).thenReturn(response);

        var result = routeService.searchPlacesByCategory("Gdansk", "museums & galleries");

        assertThat(result).hasSize(2);

        String uri = uriCaptor.getValue().toString();
        assertThat(uri).startsWith(baseUrl + "/search?format=json&q=");
        assertThat(uri).contains("museums+%26+galleries+in+Gdansk");
        assertThat(uri).contains("&limit=10&addressdetails=1");

        HttpHeaders headers = entityCaptor.getValue().getHeaders();
        assertThat(headers.getFirst("User-Agent")).isEqualTo("travel-app");
    }

    @Test
    @DisplayName("🟡 searchPlacesByCategory: returns empty array on exception")
    void searchPlacesByCategory_returnsEmptyArrayOnException() {
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(LocationDto[].class)))
                .thenThrow(new RuntimeException("boom"));

        var result = routeService.searchPlacesByCategory("City", "category");
        assertThat(result).isNotNull().isEmpty();
    }
}

