package com.milosz.podsiadly.uiservice.dto;

public record MultiModeRouteResponse(
        ModeRoute driving,
        ModeRoute walking,
        ModeRoute cycling
) {}
