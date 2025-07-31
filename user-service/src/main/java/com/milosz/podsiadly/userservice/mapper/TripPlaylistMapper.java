package com.milosz.podsiadly.userservice.mapper;

import com.milosz.podsiadly.userservice.dto.TripPlaylistDto;
import com.milosz.podsiadly.userservice.entity.TripPlaylist;
import com.milosz.podsiadly.userservice.entity.TripPlan;

public class TripPlaylistMapper {

    public static TripPlaylistDto toDto(TripPlaylist playlist) {
        return new TripPlaylistDto(
                playlist.getId(),
                playlist.getPlaylistId(),
                playlist.getName(),
                playlist.getTripPlan() != null ? playlist.getTripPlan().getId() : null
        );
    }

    public static TripPlaylist toEntity(TripPlaylistDto dto) {
        return TripPlaylist.builder()
                .id(dto.id())
                .playlistId(dto.playlistId())
                .name(dto.name())
                .tripPlan(dto.tripPlanId() != null ? TripPlan.builder().id(dto.tripPlanId()).build() : null)
                .build();
    }
}
