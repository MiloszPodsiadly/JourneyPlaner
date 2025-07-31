package com.milosz.podsiadly.userservice.service;


import com.milosz.podsiadly.userservice.entity.*;
import com.milosz.podsiadly.userservice.repository.*;
import com.milosz.podsiadly.userservice.service.TripPlanService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TripPlanServiceImpl implements TripPlanService {

    private final UserRepository userRepository;
    private final TripPlanRepository tripPlanRepository;
    private final TripPlaceRepository tripPlaceRepository;
    private final TripPlaylistRepository tripPlaylistRepository;

    @Override
    public TripPlan createTripPlan(String spotifyId, String name, String description) {
        User user = userRepository.findBySpotifyId(spotifyId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        TripPlan plan = new TripPlan();
        plan.setUser(user);
        plan.setName(name);
        plan.setDescription(description);
        return tripPlanRepository.save(plan);
    }

    @Override
    public List<TripPlan> getUserTripPlans(String spotifyId) {
        User user = userRepository.findBySpotifyId(spotifyId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return tripPlanRepository.findByUser(user);
    }

    @Override
    public void deleteTripPlan(Long tripPlanId) {
        tripPlanRepository.deleteById(tripPlanId);
    }

    @Override
    public void addPlaceToTrip(Long tripPlanId, String name, double lat, double lon) {
        TripPlan plan = tripPlanRepository.findById(tripPlanId)
                .orElseThrow(() -> new IllegalArgumentException("Trip plan not found"));
        TripPlace place = new TripPlace();
        place.setTripPlan(plan);
        place.setDisplayName(name);
        place.setLat(lat);
        place.setLon(lon);
        tripPlaceRepository.save(place);
    }

    @Override
    public void addPlaylistToTrip(Long tripPlanId, String playlistId, String name) {
        TripPlan plan = tripPlanRepository.findById(tripPlanId)
                .orElseThrow(() -> new IllegalArgumentException("Trip plan not found"));
        TripPlaylist playlist = new TripPlaylist();
        playlist.setTripPlan(plan);
        playlist.setPlaylistId(playlistId);
        playlist.setName(name);
        tripPlaylistRepository.save(playlist);
    }

    @Override
    public void removePlaceFromTrip(Long tripPlaceId) {
        tripPlaceRepository.deleteById(tripPlaceId);
    }

    @Override
    public void removePlaylistFromTrip(Long tripPlaylistId) {
        tripPlaylistRepository.deleteById(tripPlaylistId);
    }

    @Override
    public void updateTripPlan(Long id, String name, String description) {
        TripPlan plan = tripPlanRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Trip plan not found"));
        plan.setName(name);
        plan.setDescription(description);
        tripPlanRepository.save(plan);
    }


}

