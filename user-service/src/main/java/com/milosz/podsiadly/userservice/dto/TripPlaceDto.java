package com.milosz.podsiadly.userservice.dto;

public record TripPlaceDto(
        Long id,
        String displayName,
        Double lat,
        Double lon,
        String category,
        Long tripPlanId,
        Integer sortOrder
) {}
