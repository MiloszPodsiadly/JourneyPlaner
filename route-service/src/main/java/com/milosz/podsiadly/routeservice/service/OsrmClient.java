package com.milosz.podsiadly.routeservice.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;

@Component
public class OsrmClient {

    private final RestTemplate restTemplate;

    private static final String BASE_URL = "https://router.project-osrm.org";

    public OsrmClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    private enum Profile {
        DRIVING("driving"),
        WALKING("walking"),
        CYCLING("cycling");

        final String path;
        Profile(String path) { this.path = path; }
    }

    public Result routeKeepOrderDriving(List<double[]> latLon) {
        return routeKeepOrder(latLon, Profile.DRIVING);
    }

    public Result routeKeepOrderWalking(List<double[]> latLon) {
        return routeKeepOrder(latLon, Profile.WALKING);
    }

    public Result routeKeepOrderCycling(List<double[]> latLon) {
        return routeKeepOrder(latLon, Profile.CYCLING);
    }

    private Result routeKeepOrder(List<double[]> latLon, Profile profile) {
        String coords = toCoords(latLon);
        String url = BASE_URL + "/route/v1/" + profile.path + "/" + coords + "?geometries=geojson&overview=full";

        RouteResponse body = restTemplate
                .exchange(URI.create(url), HttpMethod.GET, HttpEntity.EMPTY, RouteResponse.class)
                .getBody();

        if (body == null || !"Ok".equals(body.code) || body.routes == null || body.routes.isEmpty()) {
            throw new IllegalStateException("OSRM route failed (" + profile.path + ")");
        }

        Route r = body.routes.get(0);
        return new Result(r.distance, r.duration, r.geometry, null);
    }

    public Result routeKeepOrder(List<double[]> latLon) {
        String coords = toCoords(latLon);
        String url = BASE_URL + "/route/v1/driving/" + coords + "?geometries=geojson&overview=full";
        RouteResponse body = restTemplate
                .exchange(URI.create(url), HttpMethod.GET, HttpEntity.EMPTY, RouteResponse.class)
                .getBody();

        if (body == null || !"Ok".equals(body.code) || body.routes == null || body.routes.isEmpty()) {
            throw new IllegalStateException("OSRM route failed");
        }

        Route r = body.routes.get(0);
        return new Result(r.distance, r.duration, r.geometry, null);
    }

    public Result tripOptimize(List<double[]> latLon) {
        String coords = toCoords(latLon);
        String url = BASE_URL + "/trip/v1/driving/" + coords +
                "?roundtrip=false&source=first&destination=last&geometries=geojson&overview=full";
        TripResponse body = restTemplate
                .exchange(URI.create(url), HttpMethod.GET, HttpEntity.EMPTY, TripResponse.class)
                .getBody();

        if (body == null || !"Ok".equals(body.code) || body.trips == null || body.trips.isEmpty()) {
            throw new IllegalStateException("OSRM trip failed");
        }

        Trip t = body.trips.get(0);
        return new Result(t.distance, t.duration, t.geometry, body.waypoints);
    }

    private String toCoords(List<double[]> latLon) {
        return latLon.stream()
                .map(p -> p[1] + "," + p[0])
                .reduce((a, b) -> a + ";" + b)
                .orElseThrow();
    }

    public record Result(double distance, double duration, GeoJson geometry, List<Waypoint> waypoints) {}

    public static class GeoJson {
        public String type;
        public List<List<Double>> coordinates;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Waypoint {
        public int waypoint_index;
        public String name;
        public double[] location;
    }

    public static class RouteResponse {
        public String code;
        public List<Route> routes;
    }

    public static class Route {
        public double distance;
        public double duration;
        public GeoJson geometry;
    }

    public static class TripResponse {
        public String code;
        public List<Trip> trips;
        public List<Waypoint> waypoints;
    }

    public static class Trip {
        public double distance;
        public double duration;
        public GeoJson geometry;
    }
}
