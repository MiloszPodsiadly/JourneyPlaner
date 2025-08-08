package com.milosz.podsiadly.routeservice.dto;

public record TripPlaceView(
        Long id,
        String displayName,
        Double lat,
        Double lon
) {}