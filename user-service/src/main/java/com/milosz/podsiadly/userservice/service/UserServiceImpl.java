package com.milosz.podsiadly.userservice.service;



import com.milosz.podsiadly.userservice.entity.User;
import com.milosz.podsiadly.userservice.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
@Service
public class UserServiceImpl {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getOrCreateUser(String spotifyId, String email) {
        return userRepository.findBySpotifyId(spotifyId)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setSpotifyId(spotifyId);
                    return userRepository.save(newUser);
                });
    }
}
