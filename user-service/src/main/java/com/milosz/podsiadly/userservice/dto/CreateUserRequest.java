package com.milosz.podsiadly.userservice.dto;

public record CreateUserRequest(
        String spotifyId,
        String name,
        String email
) {}
