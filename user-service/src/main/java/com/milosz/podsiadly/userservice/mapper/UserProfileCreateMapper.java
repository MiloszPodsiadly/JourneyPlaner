package com.milosz.podsiadly.userservice.mapper;

import com.milosz.podsiadly.userservice.dto.CreateUserProfileRequest;
import com.milosz.podsiadly.userservice.entity.User;
import com.milosz.podsiadly.userservice.entity.UserProfile;

public final class UserProfileCreateMapper {

    private UserProfileCreateMapper() {}

    public static UserProfile fromRequest(CreateUserProfileRequest req, User user) {
        if (user == null) throw new IllegalArgumentException("User is required to build UserProfile");

        return UserProfile.builder()
                .id(user.getId())
                .user(user)
                .displayName(req.displayName())
                .bio(req.bio())
                .avatarUrl(req.avatarUrl())
                .build();
    }
}
