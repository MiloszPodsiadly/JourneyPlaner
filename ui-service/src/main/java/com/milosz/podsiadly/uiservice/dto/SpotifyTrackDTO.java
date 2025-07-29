package com.milosz.podsiadly.uiservice.dto;


import java.util.List;

public record SpotifyTrackDTO(
        String id,
        String name,
        List<String> artists,
        String albumName,
        String previewUrl,
        String externalUrl
) {}
