package com.milosz.podsiadly.userservice.dto;

public record TripPlaylistDto(
        Long id,
        String playlistId,
        String name,
        Long tripPlanId
) {}
