package com.milosz.podsiadly.uiservice.dto;

public record UserProfileDto(
        Long id,
        Long userId,
        String displayName,
        String bio,
        String avatarUrl
) {}
