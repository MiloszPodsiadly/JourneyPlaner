// UserDto.java
package com.milosz.podsiadly.userservice.dto;

import java.util.List;

public record UserDto(
        Long id,
        String spotifyId,
        List<TripPlanDto> tripPlans
) {}
