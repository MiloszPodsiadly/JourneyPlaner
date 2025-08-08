package com.milosz.podsiadly.routeservice.controller;

import com.milosz.podsiadly.routeservice.dto.ModeRoute;
import com.milosz.podsiadly.routeservice.dto.MultiModeRouteResponse;
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

    @GetMapping("/route/driving")
    public ResponseEntity<RouteResponse> routeDriving(@RequestParam Long tripPlanId,
                                                      HttpServletRequest request) {
        String jwt = extractJwt(request);
        return ResponseEntity.ok(journeyService.routeDrivingByTripPlan(tripPlanId, jwt));
    }

    @GetMapping("/route/walking")
    public ResponseEntity<RouteResponse> routeWalking(@RequestParam Long tripPlanId,
                                                      HttpServletRequest request) {
        String jwt = extractJwt(request);
        return ResponseEntity.ok(journeyService.routeWalkingByTripPlan(tripPlanId, jwt));
    }

    @GetMapping("/route/cycling")
    public ResponseEntity<RouteResponse> routeCycling(@RequestParam Long tripPlanId,
                                                      HttpServletRequest request) {
        String jwt = extractJwt(request);
        return ResponseEntity.ok(journeyService.routeCyclingByTripPlan(tripPlanId, jwt));
    }

    @GetMapping("/route/modes")
    public ResponseEntity<MultiModeRouteResponse> routeAllModes(@RequestParam Long tripPlanId,
                                                                HttpServletRequest request) {
        String jwt = extractJwt(request);

        RouteResponse d = journeyService.routeDrivingByTripPlan(tripPlanId, jwt);
        RouteResponse w = journeyService.routeWalkingByTripPlan(tripPlanId, jwt);
        RouteResponse c = journeyService.routeCyclingByTripPlan(tripPlanId, jwt);

        MultiModeRouteResponse payload = new MultiModeRouteResponse(
                new ModeRoute("driving", d.distanceMeters(), d.durationSeconds()),
                new ModeRoute("walking", w.distanceMeters(), w.durationSeconds()),
                new ModeRoute("cycling", c.distanceMeters(), c.durationSeconds())
        );
        return ResponseEntity.ok(payload);
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
