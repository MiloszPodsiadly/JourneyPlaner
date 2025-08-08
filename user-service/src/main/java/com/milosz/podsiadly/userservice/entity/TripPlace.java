package com.milosz.podsiadly.userservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "trip_places")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TripPlace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String displayName;
    private Double lat;
    private Double lon;
    private String category;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_plan_id")
    private TripPlan tripPlan;
}
