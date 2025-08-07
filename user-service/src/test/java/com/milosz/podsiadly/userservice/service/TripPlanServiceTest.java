package com.milosz.podsiadly.userservice.service;

import com.milosz.podsiadly.userservice.entity.TripPlan;
import com.milosz.podsiadly.userservice.entity.User;
import com.milosz.podsiadly.userservice.repository.TripPlaceRepository;
import com.milosz.podsiadly.userservice.repository.TripPlanRepository;
import com.milosz.podsiadly.userservice.repository.TripPlaylistRepository;
import com.milosz.podsiadly.userservice.repository.UserRepository;

import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@DisplayName("TripPlanService Unit Tests")
class TripPlanServiceTest {

    private TripPlanService tripPlanService;
    private TripPlanRepository tripPlanRepository;
    private TripPlaceRepository tripPlaceRepository;
    private TripPlaylistRepository tripPlaylistRepository;
    private UserRepository userRepository;

    private User testUser;

    @BeforeAll
    static void beforeAll() {
        System.out.println("🔧 [BeforeAll] Starting TripPlanService tests...");
    }

    @AfterAll
    static void afterAll() {
        System.out.println("✅ [AfterAll] TripPlanService tests completed.");
    }

    @BeforeEach
    void setUp() {
        System.out.println("➡️ [BeforeEach] Setting up test data...");

        tripPlanRepository = mock(TripPlanRepository.class);
        tripPlaceRepository = mock(TripPlaceRepository.class);
        tripPlaylistRepository = mock(TripPlaylistRepository.class);
        userRepository = mock(UserRepository.class);

        tripPlanService = new TripPlanServiceImpl(
                userRepository,
                tripPlanRepository,
                tripPlaceRepository,
                tripPlaylistRepository
        );

        testUser = User.builder()
                .id(1L)
                .spotifyId("spotify:user:test")
                .name("Test User")
                .email("test@example.com")
                .build();
    }

    @AfterEach
    void tearDown() {
        tripPlanRepository = null;
        tripPlaceRepository = null;
        tripPlaylistRepository = null;
        userRepository = null;
        tripPlanService = null;
        testUser = null;

        System.out.println("⬅️ [AfterEach] Test finished. Cleaning up...");
    }

    @Test
    @DisplayName("Should create a trip plan for user")
    void shouldCreateTripPlan() {
        String name = "Summer Trip";
        String description = "Go to sea";

        when(userRepository.findBySpotifyId(testUser.getSpotifyId()))
                .thenReturn(Optional.of(testUser));

        ArgumentCaptor<TripPlan> planCaptor = ArgumentCaptor.forClass(TripPlan.class);
        when(tripPlanRepository.save(any(TripPlan.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TripPlan result = tripPlanService.createTripPlan(testUser.getSpotifyId(), name, description);

        assertThat(result.getName()).isEqualTo(name);
        assertThat(result.getDescription()).isEqualTo(description);
        assertThat(result.getUser()).isEqualTo(testUser);

        verify(tripPlanRepository).save(planCaptor.capture());
        TripPlan captured = planCaptor.getValue();

        assertThat(captured.getUser().getSpotifyId()).isEqualTo(testUser.getSpotifyId());
    }

    @Test
    @DisplayName("Should return user's trip plans")
    void shouldReturnUserTripPlans() {
        TripPlan plan1 = TripPlan.builder().id(1L).name("Trip 1").user(testUser).build();
        TripPlan plan2 = TripPlan.builder().id(2L).name("Trip 2").user(testUser).build();

        when(userRepository.findBySpotifyId(testUser.getSpotifyId()))
                .thenReturn(Optional.of(testUser));
        when(tripPlanRepository.findByUser(testUser)).thenReturn(List.of(plan1, plan2));

        List<TripPlan> result = tripPlanService.getUserTripPlans(testUser.getSpotifyId());

        assertThat(result).hasSize(2);
        assertThat(result).extracting(TripPlan::getName)
                .containsExactlyInAnyOrder("Trip 1", "Trip 2");
    }

    @Test
    @DisplayName("Should delete trip plan by ID")
    void shouldDeleteTripPlan() {
        Long id = 999L;

        tripPlanService.deleteTripPlan(id);

        verify(tripPlanRepository).deleteById(id);
    }

    @Test
    @DisplayName("Should add a place to a trip plan")
    void shouldAddPlaceToTrip() {
        Long tripPlanId = 1L;
        String placeName = "Mountains";
        double lat = 50.0;
        double lon = 19.9;

        TripPlan plan = TripPlan.builder().id(tripPlanId).name("Mountain Trip").build();
        when(tripPlanRepository.findById(tripPlanId)).thenReturn(Optional.of(plan));

        tripPlanService.addPlaceToTrip(tripPlanId, placeName, lat, lon);

        verify(tripPlaceRepository).save(argThat(place ->
                place.getTripPlan().equals(plan) &&
                        place.getDisplayName().equals(placeName) &&
                        place.getLat() == lat &&
                        place.getLon() == lon
        ));
    }

    @Test
    @DisplayName("Should add a playlist to a trip plan")
    void shouldAddPlaylistToTrip() {
        Long tripPlanId = 1L;
        String playlistId = "spotify:playlist:abc123";
        String playlistName = "Chill Vibes";

        TripPlan plan = TripPlan.builder().id(tripPlanId).name("Relax Trip").build();
        when(tripPlanRepository.findById(tripPlanId)).thenReturn(Optional.of(plan));

        tripPlanService.addPlaylistToTrip(tripPlanId, playlistId, playlistName);

        verify(tripPlaylistRepository).save(argThat(playlist ->
                playlist.getTripPlan().equals(plan) &&
                        playlist.getPlaylistId().equals(playlistId) &&
                        playlist.getName().equals(playlistName)
        ));
    }

    @Test
    @DisplayName("Should remove a trip place by ID")
    void shouldRemoveTripPlace() {
        Long placeId = 123L;

        tripPlanService.removePlaceFromTrip(placeId);

        verify(tripPlaceRepository).deleteById(placeId);
    }

    @Test
    @DisplayName("Should remove a playlist from trip by ID")
    void shouldRemoveTripPlaylist() {
        Long playlistId = 456L;

        tripPlanService.removePlaylistFromTrip(playlistId);

        verify(tripPlaylistRepository).deleteById(playlistId);
    }

    @Test
    @DisplayName("Should update a trip plan's name and description")
    void shouldUpdateTripPlan() {
        Long planId = 1L;
        TripPlan existingPlan = TripPlan.builder().id(planId).name("Old Name").description("Old Desc").build();

        when(tripPlanRepository.findById(planId)).thenReturn(Optional.of(existingPlan));
        when(tripPlanRepository.save(any())).thenReturn(existingPlan);

        tripPlanService.updateTripPlan(planId, "New Name", "New Desc");

        assertThat(existingPlan.getName()).isEqualTo("New Name");
        assertThat(existingPlan.getDescription()).isEqualTo("New Desc");

        verify(tripPlanRepository).save(existingPlan);
    }
}
