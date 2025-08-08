package com.milosz.podsiadly.userservice.service;

import com.milosz.podsiadly.userservice.entity.TripPlace;
import com.milosz.podsiadly.userservice.entity.TripPlan;

import java.util.List;

public interface TripPlanService {
    TripPlan createTripPlan(String spotifyId, String name, String description);
    List<TripPlan> getUserTripPlans(String spotifyId);
    void deleteTripPlan(Long tripPlanId);
    void addPlaceToTrip(Long tripPlanId, String name, double lat, double lon);
    void addPlaylistToTrip(Long tripPlanId, String playlistId, String name);
    void removePlaceFromTrip(Long tripPlaceId);
    void removePlaylistFromTrip(Long tripPlaylistId);
    void updateTripPlan(Long id, String name, String description);
    List<TripPlace> getPlacesForTripPlan(Long tripPlanId);
    void reorderPlaces(Long tripPlanId, List<Long> orderedPlaceIds);
}
