package com.milosz.podsiadly.userservice.service;

import com.milosz.podsiadly.userservice.dto.CreateUserRequest;
import com.milosz.podsiadly.userservice.entity.User;
import com.milosz.podsiadly.userservice.entity.UserProfile;
import com.milosz.podsiadly.userservice.repository.UserProfileRepository;
import com.milosz.podsiadly.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public void createUserIfNotExists(CreateUserRequest request) {
        if (userRepository.existsBySpotifyId(request.spotifyId())) {
            return;
        }

        User user = User.builder()
                .spotifyId(request.spotifyId())
                .name(request.name())
                .email(request.email())
                .build();

        userRepository.save(user);
    }
}
