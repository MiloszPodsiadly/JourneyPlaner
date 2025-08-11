package com.milosz.podsiadly.userservice.dto;

public record CreateUserProfileRequest(
        String displayName,
        String bio,
        String avatarUrl
) {}
