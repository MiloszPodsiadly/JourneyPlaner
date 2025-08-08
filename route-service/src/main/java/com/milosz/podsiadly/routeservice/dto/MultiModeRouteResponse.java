package com.milosz.podsiadly.routeservice.dto;

public record MultiModeRouteResponse(
        ModeRoute driving,
        ModeRoute walking,
        ModeRoute cycling
) {}
