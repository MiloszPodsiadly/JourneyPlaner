package com.milosz.podsiadly.userservice.entity;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TripPlace Entity Unit Tests")
class TripPlaceTest {

    private TripPlace.TripPlaceBuilder tripPlaceBuilder;
    private TripPlan tripPlan;

    @BeforeAll
    static void beforeAllTests() {
        System.out.println("🔧 [BeforeAll] Starting TripPlace tests...");
    }

    @AfterAll
    static void afterAllTests() {
        System.out.println("✅ [AfterAll] TripPlace tests completed.");
    }

    @BeforeEach
    void setUp() {
        System.out.println("➡️ [BeforeEach] Setting up test data...");

        tripPlan = TripPlan.builder()
                .id(1L)
                .build();

        tripPlaceBuilder = TripPlace.builder()
                .id(10L)
                .displayName("Sample Place")
                .lat(50.0)
                .lon(20.0)
                .category("TestCategory")
                .sortOrder(3)
                .tripPlan(tripPlan);
    }

    @AfterEach
    void tearDown() {
        tripPlaceBuilder = null;
        tripPlan = null;
        System.out.println("⬅️ [AfterEach] Test finished. Cleaning up...");
    }

    @Test
    @DisplayName("Should build TripPlace with correct values")
    void shouldBuildTripPlaceCorrectly() {
        TripPlace tripPlace = tripPlaceBuilder.build();

        assertThat(tripPlace.getId()).isEqualTo(10L);
        assertThat(tripPlace.getDisplayName()).isEqualTo("Sample Place");
        assertThat(tripPlace.getLat()).isEqualTo(50.0);
        assertThat(tripPlace.getLon()).isEqualTo(20.0);
        assertThat(tripPlace.getCategory()).isEqualTo("TestCategory");
        assertThat(tripPlace.getSortOrder()).isEqualTo(3);
        assertThat(tripPlace.getTripPlan()).isEqualTo(tripPlan);
    }

    @Test
    @DisplayName("Should override some builder fields while keeping others")
    void shouldAllowOverridingBuilderFields() {
        TripPlace tripPlace = tripPlaceBuilder
                .displayName("Overridden Place")
                .lat(45.1234)
                .sortOrder(7)
                .build();

        assertThat(tripPlace.getDisplayName()).isEqualTo("Overridden Place");
        assertThat(tripPlace.getLat()).isEqualTo(45.1234);
        assertThat(tripPlace.getLon()).isEqualTo(20.0);
        assertThat(tripPlace.getCategory()).isEqualTo("TestCategory");
        assertThat(tripPlace.getSortOrder()).isEqualTo(7);
    }

    @Test
    @DisplayName("Should allow setting fields via setters")
    void shouldAllowManualSetters() {
        TripPlace tripPlace = new TripPlace();
        tripPlace.setId(999L);
        tripPlace.setDisplayName("Setter Place");
        tripPlace.setLat(10.123);
        tripPlace.setLon(15.456);
        tripPlace.setCategory("SetterCategory");
        tripPlace.setSortOrder(0);
        tripPlace.setTripPlan(tripPlan);

        assertThat(tripPlace.getId()).isEqualTo(999L);
        assertThat(tripPlace.getDisplayName()).isEqualTo("Setter Place");
        assertThat(tripPlace.getLat()).isEqualTo(10.123);
        assertThat(tripPlace.getLon()).isEqualTo(15.456);
        assertThat(tripPlace.getCategory()).isEqualTo("SetterCategory");
        assertThat(tripPlace.getSortOrder()).isEqualTo(0);
        assertThat(tripPlace.getTripPlan()).isEqualTo(tripPlan);
    }
}