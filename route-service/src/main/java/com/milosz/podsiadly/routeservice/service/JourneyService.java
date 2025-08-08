package com.milosz.podsiadly.routeservice.service;

import com.milosz.podsiadly.routeservice.dto.RouteResponse;
import com.milosz.podsiadly.routeservice.dto.TripPlaceView;
import org.springframework.stereotype.Service;

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
        ensureAtLeastTwo(places);

        List<double[]> coords = places.stream()
                .map(p -> new double[]{p.lat(), p.lon()})
                .toList();
        List<Long> ids = places.stream().map(TripPlaceView::id).toList();

        OsrmClient.Result result = optimize
                ? osrmClient.tripOptimize(coords)
                : osrmClient.routeKeepOrder(coords);

        List<Long> orderedIds = optimize && result.waypoints() != null
                ? result.waypoints().stream()
                .map(wp -> ids.get(wp.waypoint_index))
                .toList()
                : ids;

        return new RouteResponse(
                result.distance(),
                result.duration(),
                result.geometry(),
                orderedIds
        );
    }

    public RouteResponse routeDrivingByTripPlan(Long tripPlanId, String jwt) {
        return buildSimpleRoute(tripPlanId, jwt, Mode.DRIVING);
    }

    public RouteResponse routeWalkingByTripPlan(Long tripPlanId, String jwt) {
        return buildSimpleRoute(tripPlanId, jwt, Mode.WALKING);
    }

    public RouteResponse routeCyclingByTripPlan(Long tripPlanId, String jwt) {
        return buildSimpleRoute(tripPlanId, jwt, Mode.CYCLING);
    }

    private enum Mode { DRIVING, WALKING, CYCLING }

    private RouteResponse buildSimpleRoute(Long tripPlanId, String jwt, Mode mode) {
        List<TripPlaceView> places = userServiceClient.getPlacesForTripPlan(tripPlanId, jwt);
        ensureAtLeastTwo(places);

        List<double[]> coords = places.stream()
                .map(p -> new double[]{p.lat(), p.lon()})
                .toList();
        List<Long> ids = places.stream().map(TripPlaceView::id).toList();

        OsrmClient.Result result =
                switch (mode) {
                    case DRIVING -> osrmClient.routeKeepOrderDriving(coords);
                    case WALKING -> osrmClient.routeKeepOrderWalking(coords);
                    case CYCLING -> osrmClient.routeKeepOrderCycling(coords);
                };

        return new RouteResponse(
                result.distance(),
                result.duration(),
                result.geometry(),
                ids
        );
    }

    private static void ensureAtLeastTwo(List<TripPlaceView> places) {
        if (places == null || places.size() < 2) {
            throw new IllegalArgumentException("Need at least 2 places to create a route");
        }
    }
}
