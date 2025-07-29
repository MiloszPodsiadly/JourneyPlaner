package com.milosz.podsiadly.routeservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LocationDto(
        @JsonProperty("lat") String latitude,
        @JsonProperty("lon") String longitude,
        @JsonProperty("display_name") String displayName
) {}

