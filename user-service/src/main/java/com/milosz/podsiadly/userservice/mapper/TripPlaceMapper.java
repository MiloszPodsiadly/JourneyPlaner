package com.milosz.podsiadly.userservice.mapper;

import com.milosz.podsiadly.userservice.dto.TripPlaceDto;
import com.milosz.podsiadly.userservice.entity.TripPlace;
import com.milosz.podsiadly.userservice.entity.TripPlan;

public class TripPlaceMapper {

    public static TripPlaceDto toDto(TripPlace place) {
        return new TripPlaceDto(
                place.getId(),
                place.getDisplayName(),
                place.getLat(),
                place.getLon(),
                place.getCategory(),
                place.getTripPlan() != null ? place.getTripPlan().getId() : null,
                place.getSortOrder()
        );
    }

    public static TripPlace toEntity(TripPlaceDto dto) {
        return TripPlace.builder()
                .id(dto.id())
                .displayName(dto.displayName())
                .lat(dto.lat())
                .lon(dto.lon())
                .category(dto.category())
                .sortOrder(dto.sortOrder() != null ? dto.sortOrder() : 0)
                .tripPlan(dto.tripPlanId() != null ? TripPlan.builder().id(dto.tripPlanId()).build() : null)
                .build();
    }
}
