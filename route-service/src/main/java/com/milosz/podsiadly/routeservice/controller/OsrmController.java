package com.milosz.podsiadly.routeservice.controller;

import com.milosz.podsiadly.routeservice.dto.RouteResponse;
import com.milosz.podsiadly.routeservice.service.JourneyService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/osrm")
public class OsrmController {

    private final JourneyService journeyService;

    public OsrmController(JourneyService journeyService) {
        this.journeyService = journeyService;
    }



    @GetMapping("/route")
    public ResponseEntity<RouteResponse> routeByTripPlan(@RequestParam Long tripPlanId,
                                                         @RequestParam(defaultValue = "false") boolean optimize,
                                                         HttpServletRequest request) {
        String jwt = extractJwt(request);
        return ResponseEntity.ok(journeyService.routeByTripPlan(tripPlanId, optimize, jwt));
    }

    private String extractJwt(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (var c : request.getCookies()) {
                if ("jwt".equals(c.getName())) {
                    return c.getValue();
                }
            }
        }
        var auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            return auth.substring(7);
        }
        return null;
    }
}

