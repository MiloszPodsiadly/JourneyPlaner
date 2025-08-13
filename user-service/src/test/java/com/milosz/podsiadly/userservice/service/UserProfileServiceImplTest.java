package com.milosz.podsiadly.userservice.service;

import com.milosz.podsiadly.userservice.dto.UserProfileDto;
import com.milosz.podsiadly.userservice.entity.User;
import com.milosz.podsiadly.userservice.entity.UserProfile;
import com.milosz.podsiadly.userservice.repository.UserProfileRepository;
import com.milosz.podsiadly.userservice.repository.UserRepository;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserProfileServiceImpl unit tests")
class UserProfileServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private UserProfileRepository userProfileRepository;

    @InjectMocks
    private UserProfileServiceImpl service;

    private final String spotifyId = "spotify:abc123";

    @BeforeAll
    static void beforeAll() {
        System.out.println("🔧 [BeforeAll] Starting UserProfileServiceImpl tests...");
    }

    @AfterAll
    static void afterAll() {
        System.out.println("✅ [AfterAll] UserProfileServiceImpl tests completed.");
    }

    @BeforeEach
    void beforeEach() {
        System.out.println("➡️ [BeforeEach] Preparing mocks...");
        Mockito.reset(userRepository, userProfileRepository);
    }

    @AfterEach
    void afterEach() {
        System.out.println("⬅️ [AfterEach] Verifying no unexpected interactions...");
        verifyNoMoreInteractions(userRepository, userProfileRepository);
    }

    @Test
    @DisplayName("getProfile: returns DTO when user and profile exist")
    void getProfile_returnsDto() {
        var user = new User();
        user.setId(1L);
        user.setSpotifyId(spotifyId);

        var profile = UserProfile.builder()
                .id(1L).user(user)
                .displayName("Milo")
                .bio("Traveler")
                .avatarUrl("avatar.png")
                .build();

        when(userRepository.findBySpotifyId(spotifyId)).thenReturn(Optional.of(user));
        when(userProfileRepository.findById(1L)).thenReturn(Optional.of(profile));

        UserProfileDto dto = service.getProfile(spotifyId);

        verify(userRepository).findBySpotifyId(spotifyId);
        verify(userProfileRepository).findById(1L);

        assertNotNull(dto);
        assertEquals(1L, dto.id());
        assertEquals(1L, dto.userId());
        assertEquals("Milo", dto.displayName());
        assertEquals("Traveler", dto.bio());
        assertEquals("avatar.png", dto.avatarUrl());
    }

    @Test
    @DisplayName("getProfile: throws when user not found by spotifyId")
    void getProfile_userNotFound() {
        when(userRepository.findBySpotifyId(spotifyId)).thenReturn(Optional.empty());

        var ex = assertThrows(IllegalArgumentException.class,
                () -> service.getProfile(spotifyId));

        assertTrue(ex.getMessage().contains("User not found by spotifyId"));
        verify(userRepository).findBySpotifyId(spotifyId);
    }

    @Test
    @DisplayName("getProfile: throws when profile missing for user id")
    void getProfile_profileNotFound() {
        var user = new User(); user.setId(2L);
        when(userRepository.findBySpotifyId(spotifyId)).thenReturn(Optional.of(user));
        when(userProfileRepository.findById(2L)).thenReturn(Optional.empty());

        var ex = assertThrows(IllegalStateException.class,
                () -> service.getProfile(spotifyId));

        assertTrue(ex.getMessage().contains("Profile not found for user id: 2"));
        verify(userRepository).findBySpotifyId(spotifyId);
        verify(userProfileRepository).findById(2L);
    }

    @Test
    @DisplayName("updateProfile: applies only non-null fields and saves")
    void updateProfile_updatesNonNullFields() {
        var user = new User(); user.setId(3L);

        var existing = UserProfile.builder()
                .id(3L).user(user)
                .displayName("Old")
                .bio("Old bio")
                .avatarUrl("old.png")
                .build();

        when(userRepository.findBySpotifyId(spotifyId)).thenReturn(Optional.of(user));
        when(userProfileRepository.findById(3L)).thenReturn(Optional.of(existing));
        when(userProfileRepository.save(existing)).thenAnswer(inv -> inv.getArgument(0));

        var update = new UserProfileDto(3L, 3L, "New Name", null, "new.png");

        UserProfileDto dto = service.updateProfile(spotifyId, update);

        verify(userRepository).findBySpotifyId(spotifyId);
        verify(userProfileRepository).findById(3L);
        verify(userProfileRepository).save(existing);

        assertNotNull(dto);
        assertEquals(3L, dto.id());
        assertEquals(3L, dto.userId());
        assertEquals("New Name", existing.getDisplayName());
        assertEquals("Old bio", existing.getBio());
        assertEquals("new.png", existing.getAvatarUrl());
        assertEquals("New Name", dto.displayName());
        assertEquals("Old bio", dto.bio());
        assertEquals("new.png", dto.avatarUrl());
    }

    @Test
    @DisplayName("updateProfile: null dto → saves existing unchanged and returns it")
    void updateProfile_nullDto_keepsExisting() {
        var user = new User(); user.setId(4L);

        var existing = UserProfile.builder()
                .id(4L).user(user)
                .displayName("Keep")
                .bio("Keep bio")
                .avatarUrl("keep.png")
                .build();

        when(userRepository.findBySpotifyId(spotifyId)).thenReturn(Optional.of(user));
        when(userProfileRepository.findById(4L)).thenReturn(Optional.of(existing));
        when(userProfileRepository.save(existing)).thenAnswer(inv -> inv.getArgument(0));

        UserProfileDto dto = service.updateProfile(spotifyId, null);

        verify(userRepository).findBySpotifyId(spotifyId);
        verify(userProfileRepository).findById(4L);
        verify(userProfileRepository).save(existing);

        assertEquals("Keep", dto.displayName());
        assertEquals("Keep bio", dto.bio());
        assertEquals("keep.png", dto.avatarUrl());
    }

    @Test
    @DisplayName("updateProfile: throws when user not found")
    void updateProfile_userNotFound() {
        when(userRepository.findBySpotifyId(spotifyId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.updateProfile(spotifyId, new UserProfileDto(1L,1L,"a","b","c")));

        verify(userRepository).findBySpotifyId(spotifyId);
    }

    @Test
    @DisplayName("updateProfile: throws when profile missing")
    void updateProfile_profileNotFound() {
        var user = new User(); user.setId(5L);
        when(userRepository.findBySpotifyId(spotifyId)).thenReturn(Optional.of(user));
        when(userProfileRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class,
                () -> service.updateProfile(spotifyId, new UserProfileDto(5L,5L,"a","b","c")));

        verify(userRepository).findBySpotifyId(spotifyId);
        verify(userProfileRepository).findById(5L);
    }
}