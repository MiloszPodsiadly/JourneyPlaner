package com.milosz.podsiadly.userservice.service;

import com.milosz.podsiadly.userservice.dto.CreateUserProfileRequest;
import com.milosz.podsiadly.userservice.dto.UserProfileDto;
import com.milosz.podsiadly.userservice.entity.User;

public interface UserProfileCreateService {

    UserProfileDto createIfAbsentBySpotifyId(String spotifyId, CreateUserProfileRequest request);

    UserProfileDto createIfAbsent(User user, CreateUserProfileRequest request);

    UserProfileDto ensureForUser(User user);
}