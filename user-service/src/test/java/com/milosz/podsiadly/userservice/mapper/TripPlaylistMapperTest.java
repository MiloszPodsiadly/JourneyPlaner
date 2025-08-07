package com.milosz.podsiadly.userservice.mapper;

import com.milosz.podsiadly.userservice.dto.TripPlaylistDto;
import com.milosz.podsiadly.userservice.entity.TripPlan;
import com.milosz.podsiadly.userservice.entity.TripPlaylist;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TripPlaylistMapper Unit Tests")
class TripPlaylistMapperTest {

    private TripPlan tripPlan;
    private TripPlaylist playlist;
    private TripPlaylistDto playlistDto;

    @BeforeAll
    static void beforeAllTests() {
        System.out.println("🔧 [BeforeAll] Starting TripPlaylistMapper tests...");
    }

    @AfterAll
    static void afterAllTests() {
        System.out.println("✅ [AfterAll] TripPlaylistMapper tests completed.");
    }

    @BeforeEach
    void setUp() {
        System.out.println("➡️ [BeforeEach] Setting up test data...");

        tripPlan = TripPlan.builder()
                .id(1L)
                .name("Trip 1")
                .build();

        playlist = TripPlaylist.builder()
                .id(100L)
                .playlistId("spotify:playlist:abc123")
                .name("Roadtrip Mix")
                .tripPlan(tripPlan)
                .build();

        playlistDto = new TripPlaylistDto(
                100L,
                "spotify:playlist:abc123",
                "Roadtrip Mix",
                1L
        );
    }

    @AfterEach
    void tearDown() {
        tripPlan = null;
        playlist = null;
        playlistDto = null;
        System.out.println("⬅️ [AfterEach] Test finished. Cleaning up...");
    }

    @Test
    @DisplayName("Should map TripPlaylist to TripPlaylistDto correctly")
    void shouldMapToDtoCorrectly() {
        TripPlaylistDto dto = TripPlaylistMapper.toDto(playlist);

        assertThat(dto.id()).isEqualTo(100L);
        assertThat(dto.playlistId()).isEqualTo("spotify:playlist:abc123");
        assertThat(dto.name()).isEqualTo("Roadtrip Mix");
        assertThat(dto.tripPlanId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should map TripPlaylistDto to TripPlaylist correctly")
    void shouldMapToEntityCorrectly() {
        TripPlaylist entity = TripPlaylistMapper.toEntity(playlistDto);

        assertThat(entity.getId()).isEqualTo(100L);
        assertThat(entity.getPlaylistId()).isEqualTo("spotify:playlist:abc123");
        assertThat(entity.getName()).isEqualTo("Roadtrip Mix");
        assertThat(entity.getTripPlan()).isNotNull();
        assertThat(entity.getTripPlan().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should handle null tripPlan in entity")
    void shouldHandleNullTripPlanInDto() {
        playlist.setTripPlan(null);

        TripPlaylistDto dto = TripPlaylistMapper.toDto(playlist);

        assertThat(dto.tripPlanId()).isNull();
    }

    @Test
    @DisplayName("Should handle null tripPlanId in DTO")
    void shouldHandleNullTripPlanIdInEntity() {
        TripPlaylistDto dtoWithNullPlan = new TripPlaylistDto(
                101L, "spotify:playlist:zzz999", "Chill List", null
        );

        TripPlaylist entity = TripPlaylistMapper.toEntity(dtoWithNullPlan);

        assertThat(entity.getTripPlan()).isNull();
    }
}