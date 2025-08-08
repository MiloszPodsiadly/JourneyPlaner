package com.milosz.podsiadly.uiservice.dto;

public record ModeRoute(
        String mode,
        double distanceMeters,
        double durationSeconds
) {}
