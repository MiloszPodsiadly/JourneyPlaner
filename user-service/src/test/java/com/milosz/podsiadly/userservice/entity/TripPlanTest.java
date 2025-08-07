package com.milosz.podsiadly.userservice.entity;

import org.junit.jupiter.api.*;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TripPlan Entity Unit Tests")
class TripPlanTest {

    private TripPlan.TripPlanBuilder tripPlanBuilder;
    private User user;
    private List<TripPlace> places;
    private List<TripPlaylist> playlists;

    @BeforeAll
    static void beforeAllTests() {
        System.out.println("🔧 [BeforeAll] Starting TripPlan tests...");
    }

    @AfterAll
    static void afterAllTests() {
        System.out.println("✅ [AfterAll] TripPlan tests completed.");
    }

    @BeforeEach
    void setUp() {
        System.out.println("➡️ [BeforeEach] Setting up test data...");

        user = User.builder()
                .id(100L)
                .build();

        places = List.of(
                TripPlace.builder().id(1L).displayName("Place 1").build(),
                TripPlace.builder().id(2L).displayName("Place 2").build()
        );

        playlists = List.of(
                TripPlaylist.builder().id(1L).name("Playlist 1").build(),
                TripPlaylist.builder().id(2L).name("Playlist 2").build()
        );

        tripPlanBuilder = TripPlan.builder()
                .id(10L)
                .name("My Trip")
                .description("Test trip description")
                .user(user)
                .places(places)
                .playlists(playlists);
    }

    @AfterEach
    void tearDown() {
        tripPlanBuilder = null;
        user = null;
        places = null;
        playlists = null;

        System.out.println("⬅️ [AfterEach] Test finished. Cleaning up...");
    }

    @Test
    @DisplayName("Should build TripPlan with correct values")
    void shouldBuildTripPlanCorrectly() {
        TripPlan tripPlan = tripPlanBuilder.build();

        assertThat(tripPlan.getId()).isEqualTo(10L);
        assertThat(tripPlan.getName()).isEqualTo("My Trip");
        assertThat(tripPlan.getDescription()).isEqualTo("Test trip description");
        assertThat(tripPlan.getUser()).isEqualTo(user);
        assertThat(tripPlan.getPlaces()).hasSize(2);
        assertThat(tripPlan.getPlaylists()).hasSize(2);
    }

    @Test
    @DisplayName("Should override fields in builder")
    void shouldOverrideFields() {
        TripPlan tripPlan = tripPlanBuilder
                .name("Overridden Name")
                .description("New description")
                .build();

        assertThat(tripPlan.getName()).isEqualTo("Overridden Name");
        assertThat(tripPlan.getDescription()).isEqualTo("New description");
    }

    @Test
    @DisplayName("Should allow setting fields via setters")
    void shouldAllowManualSetters() {
        TripPlan tripPlan = new TripPlan();
        tripPlan.setId(999L);
        tripPlan.setName("Setter Plan");
        tripPlan.setDescription("Set by setter");
        tripPlan.setUser(user);
        tripPlan.setPlaces(places);
        tripPlan.setPlaylists(playlists);

        assertThat(tripPlan.getId()).isEqualTo(999L);
        assertThat(tripPlan.getName()).isEqualTo("Setter Plan");
        assertThat(tripPlan.getDescription()).isEqualTo("Set by setter");
        assertThat(tripPlan.getUser()).isEqualTo(user);
        assertThat(tripPlan.getPlaces()).isEqualTo(places);
        assertThat(tripPlan.getPlaylists()).isEqualTo(playlists);
    }
}