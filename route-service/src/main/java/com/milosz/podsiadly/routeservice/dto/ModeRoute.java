package com.milosz.podsiadly.routeservice.dto;

public record ModeRoute(
        String mode,
        double distanceMeters,
        double durationSeconds
) {}

