package com.milosz.podsiadly.routeservice.service;

import com.milosz.podsiadly.routeservice.service.OsrmClient.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("OsrmClient unit tests (RestTemplate mocked)")
class OsrmClientTest {

    private RestTemplate restTemplate;
    private OsrmClient client;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        client = new OsrmClient(restTemplate);
    }

    private static RouteResponse okRoute(double distance, double duration, GeoJson geom) {
        Route r = new Route();
        r.distance = distance;
        r.duration = duration;
        r.geometry = geom;

        RouteResponse rr = new RouteResponse();
        rr.code = "Ok";
        rr.routes = List.of(r);
        return rr;
    }

    private static TripResponse okTrip(double distance, double duration, GeoJson geom, List<Waypoint> wps) {
        Trip t = new Trip();
        t.distance = distance;
        t.duration = duration;
        t.geometry = geom;

        TripResponse tr = new TripResponse();
        tr.code = "Ok";
        tr.trips = List.of(t);
        tr.waypoints = wps;
        return tr;
    }

    private static GeoJson geo() {
        GeoJson g = new GeoJson();
        g.type = "LineString";
        g.coordinates = List.of(
                List.of(19.9, 50.0),
                List.of(20.0, 50.1)
        );
        return g;
    }

    private static Waypoint wp(int idx, String name, double lon, double lat) {
        Waypoint w = new Waypoint();
        w.waypoint_index = idx;
        w.name = name;
        w.location = new double[]{lon, lat};
        return w;
    }

    private static List<double[]> coords(double... valuesLonLatAlt) {
        List<double[]> out = new ArrayList<>();
        for (int i = 0; i + 1 < valuesLonLatAlt.length; i += 2) {
            out.add(new double[]{valuesLonLatAlt[i], valuesLonLatAlt[i + 1]});
        }
        return out;
    }

    @Test
    @DisplayName("routeKeepOrderDriving builds correct URL and maps response")
    void routeKeepOrderDriving_ok() {
        var input = coords(50.0, 19.9, 50.1, 20.0);
        var body = okRoute(123.4, 56.7, geo());
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), eq(HttpEntity.EMPTY), eq(RouteResponse.class)))
                .thenReturn(ResponseEntity.ok(body));

        var res = client.routeKeepOrderDriving(input);

        ArgumentCaptor<URI> uriCap = ArgumentCaptor.forClass(URI.class);
        verify(restTemplate).exchange(uriCap.capture(), eq(HttpMethod.GET), eq(HttpEntity.EMPTY), eq(RouteResponse.class));
        String url = uriCap.getValue().toString();

        assertThat(url)
                .contains("/route/v1/driving/")
                .contains("19.9,50.0;20.0,50.1")
                .contains("geometries=geojson")
                .contains("overview=full");

        assertThat(res.distance()).isEqualTo(123.4);
        assertThat(res.duration()).isEqualTo(56.7);
        assertThat(res.geometry()).isNotNull();
        assertThat(res.waypoints()).isNull();
    }

    @Test
    @DisplayName("routeKeepOrderWalking builds correct URL and maps response")
    void routeKeepOrderWalking_ok() {
        var input = coords(10.0, 11.0, 12.0, 13.0);
        var body = okRoute(5.5, 6.6, null);
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), eq(HttpEntity.EMPTY), eq(RouteResponse.class)))
                .thenReturn(ResponseEntity.ok(body));

        var res = client.routeKeepOrderWalking(input);

        ArgumentCaptor<URI> uriCap = ArgumentCaptor.forClass(URI.class);
        verify(restTemplate).exchange(uriCap.capture(), eq(HttpMethod.GET), eq(HttpEntity.EMPTY), eq(RouteResponse.class));
        String url = uriCap.getValue().toString();

        assertThat(url).contains("/route/v1/walking/");
        assertThat(url).contains("11.0,10.0;13.0,12.0");

        assertThat(res.distance()).isEqualTo(5.5);
        assertThat(res.duration()).isEqualTo(6.6);
        assertThat(res.geometry()).isNull();
        assertThat(res.waypoints()).isNull();
    }

    @Test
    @DisplayName("routeKeepOrderCycling builds correct URL and maps response")
    void routeKeepOrderCycling_ok() {
        var input = coords(0.0, 1.0, 2.0, 3.0);
        var body = okRoute(1.1, 2.2, null);
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), eq(HttpEntity.EMPTY), eq(RouteResponse.class)))
                .thenReturn(ResponseEntity.ok(body));

        var res = client.routeKeepOrderCycling(input);

        ArgumentCaptor<URI> uriCap = ArgumentCaptor.forClass(URI.class);
        verify(restTemplate).exchange(uriCap.capture(), eq(HttpMethod.GET), eq(HttpEntity.EMPTY), eq(RouteResponse.class));
        String url = uriCap.getValue().toString();

        assertThat(url).contains("/route/v1/cycling/");
        assertThat(url).contains("1.0,0.0;3.0,2.0");

        assertThat(res.distance()).isEqualTo(1.1);
        assertThat(res.duration()).isEqualTo(2.2);
    }

    @Test
    @DisplayName("routeKeepOrder (default driving) builds driving URL and maps response")
    void routeKeepOrder_defaultDriving_ok() {
        var input = coords(50.0, 19.9, 51.0, 20.5);
        var body = okRoute(10.0, 20.0, null);
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), eq(HttpEntity.EMPTY), eq(RouteResponse.class)))
                .thenReturn(ResponseEntity.ok(body));

        var res = client.routeKeepOrder(input);

        ArgumentCaptor<URI> uriCap = ArgumentCaptor.forClass(URI.class);
        verify(restTemplate).exchange(uriCap.capture(), eq(HttpMethod.GET), eq(HttpEntity.EMPTY), eq(RouteResponse.class));
        String url = uriCap.getValue().toString();

        assertThat(url).contains("/route/v1/driving/");
        assertThat(url).contains("19.9,50.0;20.5,51.0");

        assertThat(res.distance()).isEqualTo(10.0);
        assertThat(res.duration()).isEqualTo(20.0);
        assertThat(res.waypoints()).isNull();
    }

    @Test
    @DisplayName("tripOptimize builds correct URL and maps trips + waypoints")
    void tripOptimize_ok() {
        var input = coords(50.0, 19.9, 50.1, 20.0, 50.2, 20.1);
        var wps = List.of(
                wp(2, "C", 20.1, 50.2),
                wp(0, "A", 19.9, 50.0),
                wp(1, "B", 20.0, 50.1)
        );
        var body = okTrip(123.0, 456.0, geo(), wps);

        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), eq(HttpEntity.EMPTY), eq(TripResponse.class)))
                .thenReturn(ResponseEntity.ok(body));

        var res = client.tripOptimize(input);

        ArgumentCaptor<URI> uriCap = ArgumentCaptor.forClass(URI.class);
        verify(restTemplate).exchange(uriCap.capture(), eq(HttpMethod.GET), eq(HttpEntity.EMPTY), eq(TripResponse.class));
        String url = uriCap.getValue().toString();

        assertThat(url)
                .contains("/trip/v1/driving/")
                .contains("19.9,50.0;20.0,50.1;20.1,50.2")
                .contains("roundtrip=false")
                .contains("source=first")
                .contains("destination=last")
                .contains("geometries=geojson")
                .contains("overview=full");

        assertThat(res.distance()).isEqualTo(123.0);
        assertThat(res.duration()).isEqualTo(456.0);
        assertThat(res.geometry()).isNotNull();
        assertThat(res.waypoints()).hasSize(3);
        assertThat(res.waypoints().get(0).waypoint_index).isEqualTo(2);
        assertThat(res.waypoints().get(1).waypoint_index).isEqualTo(0);
        assertThat(res.waypoints().get(2).waypoint_index).isEqualTo(1);
    }

    @Test
    @DisplayName("routeKeepOrderDriving: throws when body is null")
    void routeKeepOrderDriving_nullBody() {
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), eq(HttpEntity.EMPTY), eq(RouteResponse.class)))
                .thenReturn(ResponseEntity.ok(null));

        assertThatThrownBy(() -> client.routeKeepOrderDriving(coords(50.0, 19.9, 50.1, 20.0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("OSRM route failed");
    }

    @Test
    @DisplayName("routeKeepOrder: throws when code != Ok")
    void routeKeepOrder_nonOkCode() {
        RouteResponse rr = new RouteResponse();
        rr.code = "Error";
        rr.routes = List.of();

        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), eq(HttpEntity.EMPTY), eq(RouteResponse.class)))
                .thenReturn(ResponseEntity.ok(rr));

        assertThatThrownBy(() -> client.routeKeepOrder(coords(50.0, 19.9, 50.1, 20.0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("OSRM route failed");
    }

    @Test
    @DisplayName("routeKeepOrderWalking: throws when routes empty")
    void routeKeepOrderWalking_emptyRoutes() {
        RouteResponse rr = new RouteResponse();
        rr.code = "Ok";
        rr.routes = List.of();

        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), eq(HttpEntity.EMPTY), eq(RouteResponse.class)))
                .thenReturn(ResponseEntity.ok(rr));

        assertThatThrownBy(() -> client.routeKeepOrderWalking(coords(1.0, 2.0, 3.0, 4.0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("OSRM route failed");
    }

    @Test
    @DisplayName("tripOptimize: throws when body is null")
    void tripOptimize_nullBody() {
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), eq(HttpEntity.EMPTY), eq(TripResponse.class)))
                .thenReturn(ResponseEntity.ok(null));

        assertThatThrownBy(() -> client.tripOptimize(coords(50.0, 19.9, 50.1, 20.0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("OSRM trip failed");
    }

    @Test
    @DisplayName("tripOptimize: throws when code != Ok")
    void tripOptimize_nonOk() {
        TripResponse tr = new TripResponse();
        tr.code = "Nope";
        tr.trips = List.of();

        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), eq(HttpEntity.EMPTY), eq(TripResponse.class)))
                .thenReturn(ResponseEntity.ok(tr));

        assertThatThrownBy(() -> client.tripOptimize(coords(50.0, 19.9, 50.1, 20.0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("OSRM trip failed");
    }

    @Test
    @DisplayName("tripOptimize: throws when trips empty")
    void tripOptimize_emptyTrips() {
        TripResponse tr = new TripResponse();
        tr.code = "Ok";
        tr.trips = List.of();
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), eq(HttpEntity.EMPTY), eq(TripResponse.class)))
                .thenReturn(ResponseEntity.ok(tr));

        assertThatThrownBy(() -> client.tripOptimize(coords(50.0, 19.9, 50.1, 20.0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("OSRM trip failed");
    }
}