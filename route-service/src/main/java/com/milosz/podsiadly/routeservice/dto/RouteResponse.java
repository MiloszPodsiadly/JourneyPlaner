package com.milosz.podsiadly.routeservice.dto;

import java.util.List;

public record RouteResponse(
        double distanceMeters,
        double durationSeconds,
        Object geometry,
        List<Long> orderedPlaceIds
) {}
