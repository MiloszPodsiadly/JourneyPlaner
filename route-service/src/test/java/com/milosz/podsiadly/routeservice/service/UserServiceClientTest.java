package com.milosz.podsiadly.routeservice.service;

import com.milosz.podsiadly.routeservice.dto.TripPlaceView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("UserServiceClient unit tests")
class UserServiceClientTest {

    private RestTemplate restTemplate;
    private UserServiceClient client;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        client = new UserServiceClient(restTemplate);
    }

    private TripPlaceView place(long id, double lat, double lon) {
        try {
            if (!TripPlaceView.class.isRecord()) {
                throw new IllegalStateException("TripPlaceView is not a record – adjust test helper if needed.");
            }
            RecordComponent[] comps = TripPlaceView.class.getRecordComponents();
            Class<?>[] paramTypes = new Class<?>[comps.length];
            Object[] args = new Object[comps.length];

            for (int i = 0; i < comps.length; i++) {
                var c = comps[i];
                paramTypes[i] = c.getType();
                String name = c.getName().toLowerCase();

                if (name.equals("id") && (c.getType() == Long.class || c.getType() == long.class)) {
                    args[i] = id;
                } else if ((name.equals("lat") || name.equals("latitude"))
                        && (c.getType() == double.class || c.getType() == Double.class)) {
                    args[i] = lat;
                } else if ((name.equals("lon") || name.equals("lng") || name.equals("longitude"))
                        && (c.getType() == double.class || c.getType() == Double.class)) {
                    args[i] = lon;
                } else {
                    Class<?> t = c.getType();
                    if (t == String.class) args[i] = null;
                    else if (t == Integer.class || t == int.class) args[i] = 0;
                    else if (t == Long.class || t == long.class) args[i] = 0L;
                    else if (t == Double.class || t == double.class) args[i] = 0.0;
                    else if (t == Boolean.class || t == boolean.class) args[i] = false;
                    else args[i] = null;
                }
            }

            Constructor<TripPlaceView> ctor = TripPlaceView.class.getDeclaredConstructor(paramTypes);
            ctor.setAccessible(true);
            return ctor.newInstance(args);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot construct TripPlaceView – update helper to match record signature.", e);
        }
    }

    @Test
    @DisplayName("getPlacesForTripPlan: sends Bearer token when JWT provided and returns body")
    void getPlaces_withJwt_sendsBearer_andReturnsBody() {
        Long planId = 42L;
        String jwt = "jwt-123";

        List<TripPlaceView> expectedBody = List.of(
                place(10L, 50.0, 19.9),
                place(11L, 50.1, 20.0)
        );

        @SuppressWarnings("unchecked")
        ResponseEntity<List<TripPlaceView>> response =
                (ResponseEntity<List<TripPlaceView>>) (ResponseEntity<?>) ResponseEntity.ok(expectedBody);

        when(restTemplate.exchange(
                any(URI.class),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(response);

        List<TripPlaceView> out = client.getPlacesForTripPlan(planId, jwt);

        ArgumentCaptor<URI> uriCap = ArgumentCaptor.forClass(URI.class);
        ArgumentCaptor<HttpEntity<Void>> entityCap = ArgumentCaptor.forClass(HttpEntity.class);

        verify(restTemplate).exchange(
                uriCap.capture(),
                eq(HttpMethod.GET),
                entityCap.capture(),
                any(ParameterizedTypeReference.class)
        );

        String url = uriCap.getValue().toString();
        assertThat(url).isEqualTo("http://user-service:8081/api/trip-plans/42/places");

        HttpHeaders headers = entityCap.getValue().getHeaders();
        assertThat(headers.getFirst(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer " + jwt);

        assertThat(out).hasSize(2);
        assertThat(out.get(0).id()).isEqualTo(10L);
        assertThat(out.get(1).id()).isEqualTo(11L);
    }

    @Test
    @DisplayName("getPlacesForTripPlan: does NOT set Authorization when JWT is null/blank")
    void getPlaces_withoutJwt_doesNotSetAuthHeader() {
        Long planId = 7L;

        List<TripPlaceView> expectedBody = List.of();
        @SuppressWarnings("unchecked")
        ResponseEntity<List<TripPlaceView>> response =
                (ResponseEntity<List<TripPlaceView>>) (ResponseEntity<?>) ResponseEntity.ok(expectedBody);

        when(restTemplate.exchange(
                any(URI.class),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(response);

        List<TripPlaceView> out1 = client.getPlacesForTripPlan(planId, null);
        List<TripPlaceView> out2 = client.getPlacesForTripPlan(planId, "   ");

        ArgumentCaptor<HttpEntity<Void>> entityCap = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate, times(2)).exchange(
                any(URI.class),
                eq(HttpMethod.GET),
                entityCap.capture(),
                any(ParameterizedTypeReference.class)
        );

        for (HttpEntity<Void> e : entityCap.getAllValues()) {
            HttpHeaders h = e.getHeaders();
            assertThat(h.getFirst(HttpHeaders.AUTHORIZATION)).isNull();
        }

        assertThat(out1).isEmpty();
        assertThat(out2).isEmpty();
    }

    @Test
    @DisplayName("getPlacesForTripPlan: returns null when response body is null")
    void getPlaces_nullBody_returnsNull() {
        @SuppressWarnings("unchecked")
        ResponseEntity<List<TripPlaceView>> response =
                (ResponseEntity<List<TripPlaceView>>) (ResponseEntity<?>) ResponseEntity.ok(null);

        when(restTemplate.exchange(
                any(URI.class),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(response);

        List<TripPlaceView> out = client.getPlacesForTripPlan(1L, "tok");
        assertThat(out).isNull();
    }

    @Test
    @DisplayName("getPlacesForTripPlan: uses GET and empty body entity")
    void getPlaces_usesGetAndEmptyEntity() {
        @SuppressWarnings("unchecked")
        ResponseEntity<List<TripPlaceView>> response =
                (ResponseEntity<List<TripPlaceView>>) (ResponseEntity<?>) ResponseEntity.ok(List.of());

        when(restTemplate.exchange(
                any(URI.class),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(response);

        client.getPlacesForTripPlan(99L, "jwt");

        ArgumentCaptor<HttpMethod> methodCap = ArgumentCaptor.forClass(HttpMethod.class);
        verify(restTemplate).exchange(
                any(URI.class),
                methodCap.capture(),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        );
        assertThat(methodCap.getValue()).isEqualTo(HttpMethod.GET);
    }
}