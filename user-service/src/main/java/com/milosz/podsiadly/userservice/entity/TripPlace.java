package com.milosz.podsiadly.userservice.entity;


import com.sun.jdi.DoubleValue;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "trip_places")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripPlace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String displayName;

    private Double lat;

    private Double lon;

    private String category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_plan_id")
    private TripPlan tripPlan;
}

