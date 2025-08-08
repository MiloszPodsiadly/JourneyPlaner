package com.milosz.podsiadly.uiservice.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record RouteResponse(
        @JsonProperty("distance")
        @JsonAlias("distanceMeters")
        double distance,

        @JsonProperty("duration")
        @JsonAlias("durationSeconds")
        double duration,

        @JsonProperty("geometry")
        GeoJson geometry,

        @JsonProperty("orderedIds")
        @JsonAlias("orderedPlaceIds")
        List<Long> orderedIds
) {
    public record GeoJson(String type, List<List<Double>> coordinates) {}
}
