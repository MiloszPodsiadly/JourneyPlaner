package com.milosz.podsiadly.userservice.service;

import com.milosz.podsiadly.userservice.dto.CreateUserProfileRequest;
import com.milosz.podsiadly.userservice.dto.UserProfileDto;
import com.milosz.podsiadly.userservice.entity.User;
import com.milosz.podsiadly.userservice.entity.UserProfile;
import com.milosz.podsiadly.userservice.repository.UserProfileRepository;
import com.milosz.podsiadly.userservice.repository.UserRepository;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserProfileCreateServiceImpl unit tests")
class UserProfileCreateServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private UserProfileRepository userProfileRepository;
    @Mock private EntityManager em;

    @InjectMocks
    private UserProfileCreateServiceImpl service;

    @BeforeAll
    static void beforeAll() {
        System.out.println("🔧 [BeforeAll] Starting UserProfileCreateServiceImpl tests...");
    }

    @AfterAll
    static void afterAll() {
        System.out.println("✅ [AfterAll] UserProfileCreateServiceImpl tests completed.");
    }

    @BeforeEach
    void beforeEach() {
        System.out.println("➡️ [BeforeEach] Preparing mocks...");
        Mockito.reset(userRepository, userProfileRepository, em);
        ReflectionTestUtils.setField(service, "em", em);
    }

    @AfterEach
    void afterEach() {
        System.out.println("⬅️ [AfterEach] Verifying & cleaning...");
        verifyNoMoreInteractions(userRepository, userProfileRepository, em);
        ReflectionTestUtils.setField(service, "em", null);
        Mockito.reset(userRepository, userProfileRepository, em);
    }

    @Test
    @DisplayName("createIfAbsentBySpotifyId: creates profile when user exists and profile absent")
    void createIfAbsentBySpotifyId_creates() {
        String spotifyId = "spotify:abc123";
        User user = new User();
        user.setId(42L);
        user.setName("Milo");
        user.setSpotifyId(spotifyId);

        when(userRepository.findBySpotifyId(spotifyId)).thenReturn(Optional.of(user));
        when(userProfileRepository.findById(42L)).thenReturn(Optional.empty());
        when(userRepository.getReferenceById(42L)).thenReturn(user);

        CreateUserProfileRequest req = new CreateUserProfileRequest(
                "Display Milo", "Bio Milo", "https://cdn.example.com/avatars/milo.png"
        );

        UserProfileDto dto = service.createIfAbsentBySpotifyId(spotifyId, req);

        ArgumentCaptor<UserProfile> captor = ArgumentCaptor.forClass(UserProfile.class);
        verify(userRepository).findBySpotifyId(spotifyId);
        verify(userProfileRepository).findById(42L);
        verify(userRepository).getReferenceById(42L);
        verify(em).persist(captor.capture());

        UserProfile saved = captor.getValue();
        assertNotNull(saved);
        assertEquals(42L, saved.getId());
        assertSame(user, saved.getUser());
        assertEquals("Display Milo", saved.getDisplayName());
        assertEquals("Bio Milo", saved.getBio());
        assertEquals("https://cdn.example.com/avatars/milo.png", saved.getAvatarUrl());

        assertNotNull(dto);
        assertEquals(42L, dto.id());
        assertEquals(42L, dto.userId());
        assertEquals("Display Milo", dto.displayName());
        assertEquals("Bio Milo", dto.bio());
        assertEquals("https://cdn.example.com/avatars/milo.png", dto.avatarUrl());
    }

    @Test
    @DisplayName("createIfAbsentBySpotifyId: throws when user not found by spotifyId")
    void createIfAbsentBySpotifyId_userNotFound() {
        String spotifyId = "missing";
        when(userRepository.findBySpotifyId(spotifyId)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.createIfAbsentBySpotifyId(spotifyId, null)
        );
        assertTrue(ex.getMessage().contains("User not found by spotifyId"));
        verify(userRepository).findBySpotifyId(spotifyId);
    }

    @Test
    @DisplayName("createIfAbsent: request provided → persists profile with request fields")
    void createIfAbsent_withRequest_persists() {
        User user = new User();
        user.setId(7L);
        user.setName("IgnoreMe");

        when(userProfileRepository.findById(7L)).thenReturn(Optional.empty());
        when(userRepository.getReferenceById(7L)).thenReturn(user);

        CreateUserProfileRequest req = new CreateUserProfileRequest(
                "GivenName", "GivenBio", "avatar.png"
        );

        UserProfileDto dto = service.createIfAbsent(user, req);

        ArgumentCaptor<UserProfile> captor = ArgumentCaptor.forClass(UserProfile.class);
        verify(userProfileRepository).findById(7L);
        verify(userRepository).getReferenceById(7L);
        verify(em).persist(captor.capture());

        UserProfile saved = captor.getValue();
        assertEquals(7L, saved.getId());
        assertEquals("GivenName", saved.getDisplayName());
        assertEquals("GivenBio", saved.getBio());
        assertEquals("avatar.png", saved.getAvatarUrl());
        assertSame(user, saved.getUser());

        assertEquals(7L, dto.id());
        assertEquals(7L, dto.userId());
        assertEquals("GivenName", dto.displayName());
        assertEquals("GivenBio", dto.bio());
        assertEquals("avatar.png", dto.avatarUrl());
    }

    @Test
    @DisplayName("createIfAbsent: request null & user.name present → uses user.name as displayName")
    void createIfAbsent_nullRequest_usesUserName() {
        User user = new User();
        user.setId(9L);
        user.setName("FromUserName");

        when(userProfileRepository.findById(9L)).thenReturn(Optional.empty());
        when(userRepository.getReferenceById(9L)).thenReturn(user);

        UserProfileDto dto = service.createIfAbsent(user, null);

        ArgumentCaptor<UserProfile> captor = ArgumentCaptor.forClass(UserProfile.class);
        verify(userProfileRepository).findById(9L);
        verify(userRepository).getReferenceById(9L);
        verify(em).persist(captor.capture());

        UserProfile saved = captor.getValue();
        assertEquals(9L, saved.getId());
        assertEquals("FromUserName", saved.getDisplayName());
        assertNull(saved.getBio());
        assertNull(saved.getAvatarUrl());

        assertEquals("FromUserName", dto.displayName());
        assertNull(dto.bio());
        assertNull(dto.avatarUrl());
    }

    @Test
    @DisplayName("createIfAbsent: request null & user.name null → uses default 'User'")
    void createIfAbsent_nullRequest_usesDefaultUser() {
        User user = new User();
        user.setId(11L);
        user.setName(null);

        when(userProfileRepository.findById(11L)).thenReturn(Optional.empty());
        when(userRepository.getReferenceById(11L)).thenReturn(user);

        UserProfileDto dto = service.createIfAbsent(user, null);

        ArgumentCaptor<UserProfile> captor = ArgumentCaptor.forClass(UserProfile.class);
        verify(userProfileRepository).findById(11L);
        verify(userRepository).getReferenceById(11L);
        verify(em).persist(captor.capture());

        UserProfile saved = captor.getValue();
        assertEquals(11L, saved.getId());
        assertEquals("User", saved.getDisplayName());
        assertNull(saved.getBio());
        assertNull(saved.getAvatarUrl());

        assertEquals("User", dto.displayName());
        assertNull(dto.bio());
        assertNull(dto.avatarUrl());
    }

    @Test
    @DisplayName("createIfAbsent: when profile exists → returns existing DTO and does not persist")
    void createIfAbsent_profileExists_returnsExisting() {
        User user = new User();
        user.setId(100L);

        UserProfile existing = UserProfile.builder()
                .id(100L)
                .user(user)
                .displayName("Existing")
                .bio("Old bio")
                .avatarUrl("old.png")
                .build();

        when(userProfileRepository.findById(100L)).thenReturn(Optional.of(existing));

        UserProfileDto dto = service.createIfAbsent(user, new CreateUserProfileRequest("New", "New bio", "new.png"));

        verify(userProfileRepository).findById(100L);
        verify(userRepository, never()).getReferenceById(anyLong());
        verify(em, never()).persist(any());

        assertEquals(100L, dto.id());
        assertEquals(100L, dto.userId());
        assertEquals("Existing", dto.displayName());
        assertEquals("Old bio", dto.bio());
        assertEquals("old.png", dto.avatarUrl());
    }
}