package com.milosz.podsiadly.userservice.repository;

import com.milosz.podsiadly.userservice.entity.TripPlace;
import com.milosz.podsiadly.userservice.entity.TripPlan;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("TripPlaceRepository Integration Tests")
class TripPlaceRepositoryTest {

    @Autowired
    private TripPlaceRepository tripPlaceRepository;

    @Autowired
    private TripPlanRepository tripPlanRepository;

    private TripPlan tripPlan;
    private TripPlace tripPlace;

    @BeforeAll
    static void beforeAll() {
        System.out.println("🔧 [BeforeAll] Starting TripPlaceRepository tests...");
    }

    @AfterAll
    static void afterAll() {
        System.out.println("✅ [AfterAll] TripPlaceRepository tests completed.");
    }

    @BeforeEach
    void setUp() {
        System.out.println("➡️ [BeforeEach] Setting up test data...");

        tripPlan = tripPlanRepository.save(
                TripPlan.builder()
                        .name("Test Plan")
                        .description("For TripPlace tests")
                        .build()
        );

        tripPlace = TripPlace.builder()
                .displayName("Sample Place")
                .lat(10.0)
                .lon(20.0)
                .category("Test Category")
                .sortOrder(0)
                .tripPlan(tripPlan)
                .build();
    }

    @AfterEach
    void tearDown() {
        tripPlaceRepository.deleteAll();
        tripPlanRepository.deleteAll();
        System.out.println("⬅️ [AfterEach] Test finished. Cleaning up...");
    }

    @Test
    @DisplayName("Should save and retrieve TripPlace by ID")
    void shouldSaveAndFindById() {
        TripPlace saved = tripPlaceRepository.save(tripPlace);

        Optional<TripPlace> found = tripPlaceRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getDisplayName()).isEqualTo("Sample Place");
        assertThat(found.get().getLat()).isEqualTo(10.0);
        assertThat(found.get().getLon()).isEqualTo(20.0);
        assertThat(found.get().getCategory()).isEqualTo("Test Category");
        assertThat(found.get().getSortOrder()).isEqualTo(0);
        assertThat(found.get().getTripPlan().getId()).isEqualTo(tripPlan.getId());
    }

    @Test
    @DisplayName("Should return all TripPlaces")
    void shouldReturnAllTripPlaces() {
        TripPlace placeA = TripPlace.builder()
                .displayName("Place A")
                .lat(1.0)
                .lon(1.0)
                .category("A")
                .sortOrder(1)
                .tripPlan(tripPlan)
                .build();

        TripPlace placeB = TripPlace.builder()
                .displayName("Place B")
                .lat(2.0)
                .lon(2.0)
                .category("B")
                .sortOrder(2)
                .tripPlan(tripPlan)
                .build();

        tripPlaceRepository.saveAll(List.of(placeA, placeB));

        List<TripPlace> all = tripPlaceRepository.findAll();

        assertThat(all).hasSize(2);
        assertThat(all).extracting(TripPlace::getDisplayName)
                .containsExactlyInAnyOrder("Place A", "Place B");
    }

    @Test
    @DisplayName("Should delete TripPlace")
    void shouldDeleteTripPlace() {
        TripPlace saved = tripPlaceRepository.save(tripPlace);

        tripPlaceRepository.deleteById(saved.getId());

        Optional<TripPlace> deleted = tripPlaceRepository.findById(saved.getId());
        assertThat(deleted).isNotPresent();
    }

    @Test
    @DisplayName("findByTripPlanIdOrderBySortOrderAsc returns places in ascending sort order")
    void shouldFindPlacesOrderedBySortOrder() {
        TripPlace p1 = TripPlace.builder()
                .displayName("P1").lat(1.0).lon(1.0).category("X").sortOrder(2).tripPlan(tripPlan).build();
        TripPlace p2 = TripPlace.builder()
                .displayName("P2").lat(2.0).lon(2.0).category("Y").sortOrder(0).tripPlan(tripPlan).build();
        TripPlace p3 = TripPlace.builder()
                .displayName("P3").lat(3.0).lon(3.0).category("Z").sortOrder(1).tripPlan(tripPlan).build();

        tripPlaceRepository.saveAll(List.of(p1, p2, p3));

        List<TripPlace> ordered = tripPlaceRepository.findByTripPlanIdOrderBySortOrderAsc(tripPlan.getId());

        assertThat(ordered).extracting(TripPlace::getDisplayName)
                .containsExactly("P2", "P3", "P1");
    }

    @Test
    @DisplayName("findMaxSortOrderByTripPlanId returns max sortOrder or -1 when none")
    void shouldFindMaxSortOrder() {
        Integer none = tripPlaceRepository.findMaxSortOrderByTripPlanId(tripPlan.getId());
        assertThat(none).isEqualTo(-1);

        TripPlace p1 = TripPlace.builder().displayName("A").lat(0d).lon(0d).category("C").sortOrder(3).tripPlan(tripPlan).build();
        TripPlace p2 = TripPlace.builder().displayName("B").lat(0d).lon(0d).category("C").sortOrder(7).tripPlan(tripPlan).build();
        tripPlaceRepository.saveAll(List.of(p1, p2));

        Integer max = tripPlaceRepository.findMaxSortOrderByTripPlanId(tripPlan.getId());
        assertThat(max).isEqualTo(7);
    }
}
