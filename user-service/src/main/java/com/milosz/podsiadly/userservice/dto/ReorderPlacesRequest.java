package com.milosz.podsiadly.userservice.dto;

import java.util.List;

public record ReorderPlacesRequest(List<Long> orderedPlaceIds) {}

