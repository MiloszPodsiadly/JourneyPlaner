package com.milosz.podsiadly.musicservice.controller;

import com.milosz.podsiadly.musicservice.dto.SpotifyTrackDTO;
import com.milosz.podsiadly.musicservice.service.SpotifyTrackService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/spotify/playlists")
public class SpotifyTrackController {

    private final SpotifyTrackService trackService;

    public SpotifyTrackController(SpotifyTrackService trackService) {
        this.trackService = trackService;
    }

    @GetMapping("/{playlistId}/tracks")
    public ResponseEntity<List<SpotifyTrackDTO>> getPlaylistTracks(
            @PathVariable String playlistId,
            @RequestHeader("Authorization") String authHeader
    ) {
        if (!authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = authHeader.substring(7);
        List<SpotifyTrackDTO> tracks = trackService.getPlaylistTracks(playlistId, token);
        return ResponseEntity.ok(tracks);
    }
}

