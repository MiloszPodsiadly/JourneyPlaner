package com.milosz.podsiadly.userservice.mapper;


import com.milosz.podsiadly.userservice.dto.TripPlanDto;
import com.milosz.podsiadly.userservice.dto.TripPlaceDto;
import com.milosz.podsiadly.userservice.entity.*;

import org.junit.jupiter.api.*;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TripPlanMapper Unit Tests")
class TripPlanMapperTest {

    private TripPlan tripPlan;
    private TripPlace tripPlace1;
    private TripPlace tripPlace2;
    private TripPlaylist playlist1;
    private TripPlaylist playlist2;
    private User user;

    @BeforeAll
    static void beforeAll() {
        System.out.println("🔧 [BeforeAll] Starting TripPlanMapper tests...");
    }

    @AfterAll
    static void afterAll() {
        System.out.println("✅ [AfterAll] TripPlanMapper tests completed.");
    }

    @BeforeEach
    void setUp() {
        System.out.println("➡️ [BeforeEach] Setting up test data...");

        user = User.builder()
                .id(42L)
                .name("Test User")
                .build();

        tripPlace1 = TripPlace.builder()
                .id(1L)
                .displayName("Place A")
                .lat(10.0)
                .lon(20.0)
                .category("Park")
                .build();

        tripPlace2 = TripPlace.builder()
                .id(2L)
                .displayName("Place B")
                .lat(30.0)
                .lon(40.0)
                .category("Museum")
                .build();

        playlist1 = TripPlaylist.builder()
                .id(1L)
                .playlistId("spotify:playlist:abc123")
                .name("Morning Vibes")
                .build();

        playlist2 = TripPlaylist.builder()
                .id(2L)
                .playlistId("spotify:playlist:def456")
                .name("Evening Chill")
                .build();

        tripPlan = TripPlan.builder()
                .id(100L)
                .name("My Europe Trip")
                .description("Visiting EU countries")
                .user(user)
                .places(List.of(tripPlace1, tripPlace2))
                .playlists(List.of(playlist1, playlist2))
                .build();
    }

    @AfterEach
    void tearDown() {
        tripPlan = null;
        tripPlace1 = null;
        tripPlace2 = null;
        playlist1 = null;
        playlist2 = null;
        user = null;
        System.out.println("⬅️ [AfterEach] Test finished. Cleaning up...");
    }

    @Test
    @DisplayName("Should map TripPlan to TripPlanDto correctly")
    void shouldMapTripPlanToDtoCorrectly() {
        TripPlanDto dto = TripPlanMapper.toDto(tripPlan);

        assertThat(dto.id()).isEqualTo(100L);
        assertThat(dto.name()).isEqualTo("My Europe Trip");
        assertThat(dto.description()).isEqualTo("Visiting EU countries");
        assertThat(dto.userId()).isEqualTo(42L);
        assertThat(dto.places()).hasSize(2);
        assertThat(dto.playlists()).hasSize(2);

        TripPlaceDto firstPlace = dto.places().get(0);
        assertThat(firstPlace.displayName()).isEqualTo("Place A");
    }

    @Test
    @DisplayName("Should handle null places and playlists")
    void shouldHandleNullLists() {
        tripPlan.setPlaces(null);
        tripPlan.setPlaylists(null);

        TripPlanDto dto = TripPlanMapper.toDto(tripPlan);

        assertThat(dto.places()).isNull();
        assertThat(dto.playlists()).isNull();
    }

    @Test
    @DisplayName("Should handle null user")
    void shouldHandleNullUser() {
        tripPlan.setUser(null);

        TripPlanDto dto = TripPlanMapper.toDto(tripPlan);

        assertThat(dto.userId()).isNull();
    }
}