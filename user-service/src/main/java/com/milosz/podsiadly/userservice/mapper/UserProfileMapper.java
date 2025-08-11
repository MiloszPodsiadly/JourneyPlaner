package com.milosz.podsiadly.userservice.mapper;

import com.milosz.podsiadly.userservice.dto.UserProfileDto;
import com.milosz.podsiadly.userservice.entity.User;
import com.milosz.podsiadly.userservice.entity.UserProfile;

public final class UserProfileMapper {

    private UserProfileMapper() {}

    public static UserProfileDto toDto(UserProfile entity) {
        if (entity == null) return null;

        return new UserProfileDto(
                entity.getId(),
                entity.getUser() != null ? entity.getUser().getId() : null,
                entity.getDisplayName(),
                entity.getBio(),
                entity.getAvatarUrl()
        );
    }
}
