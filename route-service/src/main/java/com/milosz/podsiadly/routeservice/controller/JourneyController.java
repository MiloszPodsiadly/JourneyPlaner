package com.milosz.podsiadly.routeservice.controller;


import com.milosz.podsiadly.routeservice.dto.RouteByTripPlanRequest;
import com.milosz.podsiadly.routeservice.dto.RouteResponse;
import com.milosz.podsiadly.routeservice.service.JourneyService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/journey")
public class JourneyController {

    private final JourneyService journeyService;

    public JourneyController(JourneyService journeyService) {
        this.journeyService = journeyService;
    }

    @PostMapping("/route/by-trip-plan")
    public ResponseEntity<RouteResponse> routeByTripPlan(@RequestBody RouteByTripPlanRequest req,
                                                         HttpServletRequest http) {
        String jwt = extractJwt(http);
        return ResponseEntity.ok(
                journeyService.routeByTripPlan(req.tripPlanId(), req.optimize(), jwt)
        );
    }

    private String extractJwt(HttpServletRequest http) {
        var cookies = http.getCookies();
        if (cookies != null) {
            for (var c : cookies) {
                if ("jwt".equals(c.getName())) return c.getValue();
            }
        }
        var auth = http.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            return auth.substring(7);
        }
        return null;
    }
}

