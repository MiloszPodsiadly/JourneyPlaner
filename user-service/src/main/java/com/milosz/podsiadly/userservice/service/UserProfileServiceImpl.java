package com.milosz.podsiadly.userservice.service;

import com.milosz.podsiadly.userservice.dto.UserProfileDto;
import com.milosz.podsiadly.userservice.entity.User;
import com.milosz.podsiadly.userservice.entity.UserProfile;
import com.milosz.podsiadly.userservice.mapper.UserProfileMapper;
import com.milosz.podsiadly.userservice.repository.UserProfileRepository;
import com.milosz.podsiadly.userservice.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class UserProfileServiceImpl implements UserProfileService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    @Override
    public UserProfileDto getProfile(String spotifyId) {
        User user = findUser(spotifyId);
        UserProfile profile = userProfileRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalStateException(
                        "Profile not found for user id: " + user.getId()));
        return UserProfileMapper.toDto(profile);
    }

    @Override
    public UserProfileDto updateProfile(String spotifyId, UserProfileDto dto) {
        User user = findUser(spotifyId);
        UserProfile profile = userProfileRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalStateException(
                        "Profile not found for user id: " + user.getId()));

        if (dto != null) {
            if (dto.displayName() != null) profile.setDisplayName(dto.displayName());
            if (dto.bio() != null)         profile.setBio(dto.bio());
            if (dto.avatarUrl() != null)   profile.setAvatarUrl(dto.avatarUrl());
        }

        return UserProfileMapper.toDto(userProfileRepository.save(profile));
    }

    private User findUser(String spotifyId) {
        return userRepository.findBySpotifyId(spotifyId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found by spotifyId: " + spotifyId));
    }
}
