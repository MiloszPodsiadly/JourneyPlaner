package com.milosz.podsiadly.userservice.mapper;

import com.milosz.podsiadly.userservice.dto.TripPlanDto;
import com.milosz.podsiadly.userservice.dto.UserDto;
import com.milosz.podsiadly.userservice.entity.User;

import java.util.List;
import java.util.stream.Collectors;

public class UserMapper {

    public static UserDto toDto(User user) {
        List<TripPlanDto> tripPlans = user.getTripPlans() != null
                ? user.getTripPlans().stream().map(TripPlanMapper::toDto).collect(Collectors.toList())
                : null;

        return new UserDto(
                user.getId(),
                user.getSpotifyId(),
                tripPlans
        );
    }

    public static User toEntity(UserDto dto) {
        return User.builder()
                .id(dto.id())
                .spotifyId(dto.spotifyId())
                .build();
    }
}
