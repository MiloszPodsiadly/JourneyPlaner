package com.milosz.podsiadly.musicservice.service;

import com.fasterxml.jackson.databind.JsonNode;

import com.milosz.podsiadly.musicservice.dto.SpotifyPlaylistDTO;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class SpotifyPlaylistService {

    private final RestTemplate restTemplate;
    private final String SPOTIFY_API = "https://api.spotify.com/v1/me/playlists";

    public SpotifyPlaylistService(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    public List<SpotifyPlaylistDTO> getUserPlaylists(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<JsonNode> response = restTemplate.exchange(
                SPOTIFY_API, HttpMethod.GET, entity, JsonNode.class
        );

        JsonNode items = response.getBody().get("items");

        List<SpotifyPlaylistDTO> playlists = new ArrayList<>();
        for (JsonNode item : items) {
            String id = item.get("id").asText();
            String name = item.get("name").asText();
            String description = item.path("description").asText("");
            String url = item.path("external_urls").path("spotify").asText();
            String ownerName = item.path("owner").path("display_name").asText("");
            int totalTracks = item.path("tracks").path("total").asInt();
            boolean isPublic = item.path("public").asBoolean();
            boolean isCollaborative = item.path("collaborative").asBoolean();
            String imageUrl = item.path("images").isArray() && item.path("images").size() > 0
                    ? item.path("images").get(0).path("url").asText()
                    : null;
            String snapshotId = item.path("snapshot_id").asText();

            playlists.add(new SpotifyPlaylistDTO(
                    id, name, description, url, ownerName,
                    totalTracks, isPublic, isCollaborative, imageUrl, snapshotId
            ));
        }

        return playlists;
    }
}

