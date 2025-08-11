package com.milosz.podsiadly.routeservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.milosz.podsiadly.routeservice.dto.RouteByTripPlanRequest;
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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(JourneyController.class)
@DisplayName("JourneyController WebMvc tests")
class JourneyControllerTest {

    @TestConfiguration
    static class Cfg {
        @Bean
        JourneyService journeyService() {
            return Mockito.mock(JourneyService.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JourneyService journeyService;

    @Autowired
    private ObjectMapper objectMapper;

    private static RouteResponse rr(double dist, double dur, List<Long> ids) {
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
    @DisplayName("POST /api/journey/route/by-trip-plan: uses JWT from cookie and passes body fields")
    void routeByTripPlan_usesCookieJwt() throws Exception {
        RouteByTripPlanRequest req = new RouteByTripPlanRequest(42L, true);

        when(journeyService.routeByTripPlan(42L, true, "cookie-jwt"))
                .thenReturn(rr(12345.6, 789.0, List.of(1L, 2L)));

        mockMvc.perform(post("/api/journey/route/by-trip-plan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .cookie(new Cookie("jwt", "cookie-jwt"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.distanceMeters").value(12345.6))
                .andExpect(jsonPath("$.durationSeconds").value(789.0))
                .andExpect(jsonPath("$.orderedPlaceIds.length()").value(2));

        verify(journeyService, times(1))
                .routeByTripPlan(eq(42L), eq(true), eq("cookie-jwt"));
    }

    @Test
    @DisplayName("POST /api/journey/route/by-trip-plan: uses JWT from Authorization header when no cookie")
    void routeByTripPlan_usesHeaderJwt() throws Exception {
        RouteByTripPlanRequest req = new RouteByTripPlanRequest(7L, false);

        when(journeyService.routeByTripPlan(7L, false, "hdr-123"))
                .thenReturn(rr(1.0, 2.0, List.of()));

        mockMvc.perform(post("/api/journey/route/by-trip-plan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .header("Authorization", "Bearer hdr-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.distanceMeters").value(1.0))
                .andExpect(jsonPath("$.durationSeconds").value(2.0));

        verify(journeyService).routeByTripPlan(7L, false, "hdr-123");
    }

    @Test
    @DisplayName("POST /api/journey/route/by-trip-plan: cookie is preferred over Authorization header")
    void routeByTripPlan_cookiePreferredOverHeader() throws Exception {
        RouteByTripPlanRequest req = new RouteByTripPlanRequest(9L, true);

        when(journeyService.routeByTripPlan(9L, true, "cookie-first"))
                .thenReturn(rr(10.0, 20.0, List.of(9L)));

        mockMvc.perform(post("/api/journey/route/by-trip-plan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .cookie(new Cookie("jwt", "cookie-first"))
                        .header("Authorization", "Bearer header-second"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderedPlaceIds[0]").value(9L));

        verify(journeyService).routeByTripPlan(9L, true, "cookie-first");
        verify(journeyService, never()).routeByTripPlan(9L, true, "header-second");
    }

    @Test
    @DisplayName("POST /api/journey/route/by-trip-plan: passes null JWT if neither cookie nor header present")
    void routeByTripPlan_noJwt() throws Exception {
        RouteByTripPlanRequest req = new RouteByTripPlanRequest(5L, false);

        when(journeyService.routeByTripPlan(5L, false, null))
                .thenReturn(rr(0.0, 0.0, List.of()));

        mockMvc.perform(post("/api/journey/route/by-trip-plan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.distanceMeters").value(0.0))
                .andExpect(jsonPath("$.durationSeconds").value(0.0));

        verify(journeyService).routeByTripPlan(5L, false, null);
    }

    @Test
    @DisplayName("POST /api/journey/route/by-trip-plan: validates that request body fields are forwarded")
    void routeByTripPlan_forwardsRequestFields() throws Exception {
        RouteByTripPlanRequest req = new RouteByTripPlanRequest(123L, true);

        when(journeyService.routeByTripPlan(123L, true, "tok"))
                .thenReturn(rr(555.5, 66.6, List.of(1L, 2L, 3L)));

        mockMvc.perform(post("/api/journey/route/by-trip-plan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .cookie(new Cookie("jwt", "tok")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.distanceMeters").value(555.5))
                .andExpect(jsonPath("$.durationSeconds").value(66.6))
                .andExpect(jsonPath("$.orderedPlaceIds.length()").value(3));

        verify(journeyService).routeByTripPlan(123L, true, "tok");
    }
}