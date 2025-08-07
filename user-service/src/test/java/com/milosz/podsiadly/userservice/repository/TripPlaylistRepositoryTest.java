package com.milosz.podsiadly.userservice.repository;

import com.milosz.podsiadly.userservice.entity.TripPlan;
import com.milosz.podsiadly.userservice.entity.TripPlaylist;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("TripPlaylistRepository Integration Tests")
class TripPlaylistRepositoryTest {

    @Autowired
    private TripPlaylistRepository tripPlaylistRepository;

    @Autowired
    private TripPlanRepository tripPlanRepository;

    private TripPlan tripPlan;
    private TripPlaylist tripPlaylist;

    @BeforeAll
    static void beforeAll() {
        System.out.println("🔧 [BeforeAll] Starting TripPlaylistRepository tests...");
    }

    @AfterAll
    static void afterAll() {
        System.out.println("✅ [AfterAll] TripPlaylistRepository tests completed.");
    }

    @BeforeEach
    void setUp() {
        System.out.println("➡️ [BeforeEach] Setting up test data...");

        tripPlan = tripPlanRepository.save(
                TripPlan.builder()
                        .name("Music Trip")
                        .description("Playlist test trip")
                        .build()
        );

        tripPlaylist = TripPlaylist.builder()
                .playlistId("spotify:playlist:test123")
                .name("Test Playlist")
                .tripPlan(tripPlan)
                .build();
    }

    @AfterEach
    void tearDown() {
        tripPlaylistRepository.deleteAll();
        tripPlanRepository.deleteAll();

        System.out.println("⬅️ [AfterEach] Test finished. Cleaning up...");
    }

    @Test
    @DisplayName("Should save and retrieve TripPlaylist by ID")
    void shouldSaveAndFindById() {
        TripPlaylist saved = tripPlaylistRepository.save(tripPlaylist);

        Optional<TripPlaylist> found = tripPlaylistRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test Playlist");
        assertThat(found.get().getPlaylistId()).isEqualTo("spotify:playlist:test123");
        assertThat(found.get().getTripPlan().getId()).isEqualTo(tripPlan.getId());
    }

    @Test
    @DisplayName("Should return all TripPlaylists")
    void shouldReturnAllTripPlaylists() {
        TripPlaylist playlistA = TripPlaylist.builder()
                .playlistId("spotify:playlist:a")
                .name("Chill Vibes")
                .tripPlan(tripPlan)
                .build();

        TripPlaylist playlistB = TripPlaylist.builder()
                .playlistId("spotify:playlist:b")
                .name("Party Time")
                .tripPlan(tripPlan)
                .build();

        tripPlaylistRepository.saveAll(List.of(playlistA, playlistB));

        List<TripPlaylist> all = tripPlaylistRepository.findAll();

        assertThat(all).hasSize(2);
        assertThat(all).extracting(TripPlaylist::getName)
                .containsExactlyInAnyOrder("Chill Vibes", "Party Time");
    }

    @Test
    @DisplayName("Should delete TripPlaylist")
    void shouldDeleteTripPlaylist() {
        TripPlaylist saved = tripPlaylistRepository.save(tripPlaylist);

        tripPlaylistRepository.deleteById(saved.getId());

        Optional<TripPlaylist> deleted = tripPlaylistRepository.findById(saved.getId());
        assertThat(deleted).isNotPresent();
    }
}