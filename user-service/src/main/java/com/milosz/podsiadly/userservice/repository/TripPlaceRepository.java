package com.milosz.podsiadly.userservice.repository;

import com.milosz.podsiadly.userservice.entity.TripPlace;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TripPlaceRepository extends JpaRepository<TripPlace, Long> {
    List<TripPlace> findByTripPlanIdOrderByIdAsc(Long tripPlanId);

}
