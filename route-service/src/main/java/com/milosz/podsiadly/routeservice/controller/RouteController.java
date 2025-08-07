package com.milosz.podsiadly.routeservice.controller;

import com.milosz.podsiadly.routeservice.dto.AddressDto;
import com.milosz.podsiadly.routeservice.dto.LocationDto;
import com.milosz.podsiadly.routeservice.service.RouteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
                ex.printStackTrace();
                return ResponseEntity.internalServerError().build();
            }
        }
    @GetMapping("/top-places")
    public ResponseEntity<LocationDto[]> topPlaces(@RequestParam String city) {
        try {
            System.out.println("🎯 RouteController: /top-places called with city = " + city);
            List<LocationDto> places = routeService.searchTopPlaces(city);

            if (!places.isEmpty()) {
                return ResponseEntity.ok(places.toArray(new LocationDto[0]));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    @GetMapping("/discover")
    public ResponseEntity<LocationDto[]> discoverPlaces(
            @RequestParam String city,
            @RequestParam String category) {
        try {
            System.out.printf("📌 Discover places in %s, category: %s%n", city, category);
            LocationDto[] places = routeService.searchPlacesByCategory(city, category);

            if (places.length > 0) {
                return ResponseEntity.ok(places);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
