package com.milosz.podsiadly.userservice.repository;

import com.milosz.podsiadly.userservice.entity.TripPlace;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TripPlaceRepository extends JpaRepository<TripPlace, Long> {

    List<TripPlace> findByTripPlanIdOrderBySortOrderAsc(Long tripPlanId);

    @Modifying
    @Query("UPDATE TripPlace p SET p.sortOrder = :order WHERE p.id = :id")
    void updateSortOrder(@Param("id") Long id, @Param("order") int order);

    @Query("select coalesce(max(tp.sortOrder), -1) from TripPlace tp where tp.tripPlan.id = :planId")
    Integer findMaxSortOrderByTripPlanId(@Param("planId") Long planId);
}
