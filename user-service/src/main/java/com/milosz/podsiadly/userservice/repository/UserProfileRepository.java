package com.milosz.podsiadly.userservice.repository;

import com.milosz.podsiadly.userservice.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {}
