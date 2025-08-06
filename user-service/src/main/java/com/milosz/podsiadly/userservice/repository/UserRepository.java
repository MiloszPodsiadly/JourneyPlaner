package com.milosz.podsiadly.userservice.repository;

import com.milosz.podsiadly.userservice.entity.User;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;



public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findBySpotifyId(String spotifyId);

    boolean existsBySpotifyId(String spotifyId);

}
