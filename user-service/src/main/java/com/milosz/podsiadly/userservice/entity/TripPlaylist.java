package com.milosz.podsiadly.userservice.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "trip_playlists")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripPlaylist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String playlistId; // ID ze Spotify

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_plan_id")
    private TripPlan tripPlan;
}

