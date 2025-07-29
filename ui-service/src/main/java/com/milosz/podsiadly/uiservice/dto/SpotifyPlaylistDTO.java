package com.milosz.podsiadly.uiservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SpotifyPlaylistDTO(
        String id,
        String name,
        String description,
        String url,
        String ownerName,
        int totalTracks,
        @JsonProperty("public") boolean isPublic,
        @JsonProperty("collaborative") boolean isCollaborative,
        String imageUrl,
        String snapshotId
) {}
