package com.milosz.podsiadly.routeservice.controller;

import com.milosz.podsiadly.routeservice.dto.AddressDto;
import com.milosz.podsiadly.routeservice.dto.LocationDto;
import com.milosz.podsiadly.routeservice.service.RouteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/route")
public class RouteController {

    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }



    @GetMapping("/search")
    public ResponseEntity<LocationDto[]> search(@RequestParam String q) {
        try {
            System.out.println("🎯 RouteController: /search called with query = " + q);
            LocationDto location = routeService.searchPlace(q);
            if (location != null) {
                return ResponseEntity.ok(new LocationDto[]{location});
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception ex) {
            ex.printStackTrace(); // Optional: Replace with logger.error(...) in production
            return ResponseEntity.internalServerError().build();
        }
    }
}
