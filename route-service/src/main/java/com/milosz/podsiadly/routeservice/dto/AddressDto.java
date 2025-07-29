package com.milosz.podsiadly.routeservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AddressDto(
        @JsonProperty("lat") String lat,
        @JsonProperty("lon") String lon,
        @JsonProperty("display_name") String displayName
) {}

