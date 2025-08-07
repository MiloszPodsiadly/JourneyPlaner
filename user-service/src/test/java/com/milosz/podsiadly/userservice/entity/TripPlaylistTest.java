package com.milosz.podsiadly.userservice.entity;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TripPlaylist Entity Unit Tests")
class TripPlaylistTest {

    private TripPlaylist.TripPlaylistBuilder tripPlaylistBuilder;
    private TripPlan tripPlan;

    @BeforeAll
    static void beforeAllTests() {
        System.out.println("🔧 [BeforeAll] Starting TripPlaylist tests...");
    }

    @AfterAll
    static void afterAllTests() {
        System.out.println("✅ [AfterAll] TripPlaylist tests completed.");
    }

    @BeforeEach
    void setUp() {
        System.out.println("➡️ [BeforeEach] Setting up test data...");

        tripPlan = TripPlan.builder()
                .id(1L)
                .name("Test Trip")
                .build();

        tripPlaylistBuilder = TripPlaylist.builder()
                .id(100L)
                .playlistId("spotify:playlist:123456")
                .name("Chill Vibes")
                .tripPlan(tripPlan);
    }

    @AfterEach
    void tearDown() {
        tripPlaylistBuilder = null;
        tripPlan = null;
        System.out.println("⬅️ [AfterEach] Test finished. Cleaning up...");
    }

    @Test
    @DisplayName("Should build TripPlaylist with correct values")
    void shouldBuildTripPlaylistCorrectly() {
        TripPlaylist playlist = tripPlaylistBuilder.build();

        assertThat(playlist.getId()).isEqualTo(100L);
        assertThat(playlist.getPlaylistId()).isEqualTo("spotify:playlist:123456");
        assertThat(playlist.getName()).isEqualTo("Chill Vibes");
        assertThat(playlist.getTripPlan()).isEqualTo(tripPlan);
    }

    @Test
    @DisplayName("Should override some builder fields")
    void shouldOverrideFieldsInBuilder() {
        TripPlaylist playlist = tripPlaylistBuilder
                .name("Party Mix")
                .playlistId("spotify:playlist:999999")
                .build();

        assertThat(playlist.getName()).isEqualTo("Party Mix");
        assertThat(playlist.getPlaylistId()).isEqualTo("spotify:playlist:999999");
    }

    @Test
    @DisplayName("Should allow setting fields manually via setters")
    void shouldAllowSetters() {
        TripPlaylist playlist = new TripPlaylist();
        playlist.setId(321L);
        playlist.setName("Workout Playlist");
        playlist.setPlaylistId("spotify:playlist:654321");
        playlist.setTripPlan(tripPlan);

        assertThat(playlist.getId()).isEqualTo(321L);
        assertThat(playlist.getName()).isEqualTo("Workout Playlist");
        assertThat(playlist.getPlaylistId()).isEqualTo("spotify:playlist:654321");
        assertThat(playlist.getTripPlan()).isEqualTo(tripPlan);
    }
}