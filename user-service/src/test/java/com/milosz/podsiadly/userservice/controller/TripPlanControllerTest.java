package com.milosz.podsiadly.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.milosz.podsiadly.userservice.dto.CreateTripPlanRequest;
import com.milosz.podsiadly.userservice.dto.ReorderPlacesRequest;
import com.milosz.podsiadly.userservice.dto.TripPlaceDto;
import com.milosz.podsiadly.userservice.dto.TripPlanDto;
import com.milosz.podsiadly.userservice.dto.TripPlaylistDto;
import com.milosz.podsiadly.userservice.entity.TripPlace;
import com.milosz.podsiadly.userservice.entity.TripPlan;
import com.milosz.podsiadly.userservice.service.TripPlanService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TripPlanController.class)
@DisplayName("TripPlanController WebMvc (updated with places endpoints)")
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
        clearInvocations(tripPlanService);
        reset(tripPlanService);
    }

    @Test
    void shouldCreateTripPlan() throws Exception {
        var request = new CreateTripPlanRequest("spotify:123", "Plan 1", "Trip Desc");

        TripPlan returned = TripPlan.builder()
                .id(1L)
                .name("Plan 1")
                .description("Trip Desc")
                .build();

        when(tripPlanService.createTripPlan(any(), any(), any())).thenReturn(returned);

        mockMvc.perform(post("/api/trip-plans/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Plan 1"))
                .andExpect(jsonPath("$.description").value("Trip Desc"));

        verify(tripPlanService).createTripPlan("spotify:123", "Plan 1", "Trip Desc");
    }

    @Test
    void shouldGetUserTripPlans() throws Exception {
        TripPlan p1 = TripPlan.builder().id(10L).name("A").description("D").build();
        TripPlan p2 = TripPlan.builder().id(11L).name("B").description("E").build();

        when(tripPlanService.getUserTripPlans("spotify:123"))
                .thenReturn(List.of(p1, p2));

        mockMvc.perform(get("/api/trip-plans/user")
                        .param("spotifyId", "spotify:123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10L))
                .andExpect(jsonPath("$[1].id").value(11L));

        verify(tripPlanService).getUserTripPlans("spotify:123");
    }

    @Test
    void shouldDeleteTripPlan() throws Exception {
        mockMvc.perform(delete("/api/trip-plans/1"))
                .andExpect(status().isNoContent());

        verify(tripPlanService).deleteTripPlan(1L);
    }

    @Test
    void shouldAddPlaceToTrip() throws Exception {
        TripPlaceDto placeDto = new TripPlaceDto(1L, "Place", 1.1, 2.2, "Category", 1L, 1);

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

    @Test
    void shouldReturnPlacesForTripPlan() throws Exception {
        TripPlan plan = TripPlan.builder().id(77L).build();

        TripPlace place1 = TripPlace.builder()
                .id(100L)
                .displayName("Wawel")
                .lat(50.054)
                .lon(19.936)
                .category("Castle")
                .tripPlan(plan)
                .build();

        TripPlace place2 = TripPlace.builder()
                .id(101L)
                .displayName("Rynek")
                .lat(50.061)
                .lon(19.938)
                .category("Square")
                .tripPlan(plan)
                .build();

        when(tripPlanService.getPlacesForTripPlan(77L)).thenReturn(List.of(place1, place2));

        mockMvc.perform(get("/api/trip-plans/77/places"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(100L))
                .andExpect(jsonPath("$[0].displayName").value("Wawel"))
                .andExpect(jsonPath("$[1].id").value(101L))
                .andExpect(jsonPath("$[1].category").value("Square"));

        verify(tripPlanService).getPlacesForTripPlan(77L);
    }

    @Test
    void shouldReorderPlaces() throws Exception {
        ReorderPlacesRequest req = new ReorderPlacesRequest(List.of(5L, 2L, 7L));

        mockMvc.perform(put("/api/trip-plans/99/places/reorder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNoContent());

        verify(tripPlanService).reorderPlaces(99L, List.of(5L, 2L, 7L));
    }
}
