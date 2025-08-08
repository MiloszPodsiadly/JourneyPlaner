package com.milosz.podsiadly.userservice.mapper;

import com.milosz.podsiadly.userservice.dto.TripPlaceDto;
import com.milosz.podsiadly.userservice.entity.TripPlace;
import com.milosz.podsiadly.userservice.entity.TripPlan;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TripPlaceMapper Unit Tests")
class TripPlaceMapperTest {

    private TripPlan tripPlan;
    private TripPlace tripPlace;
    private TripPlaceDto tripPlaceDto;

    @BeforeAll
    static void beforeAllTests() {
        System.out.println("🔧 [BeforeAll] Starting TripPlaceMapper tests...");
    }

    @AfterAll
    static void afterAllTests() {
        System.out.println("✅ [AfterAll] TripPlaceMapper tests completed.");
    }

    @BeforeEach
    void setUp() {
        System.out.println("➡️ [BeforeEach] Setting up test data...");

        tripPlan = TripPlan.builder()
                .id(1L)
                .build();

        tripPlace = TripPlace.builder()
                .id(10L)
                .displayName("Test Place")
                .lat(12.3456)
                .lon(65.4321)
                .category("Monument")
                .sortOrder(5)
                .tripPlan(tripPlan)
                .build();

        tripPlaceDto = new TripPlaceDto(
                10L,
                "Test Place",
                12.3456,
                65.4321,
                "Monument",
                1L,
                5
        );
    }

    @AfterEach
    void tearDown() {
        tripPlan = null;
        tripPlace = null;
        tripPlaceDto = null;
        System.out.println("⬅️ [AfterEach] Test finished. Cleaning up...");
    }

    @Test
    @DisplayName("Should map TripPlace to TripPlaceDto correctly (including sortOrder)")
    void shouldMapToDtoCorrectly() {
        TripPlaceDto dto = TripPlaceMapper.toDto(tripPlace);

        assertThat(dto.id()).isEqualTo(10L);
        assertThat(dto.displayName()).isEqualTo("Test Place");
        assertThat(dto.lat()).isEqualTo(12.3456);
        assertThat(dto.lon()).isEqualTo(65.4321);
        assertThat(dto.category()).isEqualTo("Monument");
        assertThat(dto.tripPlanId()).isEqualTo(1L);
        assertThat(dto.sortOrder()).isEqualTo(5);
    }

    @Test
    @DisplayName("Should map TripPlaceDto to TripPlace correctly (including sortOrder)")
    void shouldMapToEntityCorrectly() {
        TripPlace entity = TripPlaceMapper.toEntity(tripPlaceDto);

        assertThat(entity.getId()).isEqualTo(10L);
        assertThat(entity.getDisplayName()).isEqualTo("Test Place");
        assertThat(entity.getLat()).isEqualTo(12.3456);
        assertThat(entity.getLon()).isEqualTo(65.4321);
        assertThat(entity.getCategory()).isEqualTo("Monument");
        assertThat(entity.getSortOrder()).isEqualTo(5);
        assertThat(entity.getTripPlan()).isNotNull();
        assertThat(entity.getTripPlan().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should handle null TripPlan in toDto")
    void shouldHandleNullTripPlanInToDto() {
        tripPlace.setTripPlan(null);
        TripPlaceDto dto = TripPlaceMapper.toDto(tripPlace);
        assertThat(dto.tripPlanId()).isNull();
    }

    @Test
    @DisplayName("Should handle null tripPlanId and null sortOrder in toEntity (default sortOrder = 0)")
    void shouldHandleNullTripPlanIdAndNullSortOrderInToEntity() {
        TripPlaceDto dtoWithNulls = new TripPlaceDto(
                11L, "Nowhere", 0.0, 0.0, "Unknown", null, null
        );

        TripPlace entity = TripPlaceMapper.toEntity(dtoWithNulls);

        assertThat(entity.getTripPlan()).isNull();
        assertThat(entity.getSortOrder()).isEqualTo(0);
    }
}
