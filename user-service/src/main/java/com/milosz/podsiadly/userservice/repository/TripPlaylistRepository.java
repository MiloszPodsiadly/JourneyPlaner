package com.milosz.podsiadly.userservice.repository;

import com.milosz.podsiadly.userservice.entity.TripPlaylist;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TripPlaylistRepository extends JpaRepository<TripPlaylist, Long> {
}

