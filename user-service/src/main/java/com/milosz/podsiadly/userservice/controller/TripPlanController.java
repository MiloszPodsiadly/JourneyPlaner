package com.milosz.podsiadly.userservice.controller;

import com.milosz.podsiadly.userservice.dto.*;
import com.milosz.podsiadly.userservice.entity.TripPlace;
import com.milosz.podsiadly.userservice.entity.TripPlaylist;
import com.milosz.podsiadly.userservice.mapper.TripPlaceMapper;
import com.milosz.podsiadly.userservice.mapper.TripPlanMapper;
import com.milosz.podsiadly.userservice.mapper.TripPlaylistMapper;
import com.milosz.podsiadly.userservice.service.TripPlanService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/trip-plans")
@RequiredArgsConstructor
public class TripPlanController {

    private final TripPlanService tripPlanService;

    @PostMapping("/create")
    public ResponseEntity<TripPlanDto> createTripPlan(@RequestBody CreateTripPlanRequest request) {
        var plan = tripPlanService.createTripPlan(
                request.spotifyId(),
                request.name(),
                request.description()
        );
        return ResponseEntity.ok(TripPlanMapper.toDto(plan));
    }

    @GetMapping("/user")
    public ResponseEntity<List<TripPlanDto>> getUserPlans(@RequestParam String spotifyId) {
        var plans = tripPlanService.getUserTripPlans(spotifyId);
        var dtoList = plans.stream()
                .map(TripPlanMapper::toDto)
                .toList();
        return ResponseEntity.ok(dtoList);
    }

    @DeleteMapping("/{tripPlanId}")
    public ResponseEntity<Void> deleteTripPlan(@PathVariable Long tripPlanId) {
        tripPlanService.deleteTripPlan(tripPlanId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{tripPlanId}/add-place")
    public ResponseEntity<Void> addPlaceToTrip(
            @PathVariable Long tripPlanId,
            @RequestBody TripPlaceDto placeDto) {

        TripPlace place = TripPlaceMapper.toEntity(placeDto);
        tripPlanService.addPlaceToTrip(
                tripPlanId,
                place.getDisplayName(),
                place.getLat(),
                place.getLon()
        );
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{tripPlanId}/add-playlist")
    public ResponseEntity<Void> addPlaylistToTrip(
            @PathVariable Long tripPlanId,
            @RequestBody TripPlaylistDto playlistDto) {

        TripPlaylist playlist = TripPlaylistMapper.toEntity(playlistDto);
        tripPlanService.addPlaylistToTrip(
                tripPlanId,
                playlist.getPlaylistId(),
                playlist.getName()
        );
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/place/{tripPlaceId}")
    public ResponseEntity<Void> removePlace(@PathVariable Long tripPlaceId) {
        tripPlanService.removePlaceFromTrip(tripPlaceId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/playlist/{tripPlaylistId}")
    public ResponseEntity<Void> removePlaylist(@PathVariable Long tripPlaylistId) {
        tripPlanService.removePlaylistFromTrip(tripPlaylistId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{tripPlanId}/update")
    public ResponseEntity<Void> updateTripPlan(
            @PathVariable Long tripPlanId,
            @RequestBody Map<String, String> body) {

        String name = body.get("name");
        String description = body.get("description");
        tripPlanService.updateTripPlan(tripPlanId, name, description);
        return ResponseEntity.ok().build();
    }
    @GetMapping("/{tripPlanId}/places")
    public ResponseEntity<List<TripPlaceDto>> getPlaces(@PathVariable Long tripPlanId) {
        var list = tripPlanService.getPlacesForTripPlan(tripPlanId)
                .stream()
                .map(TripPlaceMapper::toDto)
                .toList();
        return ResponseEntity.ok(list);
    }
    @PutMapping("/{tripPlanId}/places/reorder")
    public ResponseEntity<Void> reorderPlaces(
            @PathVariable Long tripPlanId,
            @RequestBody ReorderPlacesRequest req) {
        tripPlanService.reorderPlaces(tripPlanId, req.orderedPlaceIds());
        return ResponseEntity.noContent().build();
    }
}
