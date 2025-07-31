package com.milosz.podsiadly.userservice.dto;

import java.util.List;

public record TripPlanDto(
        Long id,
        String name,
        String description,
        List<TripPlaceDto> places,
        List<TripPlaylistDto> playlists,
        Long userId
) {}


