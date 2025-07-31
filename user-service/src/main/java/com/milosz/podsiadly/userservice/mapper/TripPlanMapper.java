package com.milosz.podsiadly.userservice.mapper;

import com.milosz.podsiadly.userservice.dto.TripPlanDto;
import com.milosz.podsiadly.userservice.dto.TripPlaceDto;
import com.milosz.podsiadly.userservice.dto.TripPlaylistDto;
import com.milosz.podsiadly.userservice.entity.TripPlan;
import com.milosz.podsiadly.userservice.entity.User;

import java.util.List;
import java.util.stream.Collectors;

public class TripPlanMapper {

    public static TripPlanDto toDto(TripPlan plan) {
        List<TripPlaceDto> placeDtos = plan.getPlaces() != null
                ? plan.getPlaces().stream().map(TripPlaceMapper::toDto).collect(Collectors.toList())
                : null;

        List<TripPlaylistDto> playlistDtos = plan.getPlaylists() != null
                ? plan.getPlaylists().stream().map(TripPlaylistMapper::toDto).collect(Collectors.toList())
                : null;

        return new TripPlanDto(
                plan.getId(),
                plan.getName(),
                plan.getDescription(),
                placeDtos,
                playlistDtos,
                plan.getUser() != null ? plan.getUser().getId() : null
        );
    }

    public static TripPlan toEntity(TripPlanDto dto) {
        return TripPlan.builder()
                .id(dto.id())
                .name(dto.name())
                .description(dto.description())
                .user(dto.userId() != null ? User.builder().id(dto.userId()).build() : null)
                .build();
    }
}
