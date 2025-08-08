package com.milosz.podsiadly.routeservice.controller;

import com.milosz.podsiadly.routeservice.dto.LocationDto;
import com.milosz.podsiadly.routeservice.service.RouteService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RouteController.class)
@DisplayName("🧭 RouteController WebMvc Tests")
class RouteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RouteService routeService;

    @TestConfiguration
    static class MockConfig {
        @Bean
        RouteService routeService() {
            return mock(RouteService.class);
        }
    }

    @BeforeAll
    static void beforeAll() {
        System.out.println("🔧 [BeforeAll] Starting RouteController tests...");
    }

    @AfterAll
    static void afterAll() {
        System.out.println("✅ [AfterAll] RouteController tests completed.");
    }

    @BeforeEach
    void setUp() {
        System.out.println("➡️ [BeforeEach] Preparing test case...");
    }

    @AfterEach
    void tearDown() {
        System.out.println("⬅️ [AfterEach] Cleaning up...");
        clearInvocations(routeService);
        reset(routeService);
    }

    @Test
    @DisplayName("🟢 /search returns 200 with single-element array when service returns a location")
    void search_returnsOkWithArray() throws Exception {
        LocationDto dto = new LocationDto("52.2297", "21.0122", "Warsaw, Poland");
        when(routeService.searchPlace("Warsaw")).thenReturn(dto);

        mockMvc.perform(get("/api/route/search")
                        .param("q", "Warsaw")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));

        verify(routeService).searchPlace("Warsaw");
    }

    @Test
    @DisplayName("🟡 /search returns 404 when service returns null")
    void search_returnsNotFoundWhenNull() throws Exception {
        when(routeService.searchPlace("Nowhere")).thenReturn(null);

        mockMvc.perform(get("/api/route/search")
                        .param("q", "Nowhere"))
                .andExpect(status().isNotFound());

        verify(routeService).searchPlace("Nowhere");
    }

    @Test
    @DisplayName("🔴 /search returns 500 when service throws")
    void search_returnsInternalErrorOnException() throws Exception {
        when(routeService.searchPlace(anyString())).thenThrow(new RuntimeException("boom"));

        mockMvc.perform(get("/api/route/search")
                        .param("q", "Crash"))
                .andExpect(status().isInternalServerError());

        verify(routeService).searchPlace("Crash");
    }

    @Test
    @DisplayName("🟢 /top-places returns 200 with array of places when service returns list")
    void topPlaces_returnsOkWithArray() throws Exception {
        LocationDto dto1 = new LocationDto("50.0614", "19.9366", "Krakow, Poland");
        LocationDto dto2 = new LocationDto("50.0647", "19.9450", "Old Town, Krakow");
        when(routeService.searchTopPlaces("Krakow")).thenReturn(List.of(dto1, dto2));

        mockMvc.perform(get("/api/route/top-places")
                        .param("city", "Krakow")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));

        verify(routeService).searchTopPlaces("Krakow");
    }

    @Test
    @DisplayName("🟡 /top-places returns 404 when service returns empty list")
    void topPlaces_returnsNotFoundWhenEmpty() throws Exception {
        when(routeService.searchTopPlaces("EmptyCity")).thenReturn(List.of());

        mockMvc.perform(get("/api/route/top-places")
                        .param("city", "EmptyCity"))
                .andExpect(status().isNotFound());

        verify(routeService).searchTopPlaces("EmptyCity");
    }

    @Test
    @DisplayName("🔴 /top-places returns 500 when service throws")
    void topPlaces_returnsInternalErrorOnException() throws Exception {
        when(routeService.searchTopPlaces(anyString())).thenThrow(new RuntimeException("oh no"));

        mockMvc.perform(get("/api/route/top-places")
                        .param("city", "CrashCity"))
                .andExpect(status().isInternalServerError());

        verify(routeService).searchTopPlaces("CrashCity");
    }

    @Test
    @DisplayName("🟢 /discover returns 200 with array when service returns non-empty array")
    void discover_returnsOkWithArray() throws Exception {
        LocationDto[] arr = {
                new LocationDto("54.3520", "18.6466", "Gdansk, Poland"),
                new LocationDto("54.3521", "18.6467", "Museum of Gdansk")
        };
        when(routeService.searchPlacesByCategory("Gdansk", "museums")).thenReturn(arr);

        mockMvc.perform(get("/api/route/discover")
                        .param("city", "Gdansk")
                        .param("category", "museums")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));

        verify(routeService).searchPlacesByCategory("Gdansk", "museums");
    }

    @Test
    @DisplayName("🟡 /discover returns 404 when service returns empty array")
    void discover_returnsNotFoundWhenEmpty() throws Exception {
        when(routeService.searchPlacesByCategory("City", "cats")).thenReturn(new LocationDto[0]);

        mockMvc.perform(get("/api/route/discover")
                        .param("city", "City")
                        .param("category", "cats"))
                .andExpect(status().isNotFound());

        verify(routeService).searchPlacesByCategory("City", "cats");
    }

    @Test
    @DisplayName("🔴 /discover returns 500 when service throws")
    void discover_returnsInternalErrorOnException() throws Exception {
        when(routeService.searchPlacesByCategory(anyString(), anyString()))
                .thenThrow(new RuntimeException("fatal"));

        mockMvc.perform(get("/api/route/discover")
                        .param("city", "CrashTown")
                        .param("category", "anything"))
                .andExpect(status().isInternalServerError());

        verify(routeService).searchPlacesByCategory("CrashTown", "anything");
    }
}
