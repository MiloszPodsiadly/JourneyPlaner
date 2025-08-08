package com.milosz.podsiadly.routeservice.service;


import com.milosz.podsiadly.routeservice.dto.RouteResponse;
import com.milosz.podsiadly.routeservice.service.OsrmClient;
import com.milosz.podsiadly.routeservice.dto.TripPlaceView;
import com.milosz.podsiadly.routeservice.service.UserServiceClient;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class JourneyService {

    private final UserServiceClient userServiceClient;
    private final OsrmClient osrmClient;

    public JourneyService(UserServiceClient userServiceClient, OsrmClient osrmClient) {
        this.userServiceClient = userServiceClient;
        this.osrmClient = osrmClient;
    }

    public RouteResponse routeByTripPlan(Long tripPlanId, boolean optimize, String jwt) {
        List<TripPlaceView> places = userServiceClient.getPlacesForTripPlan(tripPlanId, jwt);
        if (places == null || places.size() < 2) {
            throw new IllegalArgumentException("Need at least 2 places to create a route");
        }

        List<double[]> coords = places.stream()
                .map(p -> new double[]{p.lat(), p.lon()}) // [lat, lon]
                .toList();
        List<Long> ids = places.stream().map(TripPlaceView::id).toList();

        var result = optimize ? osrmClient.tripOptimize(coords) : osrmClient.routeKeepOrder(coords);

        List<Long> orderedIds = optimize && result.waypoints() != null
                ? result.waypoints().stream()
                .sorted(Comparator.comparingInt(w -> w.waypoint_index))
                .map(w -> ids.get(w.waypoint_index))
                .toList()
                : ids;

        return new RouteResponse(
                result.distance(),
                result.duration(),
                result.geometry(),
                orderedIds
        );
    }
}
