package com.milosz.podsiadly.userservice.service;

import com.milosz.podsiadly.userservice.dto.CreateUserProfileRequest;
import com.milosz.podsiadly.userservice.dto.UserProfileDto;
import com.milosz.podsiadly.userservice.entity.User;
import com.milosz.podsiadly.userservice.mapper.UserProfileCreateMapper;
import com.milosz.podsiadly.userservice.mapper.UserProfileMapper;
import com.milosz.podsiadly.userservice.repository.UserProfileRepository;
import com.milosz.podsiadly.userservice.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class UserProfileCreateServiceImpl implements UserProfileCreateService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    @jakarta.persistence.PersistenceContext
    private jakarta.persistence.EntityManager em;

    @Override
    public UserProfileDto createIfAbsentBySpotifyId(String spotifyId, CreateUserProfileRequest request) {
        var user = userRepository.findBySpotifyId(spotifyId)
                .orElseThrow(() -> new IllegalArgumentException("User not found by spotifyId: " + spotifyId));
        return createIfAbsent(user, request);
    }

    @Override
    public UserProfileDto createIfAbsent(User user, CreateUserProfileRequest request) {
        Long uid = user.getId();

        var existing = userProfileRepository.findById(uid).orElse(null);
        if (existing != null) return UserProfileMapper.toDto(existing);

        var managedUser = userRepository.getReferenceById(uid);

        var effective = (request != null)
                ? request
                : new CreateUserProfileRequest(
                managedUser.getName() != null ? managedUser.getName() : "User",
                null, null
        );

        var toSave = UserProfileCreateMapper.fromRequest(effective, managedUser);

        em.persist(toSave);


        return UserProfileMapper.toDto(toSave);
    }

    @Override
    public UserProfileDto ensureForUser(User user) {
        return createIfAbsent(user, null);
    }
}