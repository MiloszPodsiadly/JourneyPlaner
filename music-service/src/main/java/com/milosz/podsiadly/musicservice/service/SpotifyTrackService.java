package com.milosz.podsiadly.musicservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.milosz.podsiadly.musicservice.dto.SpotifyTrackDTO;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class SpotifyTrackService {

    private final RestTemplate restTemplate;

    public SpotifyTrackService(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    public List<SpotifyTrackDTO> getPlaylistTracks(String playlistId, String accessToken) {
        String url = "https://api.spotify.com/v1/playlists/" + playlistId + "/tracks";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<JsonNode> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, JsonNode.class
        );

        JsonNode items = response.getBody().path("items");
        List<SpotifyTrackDTO> tracks = new ArrayList<>();

        for (JsonNode item : items) {
            JsonNode trackNode = item.path("track");
            if (trackNode.isMissingNode() || trackNode.isNull()) continue;

            String id = trackNode.path("id").asText();
            String name = trackNode.path("name").asText();
            String albumName = trackNode.path("album").path("name").asText();
            String previewUrl = trackNode.path("preview_url").asText(null);
            String externalUrl = trackNode.path("external_urls").path("spotify").asText();

            List<String> artists = new ArrayList<>();
            for (JsonNode artist : trackNode.path("artists")) {
                artists.add(artist.path("name").asText());
            }

            tracks.add(new SpotifyTrackDTO(id, name, artists, albumName, previewUrl, externalUrl));
        }

        return tracks;
    }
}

