package com.milosz.podsiadly.musicservice.controller;

import com.milosz.podsiadly.musicservice.dto.SpotifyPlaylistDTO;
import com.milosz.podsiadly.musicservice.service.SpotifyPlaylistService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/playlists")
public class SpotifyPlaylistController {

    private final SpotifyPlaylistService playlistService;

    public SpotifyPlaylistController(SpotifyPlaylistService playlistService) {
        this.playlistService = playlistService;
    }

    @GetMapping
    public ResponseEntity<List<SpotifyPlaylistDTO>> getPlaylists(
        @RequestHeader("Authorization") String authHeader
    ) {
        if (!authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = authHeader.substring(7);
        List<SpotifyPlaylistDTO> playlists = playlistService.getUserPlaylists(token);
        return ResponseEntity.ok(playlists);
    }
}
