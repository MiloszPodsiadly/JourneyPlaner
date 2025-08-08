package com.milosz.podsiadly.routeservice.dto;

public record RouteByTripPlanRequest(
        Long tripPlanId,
        boolean optimize
) {}
