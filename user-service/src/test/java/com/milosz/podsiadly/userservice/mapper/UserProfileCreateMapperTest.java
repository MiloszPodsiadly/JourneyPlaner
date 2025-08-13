package com.milosz.podsiadly.userservice.mapper;

import com.milosz.podsiadly.userservice.dto.CreateUserProfileRequest;
import com.milosz.podsiadly.userservice.entity.User;
import com.milosz.podsiadly.userservice.entity.UserProfile;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UserProfileCreateMapper unit tests")
class UserProfileCreateMapperTest {

    @BeforeAll
    static void beforeAll() {
        System.out.println("🔧 [BeforeAll] Starting UserProfileCreateMapper tests...");
    }

    @AfterAll
    static void afterAll() {
        System.out.println("✅ [AfterAll] UserProfileCreateMapper tests completed.");
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
    @DisplayName("Maps all fields from CreateUserProfileRequest to UserProfile with shared id via @MapsId")
    void mapsAllFields() {
        var req = new CreateUserProfileRequest(
                "Milo",
                "Traveler & playlist hoarder",
                "https://cdn.example.com/avatars/milo.png"
        );
        var user = new User();
        user.setId(42L);

        UserProfile profile = UserProfileCreateMapper.fromRequest(req, user);

        assertNotNull(profile);
        assertEquals(42L, profile.getId());
        assertSame(user, profile.getUser());
        assertEquals("Milo", profile.getDisplayName());
        assertEquals("Traveler & playlist hoarder", profile.getBio());
        assertEquals("https://cdn.example.com/avatars/milo.png", profile.getAvatarUrl());
    }

    @Test
    @DisplayName("Throws IllegalArgumentException when user is null")
    void throwsWhenUserNull() {
        var req = new CreateUserProfileRequest("A", "B", "C");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> UserProfileCreateMapper.fromRequest(req, null)
        );
        assertTrue(ex.getMessage().contains("User is required"));
    }

    @Test
    @DisplayName("Allows null fields in request; passes them through to entity")
    void allowsNullRequestFields() {
        var req = new CreateUserProfileRequest(null, null, null);
        var user = new User();
        user.setId(7L);

        UserProfile profile = UserProfileCreateMapper.fromRequest(req, user);

        assertNotNull(profile);
        assertEquals(7L, profile.getId());
        assertSame(user, profile.getUser());
        assertNull(profile.getDisplayName());
        assertNull(profile.getBio());
        assertNull(profile.getAvatarUrl());
    }

    @Test
    @DisplayName("Throws NullPointerException when request is null (due to req.displayName() call)")
    void throwsWhenRequestNull() {
        var user = new User();
        user.setId(1L);

        assertThrows(NullPointerException.class,
                () -> UserProfileCreateMapper.fromRequest(null, user));
    }
}