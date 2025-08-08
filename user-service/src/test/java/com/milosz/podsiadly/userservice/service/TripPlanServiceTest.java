package com.milosz.podsiadly.userservice.service;

import com.milosz.podsiadly.userservice.entity.TripPlace;
import com.milosz.podsiadly.userservice.entity.TripPlan;
import com.milosz.podsiadly.userservice.entity.User;
import com.milosz.podsiadly.userservice.repository.TripPlaceRepository;
import com.milosz.podsiadly.userservice.repository.TripPlanRepository;
import com.milosz.podsiadly.userservice.repository.TripPlaylistRepository;
import com.milosz.podsiadly.userservice.repository.UserRepository;

import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
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
    @Test
    @DisplayName("addPlaceToTrip: set sortOrder = max+1 when position exist")
    void addPlaceToTrip_setsNextSortOrderFromMax() {
        Long planId = 1L;
        TripPlan plan = TripPlan.builder().id(planId).name("P").build();
        when(tripPlanRepository.findById(planId)).thenReturn(Optional.of(plan));
        when(tripPlaceRepository.findMaxSortOrderByTripPlanId(planId)).thenReturn(4);

        tripPlanService.addPlaceToTrip(planId, "Place", 1.0, 2.0);

        verify(tripPlaceRepository).save(argThat(p ->
                p.getTripPlan().equals(plan)
                        && p.getDisplayName().equals("Place")
                        && p.getLat() == 1.0
                        && p.getLon() == 2.0
                        && p.getSortOrder() == 5
        ));
    }

    @Test
    @DisplayName("addPlaceToTrip: set sortOrder = 0 when no position (max=null)")
    void addPlaceToTrip_setsZeroWhenNoExisting() {
        Long planId = 2L;
        TripPlan plan = TripPlan.builder().id(planId).name("P").build();
        when(tripPlanRepository.findById(planId)).thenReturn(Optional.of(plan));
        when(tripPlaceRepository.findMaxSortOrderByTripPlanId(planId)).thenReturn(null);

        tripPlanService.addPlaceToTrip(planId, "First", 3.3, 4.4);

        verify(tripPlaceRepository).save(argThat(p -> p.getSortOrder() == 0));
    }

    @Test
    @DisplayName("getPlacesForTripPlan: return list in growing order")
    void getPlacesForTripPlan_returnsOrderedList() {
        Long planId = 7L;
        TripPlace a = TripPlace.builder().id(10L).displayName("A").sortOrder(0).tripPlan(TripPlan.builder().id(planId).build()).build();
        TripPlace b = TripPlace.builder().id(11L).displayName("B").sortOrder(1).tripPlan(TripPlan.builder().id(planId).build()).build();
        when(tripPlaceRepository.findByTripPlanIdOrderBySortOrderAsc(planId)).thenReturn(List.of(a, b));

        var out = tripPlanService.getPlacesForTripPlan(planId);

        assertThat(out).hasSize(2);
        assertThat(out.get(0).getId()).isEqualTo(10L);
        assertThat(out.get(1).getId()).isEqualTo(11L);
        verify(tripPlaceRepository).findByTripPlanIdOrderBySortOrderAsc(planId);
    }

    @Test
    @DisplayName("reorderPlaces: success — updateSortOrder called by order")
    void reorderPlaces_updatesSortOrderInSequence() {
        Long planId = 42L;
        TripPlace p1 = TripPlace.builder().id(100L).sortOrder(0).tripPlan(TripPlan.builder().id(planId).build()).build();
        TripPlace p2 = TripPlace.builder().id(200L).sortOrder(1).tripPlan(TripPlan.builder().id(planId).build()).build();
        TripPlace p3 = TripPlace.builder().id(300L).sortOrder(2).tripPlan(TripPlan.builder().id(planId).build()).build();
        when(tripPlaceRepository.findByTripPlanIdOrderBySortOrderAsc(planId)).thenReturn(List.of(p1, p2, p3));

        var newOrder = List.of(300L, 100L, 200L);
        tripPlanService.reorderPlaces(planId, newOrder);

        InOrder in = inOrder(tripPlaceRepository);
        in.verify(tripPlaceRepository).updateSortOrder(300L, 0);
        in.verify(tripPlaceRepository).updateSortOrder(100L, 1);
        in.verify(tripPlaceRepository).updateSortOrder(200L, 2);
        in.verifyNoMoreInteractions();
    }

    @Test
    @DisplayName("reorderPlaces: throws IllegalArgumentException for empty list")
    void reorderPlaces_throwsOnEmptyList() {
        assertThatThrownBy(() -> tripPlanService.reorderPlaces(1L, List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be empty");
    }

    @Test
    @DisplayName("reorderPlaces: throws when size of the list nis not matching to exisitng place")
    void reorderPlaces_throwsOnSizeMismatch() {
        Long planId = 9L;
        TripPlace only = TripPlace.builder().id(1L).tripPlan(TripPlan.builder().id(planId).build()).build();
        when(tripPlaceRepository.findByTripPlanIdOrderBySortOrderAsc(planId)).thenReturn(List.of(only));

        assertThatThrownBy(() -> tripPlanService.reorderPlaces(planId, List.of(1L, 2L)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("size mismatch");
    }

    @Test
    @DisplayName("reorderPlaces: throws when is ID duplicated")
    void reorderPlaces_throwsOnDuplicateIds() {
        Long planId = 10L;
        TripPlace p1 = TripPlace.builder().id(1L).tripPlan(TripPlan.builder().id(planId).build()).build();
        TripPlace p2 = TripPlace.builder().id(2L).tripPlan(TripPlan.builder().id(planId).build()).build();
        when(tripPlaceRepository.findByTripPlanIdOrderBySortOrderAsc(planId)).thenReturn(List.of(p1, p2));

        assertThatThrownBy(() -> tripPlanService.reorderPlaces(planId, List.of(1L, 1L)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Duplicate");
    }

    @Test
    @DisplayName("reorderPlaces: throws when list include ID without plan")
    void reorderPlaces_throwsOnUnknownIds() {
        Long planId = 11L;
        TripPlace p1 = TripPlace.builder().id(1L).tripPlan(TripPlan.builder().id(planId).build()).build();
        TripPlace p2 = TripPlace.builder().id(2L).tripPlan(TripPlan.builder().id(planId).build()).build();
        when(tripPlaceRepository.findByTripPlanIdOrderBySortOrderAsc(planId)).thenReturn(List.of(p1, p2));

        assertThatThrownBy(() -> tripPlanService.reorderPlaces(planId, List.of(1L, 999L)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown place id");
    }
}
