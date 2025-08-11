package com.milosz.podsiadly.userservice.controller;

import com.milosz.podsiadly.userservice.dto.CreateUserProfileRequest;
import com.milosz.podsiadly.userservice.dto.UserProfileDto;
import com.milosz.podsiadly.userservice.service.UserProfileCreateService;
import com.milosz.podsiadly.userservice.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user-profiles")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileCreateService userProfileCreateService;
    private final UserProfileService userProfileService;

    @PostMapping("/{spotifyId}/ensure")
    public ResponseEntity<Void> ensureProfile(
            @PathVariable String spotifyId,
            @RequestBody(required = false) CreateUserProfileRequest request
    ) {
        userProfileCreateService.createIfAbsentBySpotifyId(spotifyId, request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{spotifyId}")
    public ResponseEntity<UserProfileDto> getProfile(@PathVariable String spotifyId) {
        return ResponseEntity.ok(userProfileService.getProfile(spotifyId));
    }

    @PutMapping("/{spotifyId}")
    public ResponseEntity<UserProfileDto> updateProfile(
            @PathVariable String spotifyId,
            @RequestBody UserProfileDto dto
    ) {
        return ResponseEntity.ok(userProfileService.updateProfile(spotifyId, dto));
    }
}
