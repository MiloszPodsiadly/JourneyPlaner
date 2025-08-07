package com.milosz.podsiadly.userservice.repository;

import com.milosz.podsiadly.userservice.entity.TripPlan;
import com.milosz.podsiadly.userservice.entity.User;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("TripPlanRepository Integration Tests")
class TripPlanRepositoryTest {

    @Autowired
    private TripPlanRepository tripPlanRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;
    private TripPlan tripPlan;

    @BeforeAll
    static void beforeAll() {
        System.out.println("🔧 [BeforeAll] Starting TripPlanRepository tests...");
    }

    @AfterAll
    static void afterAll() {
        System.out.println("✅ [AfterAll] TripPlanRepository tests completed.");
    }

    @BeforeEach
    void setUp() {
        System.out.println("➡️ [BeforeEach] Setting up test data...");

        user = userRepository.save(
                User.builder()
                        .spotifyId("spotify:user:test123")
                        .name("Test User")
                        .email("test@example.com")
                        .build()
        );

        tripPlan = TripPlan.builder()
                .name("Europe Trip")
                .description("Trip across Europe")
                .user(user)
                .build();
    }

    @AfterEach
    void tearDown() {
        tripPlanRepository.deleteAll();
        userRepository.deleteAll();

        System.out.println("⬅️ [AfterEach] Test finished. Cleaning up...");
    }

    @Test
    @DisplayName("Should save and retrieve TripPlan by ID")
    void shouldSaveAndFindById() {
        TripPlan saved = tripPlanRepository.save(tripPlan);

        Optional<TripPlan> found = tripPlanRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Europe Trip");
        assertThat(found.get().getUser().getSpotifyId()).isEqualTo("spotify:user:test123");
    }

    @Test
    @DisplayName("Should find TripPlans by User")
    void shouldFindTripPlansByUser() {
        TripPlan plan1 = TripPlan.builder()
                .name("City Tour")
                .description("Around the city")
                .user(user)
                .build();

        TripPlan plan2 = TripPlan.builder()
                .name("Beach Escape")
                .description("Relaxing by the sea")
                .user(user)
                .build();

        tripPlanRepository.saveAll(List.of(plan1, plan2));

        List<TripPlan> foundPlans = tripPlanRepository.findByUser(user);

        assertThat(foundPlans).hasSize(2);
        assertThat(foundPlans)
                .extracting(TripPlan::getName)
                .containsExactlyInAnyOrder("City Tour", "Beach Escape");
    }

    @Test
    @DisplayName("Should delete TripPlan")
    void shouldDeleteTripPlan() {
        TripPlan saved = tripPlanRepository.save(tripPlan);

        tripPlanRepository.deleteById(saved.getId());

        Optional<TripPlan> deleted = tripPlanRepository.findById(saved.getId());
        assertThat(deleted).isNotPresent();
    }
}