package com.milosz.podsiadly.uiservice.dto;

public record TripPlaceDto(
        Long id,
        String displayName,
        String lat,
        String lon,
        String category
) {}

