package com.milosz.podsiadly.userservice.mapper;

import com.milosz.podsiadly.userservice.dto.UserProfileDto;
import com.milosz.podsiadly.userservice.entity.User;
import com.milosz.podsiadly.userservice.entity.UserProfile;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UserProfileMapper.toDto unit tests")
class UserProfileMapperTest {

    @BeforeAll
    static void beforeAll() {
        System.out.println("🔧 [BeforeAll] Starting UserProfileMapper tests...");
    }

    @AfterAll
    static void afterAll() {
        System.out.println("✅ [AfterAll] UserProfileMapper tests completed.");
    }

    @BeforeEach
    void beforeEach() {
        System.out.println("➡️ [BeforeEach] Preparing test data...");
    }

    @AfterEach
    void afterEach() {
        System.out.println("⬅️ [AfterEach] Test finished.");
    }

    @Test
    @DisplayName("Returns null when entity is null")
    void returnsNullForNullEntity() {
        assertNull(UserProfileMapper.toDto(null));
    }

    @Test
    @DisplayName("Maps entity without user -> userId is null")
    void mapsEntityWithoutUser() {
        UserProfile entity = UserProfile.builder()
                .id(10L)
                .user(null)
                .displayName("Milo")
                .bio("Traveler & playlist hoarder")
                .avatarUrl("https://cdn.example.com/avatars/milo.png")
                .build();

        UserProfileDto dto = UserProfileMapper.toDto(entity);

        assertNotNull(dto);
        assertEquals(10L, dto.id());
        assertNull(dto.userId(), "userId should be null when entity.getUser() is null");
        assertEquals("Milo", dto.displayName());
        assertEquals("Traveler & playlist hoarder", dto.bio());
        assertEquals("https://cdn.example.com/avatars/milo.png", dto.avatarUrl());
    }

    @Test
    @DisplayName("Maps entity with user -> userId taken from user.getId()")
    void mapsEntityWithUser() {
        User user = new User();
        user.setId(42L);

        UserProfile entity = UserProfile.builder()
                .id(42L)
                .user(user)
                .displayName("Milo")
                .bio("Traveler & playlist hoarder")
                .avatarUrl("https://cdn.example.com/avatars/milo.png")
                .build();

        UserProfileDto dto = UserProfileMapper.toDto(entity);

        assertNotNull(dto);
        assertEquals(42L, dto.id());
        assertEquals(42L, dto.userId());
        assertEquals("Milo", dto.displayName());
        assertEquals("Traveler & playlist hoarder", dto.bio());
        assertEquals("https://cdn.example.com/avatars/milo.png", dto.avatarUrl());
    }

    @Test
    @DisplayName("Allows null fields in entity; passes them through")
    void allowsNullFields() {
        UserProfile entity = UserProfile.builder()
                .id(7L)
                .user(null)
                .displayName(null)
                .bio(null)
                .avatarUrl(null)
                .build();

        UserProfileDto dto = UserProfileMapper.toDto(entity);

        assertNotNull(dto);
        assertEquals(7L, dto.id());
        assertNull(dto.userId());
        assertNull(dto.displayName());
        assertNull(dto.bio());
        assertNull(dto.avatarUrl());
    }
}