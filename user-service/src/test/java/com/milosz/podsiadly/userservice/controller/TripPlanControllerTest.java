package com.milosz.podsiadly.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.milosz.podsiadly.userservice.dto.*;
import com.milosz.podsiadly.userservice.service.TripPlanService;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TripPlanController.class)
@DisplayName("TripPlanController WebMvc")
class TripPlanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TripPlanService tripPlanService;

    private TripPlanDto tripPlanDto;

    @TestConfiguration
    static class MockConfig {
        @Bean
        public TripPlanService tripPlanService() {
            return mock(TripPlanService.class);
        }
    }

    @BeforeAll
    static void beforeAll() {
        System.out.println("🔧 [BeforeAll] Starting TripPlanController tests...");
    }

    @AfterAll
    static void afterAll() {
        System.out.println("✅ [AfterAll] TripPlanController tests completed.");
    }

    @BeforeEach
    void setUp() {
        System.out.println("➡️ [BeforeEach] Setting up test data...");
        tripPlanDto = new TripPlanDto(1L, "Plan 1", "Trip Desc", List.of(), List.of(), 100L);
    }

    @AfterEach
    void tearDown() {
        tripPlanDto = null;
        System.out.println("⬅️ [AfterEach] Test finished. Cleaning up...");
    }

    @Test
    void shouldCreateTripPlan() throws Exception {
        var request = new CreateTripPlanRequest("spotify:123", "Plan 1", "Trip Desc");

        when(tripPlanService.createTripPlan(any(), any(), any()))
                .thenReturn(new com.milosz.podsiadly.userservice.entity.TripPlan());

        mockMvc.perform(post("/api/trip-plans/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetUserTripPlans() throws Exception {
        when(tripPlanService.getUserTripPlans("spotify:123"))
                .thenReturn(List.of(new com.milosz.podsiadly.userservice.entity.TripPlan()));

        mockMvc.perform(get("/api/trip-plans/user")
                        .param("spotifyId", "spotify:123"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDeleteTripPlan() throws Exception {
        mockMvc.perform(delete("/api/trip-plans/1"))
                .andExpect(status().isNoContent());

        verify(tripPlanService).deleteTripPlan(1L);
    }

    @Test
    void shouldAddPlaceToTrip() throws Exception {
        TripPlaceDto placeDto = new TripPlaceDto(1L, "Place", 1.1, 2.2, "Category", 1L);

        mockMvc.perform(post("/api/trip-plans/1/add-place")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(placeDto)))
                .andExpect(status().isOk());

        verify(tripPlanService).addPlaceToTrip(1L, "Place", 1.1, 2.2);
    }

    @Test
    void shouldAddPlaylistToTrip() throws Exception {
        TripPlaylistDto playlistDto = new TripPlaylistDto(1L, "PL123", "Chill Vibes", 1L);

        mockMvc.perform(post("/api/trip-plans/1/add-playlist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(playlistDto)))
                .andExpect(status().isOk());

        verify(tripPlanService).addPlaylistToTrip(1L, "PL123", "Chill Vibes");
    }

    @Test
    void shouldRemovePlace() throws Exception {
        mockMvc.perform(delete("/api/trip-plans/place/5"))
                .andExpect(status().isNoContent());

        verify(tripPlanService).removePlaceFromTrip(5L);
    }

    @Test
    void shouldRemovePlaylist() throws Exception {
        mockMvc.perform(delete("/api/trip-plans/playlist/8"))
                .andExpect(status().isNoContent());

        verify(tripPlanService).removePlaylistFromTrip(8L);
    }

    @Test
    void shouldUpdateTripPlan() throws Exception {
        Map<String, String> body = Map.of("name", "Updated Plan", "description", "Updated Desc");

        mockMvc.perform(put("/api/trip-plans/1/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());

        verify(tripPlanService).updateTripPlan(1L, "Updated Plan", "Updated Desc");
    }
}