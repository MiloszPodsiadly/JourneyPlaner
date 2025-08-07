package com.milosz.podsiadly.userservice.entity;

import org.junit.jupiter.api.*;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("User Entity Unit Tests")
class UserTest {

    private User.UserBuilder userBuilder;
    private List<TripPlan> tripPlans;

    @BeforeAll
    static void beforeAllTests() {
        System.out.println("🔧 [BeforeAll] Starting User tests...");
    }

    @AfterAll
    static void afterAllTests() {
        System.out.println("✅ [AfterAll] User tests completed.");
    }

    @BeforeEach
    void setUp() {
        System.out.println("➡️ [BeforeEach] Setting up test data...");

        tripPlans = List.of(
                TripPlan.builder().id(1L).name("Trip 1").build(),
                TripPlan.builder().id(2L).name("Trip 2").build()
        );

        userBuilder = User.builder()
                .id(101L)
                .spotifyId("spotify:user:abc123")
                .name("Test User")
                .email("test@example.com")
                .tripPlans(tripPlans);
    }

    @AfterEach
    void tearDown() {
        userBuilder = null;
        tripPlans = null;
        System.out.println("⬅️ [AfterEach] Test finished. Cleaning up...");
    }

    @Test
    @DisplayName("Should build User with correct values")
    void shouldBuildUserCorrectly() {
        User user = userBuilder.build();

        assertThat(user.getId()).isEqualTo(101L);
        assertThat(user.getSpotifyId()).isEqualTo("spotify:user:abc123");
        assertThat(user.getName()).isEqualTo("Test User");
        assertThat(user.getEmail()).isEqualTo("test@example.com");
        assertThat(user.getTripPlans()).hasSize(2);
    }

    @Test
    @DisplayName("Should override builder fields")
    void shouldOverrideBuilderFields() {
        User user = userBuilder
                .name("New Name")
                .email("new@example.com")
                .build();

        assertThat(user.getName()).isEqualTo("New Name");
        assertThat(user.getEmail()).isEqualTo("new@example.com");
    }

    @Test
    @DisplayName("Should allow setting fields manually via setters")
    void shouldAllowSetters() {
        User user = new User();
        user.setId(999L);
        user.setSpotifyId("spotify:user:xyz789");
        user.setName("Setter User");
        user.setEmail("setter@example.com");
        user.setTripPlans(tripPlans);

        assertThat(user.getId()).isEqualTo(999L);
        assertThat(user.getSpotifyId()).isEqualTo("spotify:user:xyz789");
        assertThat(user.getName()).isEqualTo("Setter User");
        assertThat(user.getEmail()).isEqualTo("setter@example.com");
        assertThat(user.getTripPlans()).isEqualTo(tripPlans);
    }
}