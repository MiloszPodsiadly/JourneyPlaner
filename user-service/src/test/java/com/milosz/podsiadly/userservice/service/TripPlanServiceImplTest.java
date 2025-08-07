package com.milosz.podsiadly.userservice.service;

import com.milosz.podsiadly.userservice.entity.TripPlan;
import com.milosz.podsiadly.userservice.entity.User;
import com.milosz.podsiadly.userservice.repository.*;

import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("TripPlanServiceImpl Unit Tests")
class TripPlanServiceImplTest {

    private TripPlanServiceImpl service;

    private UserRepository userRepository;
    private TripPlanRepository tripPlanRepository;
    private TripPlaceRepository tripPlaceRepository;
    private TripPlaylistRepository tripPlaylistRepository;

    private User user;
    private TripPlan tripPlan;

    @BeforeAll
    static void beforeAll() {
        System.out.println("🔧 [BeforeAll] Starting TripPlanServiceImpl tests...");
    }

    @AfterAll
    static void afterAll() {
        System.out.println("✅ [AfterAll] TripPlanServiceImpl tests completed.");
    }

    @BeforeEach
    void setUp() {
        System.out.println("➡️ [BeforeEach] Setting up test data...");

        userRepository = mock(UserRepository.class);
        tripPlanRepository = mock(TripPlanRepository.class);
        tripPlaceRepository = mock(TripPlaceRepository.class);
        tripPlaylistRepository = mock(TripPlaylistRepository.class);

        service = new TripPlanServiceImpl(
                userRepository,
                tripPlanRepository,
                tripPlaceRepository,
                tripPlaylistRepository
        );

        user = User.builder()
                .id(1L)
                .spotifyId("spotify:test")
                .name("Test User")
                .email("test@example.com")
                .build();

        tripPlan = TripPlan.builder()
                .id(10L)
                .name("Test Plan")
                .description("Test Desc")
                .user(user)
                .build();
    }

    @AfterEach
    void tearDown() {
        service = null;
        user = null;
        tripPlan = null;

        System.out.println("⬅️ [AfterEach] Test finished. Cleaning up...");
    }

    @Test
    @DisplayName("Should create trip plan")
    void shouldCreateTripPlan() {
        when(userRepository.findBySpotifyId("spotify:test")).thenReturn(Optional.of(user));
        when(tripPlanRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        TripPlan result = service.createTripPlan("spotify:test", "New Plan", "Desc");

        assertThat(result.getName()).isEqualTo("New Plan");
        assertThat(result.getDescription()).isEqualTo("Desc");
        assertThat(result.getUser()).isEqualTo(user);

        verify(userRepository).findBySpotifyId("spotify:test");
        verify(tripPlanRepository).save(any(TripPlan.class));
    }

    @Test
    @DisplayName("Should return all trip plans for user")
    void shouldGetUserTripPlans() {
        when(userRepository.findBySpotifyId("spotify:test")).thenReturn(Optional.of(user));
        when(tripPlanRepository.findByUser(user)).thenReturn(List.of(tripPlan));

        List<TripPlan> result = service.getUserTripPlans("spotify:test");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(tripPlan.getId());

        verify(userRepository).findBySpotifyId("spotify:test");
        verify(tripPlanRepository).findByUser(user);
    }

    @Test
    @DisplayName("Should delete trip plan by ID")
    void shouldDeleteTripPlan() {
        service.deleteTripPlan(10L);
        verify(tripPlanRepository).deleteById(10L);
    }

    @Test
    @DisplayName("Should add place to trip")
    void shouldAddPlaceToTrip() {
        when(tripPlanRepository.findById(10L)).thenReturn(Optional.of(tripPlan));

        service.addPlaceToTrip(10L, "Place", 1.1, 2.2);

        verify(tripPlaceRepository).save(argThat(place ->
                place.getDisplayName().equals("Place")
                        && place.getLat() == 1.1
                        && place.getLon() == 2.2
                        && place.getTripPlan().equals(tripPlan)
        ));
    }

    @Test
    @DisplayName("Should add playlist to trip")
    void shouldAddPlaylistToTrip() {
        when(tripPlanRepository.findById(10L)).thenReturn(Optional.of(tripPlan));

        service.addPlaylistToTrip(10L, "playlist123", "Chill Mix");

        verify(tripPlaylistRepository).save(argThat(p ->
                p.getPlaylistId().equals("playlist123")
                        && p.getName().equals("Chill Mix")
                        && p.getTripPlan().equals(tripPlan)
        ));
    }

    @Test
    @DisplayName("Should remove place from trip")
    void shouldRemovePlaceFromTrip() {
        service.removePlaceFromTrip(55L);
        verify(tripPlaceRepository).deleteById(55L);
    }

    @Test
    @DisplayName("Should remove playlist from trip")
    void shouldRemovePlaylistFromTrip() {
        service.removePlaylistFromTrip(66L);
        verify(tripPlaylistRepository).deleteById(66L);
    }

    @Test
    @DisplayName("Should update trip plan name and description")
    void shouldUpdateTripPlan() {
        when(tripPlanRepository.findById(10L)).thenReturn(Optional.of(tripPlan));
        when(tripPlanRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.updateTripPlan(10L, "Updated Name", "Updated Desc");

        assertThat(tripPlan.getName()).isEqualTo("Updated Name");
        assertThat(tripPlan.getDescription()).isEqualTo("Updated Desc");

        verify(tripPlanRepository).save(tripPlan);
    }
}
