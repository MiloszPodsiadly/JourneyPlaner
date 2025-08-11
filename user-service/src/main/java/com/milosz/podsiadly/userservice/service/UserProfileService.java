package com.milosz.podsiadly.userservice.service;

import com.milosz.podsiadly.userservice.dto.UserProfileDto;

public interface UserProfileService {
    UserProfileDto getProfile(String spotifyId);
    UserProfileDto updateProfile(String spotifyId, UserProfileDto dto);
}
