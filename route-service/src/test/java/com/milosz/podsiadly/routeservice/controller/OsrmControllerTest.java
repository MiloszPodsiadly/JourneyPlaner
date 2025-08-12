package com.milosz.podsiadly.routeservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.milosz.podsiadly.routeservice.dto.RouteResponse;
import com.milosz.podsiadly.routeservice.service.JourneyService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OsrmController.class)
@DisplayName("OsrmController WebMvc")
class OsrmControllerTest {

    @TestConfiguration
    static class Config {
        @Bean
        JourneyService journeyService() {
            return Mockito.mock(JourneyService.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JourneyService journeyService;

    private RouteResponse r(double dist, double dur, List<Long> ids) {
        return new RouteResponse(dist, dur, null, ids);
    }

    @BeforeEach
    void setUp() {
        System.out.println("➡️ [BeforeEach] Preparing case...");
    }

    @AfterEach
    void tearDown() {
        clearInvocations(journeyService);
        reset(journeyService);
        System.out.println("⬅️ [AfterEach] Cleanup done.");
    }

    @Test
    @DisplayName("GET /api/osrm/route uses JWT from cookie and passes optimize flag")
    void routeByTripPlan_cookieJwt_andOptimize() throws Exception {
        when(journeyService.routeByTripPlan(42L, true, "jwt-cookie"))
                .thenReturn(r(12345.6, 789.0, List.of(1L, 2L, 3L)));

        mockMvc.perform(get("/api/osrm/route")
                        .param("tripPlanId", "42")
                        .param("optimize", "true")
                        .cookie(new Cookie("jwt", "jwt-cookie"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.distanceMeters").value(12345.6))
                .andExpect(jsonPath("$.durationSeconds").value(789.0))
                .andExpect(jsonPath("$.orderedPlaceIds.length()").value(3));

        verify(journeyService).routeByTripPlan(42L, true, "jwt-cookie");
    }

    @Test
    @DisplayName("GET /api/osrm/route prefers Authorization header when present")
    void routeByTripPlan_headerBearerPreferred() throws Exception {
        when(journeyService.routeByTripPlan(7L, false, "hdr-token"))
                .thenReturn(r(1.0, 2.0, List.of()));

        mockMvc.perform(get("/api/osrm/route")
                        .param("tripPlanId", "7")
                        .header("Authorization", "Bearer hdr-token"))
                .andExpect(status().isOk());

        verify(journeyService).routeByTripPlan(7L, false, "hdr-token");
    }

    @Test
    @DisplayName("GET /api/osrm/route passes null JWT when neither cookie nor header is present")
    void routeByTripPlan_noAuth() throws Exception {
        when(journeyService.routeByTripPlan(5L, false, null))
                .thenReturn(r(0.0, 0.0, List.of()));

        mockMvc.perform(get("/api/osrm/route").param("tripPlanId", "5"))
                .andExpect(status().isOk());

        verify(journeyService).routeByTripPlan(5L, false, null);
    }

    @Test
    @DisplayName("GET /api/osrm/route/driving uses cookie JWT")
    void routeDriving_cookieJwt() throws Exception {
        when(journeyService.routeDrivingByTripPlan(99L, "tok"))
                .thenReturn(r(100.0, 200.0, List.of(9L)));

        mockMvc.perform(get("/api/osrm/route/driving")
                        .param("tripPlanId", "99")
                        .cookie(new Cookie("jwt", "tok")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.distanceMeters").value(100.0))
                .andExpect(jsonPath("$.durationSeconds").value(200.0));

        verify(journeyService).routeDrivingByTripPlan(99L, "tok");
    }

    @Test
    @DisplayName("GET /api/osrm/route/walking uses header JWT")
    void routeWalking_headerJwt() throws Exception {
        when(journeyService.routeWalkingByTripPlan(11L, "aaa"))
                .thenReturn(r(10.5, 33.3, List.of()));

        mockMvc.perform(get("/api/osrm/route/walking")
                        .param("tripPlanId", "11")
                        .header("Authorization", "Bearer aaa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.distanceMeters").value(10.5));

        verify(journeyService).routeWalkingByTripPlan(11L, "aaa");
    }

    @Test
    @DisplayName("GET /api/osrm/route/cycling passes null JWT if absent")
    void routeCycling_noJwt() throws Exception {
        when(journeyService.routeCyclingByTripPlan(3L, null))
                .thenReturn(r(1.2, 3.4, List.of()));

        mockMvc.perform(get("/api/osrm/route/cycling").param("tripPlanId", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.durationSeconds").value(3.4));

        verify(journeyService).routeCyclingByTripPlan(3L, null);
    }

    @Test
    @DisplayName("GET /api/osrm/route/modes aggregates three mode calls and returns combined payload")
    void routeAllModes_aggregates() throws Exception {
        when(journeyService.routeDrivingByTripPlan(77L, "tok")).thenReturn(r(1000.0, 600.0, List.of()));
        when(journeyService.routeWalkingByTripPlan(77L, "tok")).thenReturn(r(800.0, 1200.0, List.of()));
        when(journeyService.routeCyclingByTripPlan(77L, "tok")).thenReturn(r(900.0, 700.0, List.of()));

        mockMvc.perform(get("/api/osrm/route/modes")
                        .param("tripPlanId", "77")
                        .cookie(new Cookie("jwt", "tok")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.driving.mode").value("driving"))
                .andExpect(jsonPath("$.driving.distanceMeters").value(1000.0))
                .andExpect(jsonPath("$.walking.durationSeconds").value(1200.0))
                .andExpect(jsonPath("$.cycling.distanceMeters").value(900.0));

        verify(journeyService).routeDrivingByTripPlan(77L, "tok");
        verify(journeyService).routeWalkingByTripPlan(77L, "tok");
        verify(journeyService).routeCyclingByTripPlan(77L, "tok");
    }
}