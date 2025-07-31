package com.milosz.podsiadly.userservice.controller;

import com.milosz.podsiadly.userservice.dto.CreateUserRequest;
import com.milosz.podsiadly.userservice.entity.User;
import com.milosz.podsiadly.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @PostMapping("/create")
    public ResponseEntity<Void> createUserIfNotExists(@RequestBody CreateUserRequest request) {
        if (userRepository.existsBySpotifyId(request.spotifyId())) {
            return ResponseEntity.ok().build(); // już istnieje
        }

        var user = User.builder()
                .spotifyId(request.spotifyId())
                .name(request.name())
                .email(request.email())
                .build();

        userRepository.save(user);
        return ResponseEntity.ok().build(); // nowy użytkownik zapisany
    }
}

