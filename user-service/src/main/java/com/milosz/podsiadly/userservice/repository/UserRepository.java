package com.milosz.podsiadly.userservice.repository;

import com.milosz.podsiadly.userservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findBySpotifyId(String spotifyId);
    boolean existsBySpotifyId(String spotifyId);

}
