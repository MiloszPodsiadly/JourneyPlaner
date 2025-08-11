package com.milosz.podsiadly.userservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_profiles")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class UserProfile {

    @Id
    private Long id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id")
    private User user;

    private String displayName;

    private String bio;

    private String avatarUrl;

}
