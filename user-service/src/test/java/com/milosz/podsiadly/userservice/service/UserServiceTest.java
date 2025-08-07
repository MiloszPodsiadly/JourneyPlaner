package com.milosz.podsiadly.userservice.service;

import com.milosz.podsiadly.userservice.dto.CreateUserRequest;
import com.milosz.podsiadly.userservice.repository.UserRepository;

import org.junit.jupiter.api.*;

import static org.mockito.Mockito.*;

@DisplayName("UserService Unit Tests")
class UserServiceTest {

    private UserRepository userRepository;
    private UserService userService;

    private CreateUserRequest request;

    @BeforeAll
    static void beforeAll() {
        System.out.println("🔧 [BeforeAll] Starting UserService tests...");
    }

    @AfterAll
    static void afterAll() {
        System.out.println("✅ [AfterAll] UserService tests completed.");
    }

    @BeforeEach
    void setUp() {
        System.out.println("➡️ [BeforeEach] Setting up test data...");

        userRepository = mock(UserRepository.class);
        userService = new UserService(userRepository);

        request = new CreateUserRequest("spotify:user:123", "Test User", "user@example.com");
    }

    @AfterEach
    void tearDown() {
        userRepository = null;
        userService = null;
        request = null;

        System.out.println("⬅️ [AfterEach] Test finished. Cleaning up...");
    }

    @Test
    @DisplayName("Should save user if not exists")
    void shouldCreateUserIfNotExists() {
        when(userRepository.existsBySpotifyId(request.spotifyId())).thenReturn(false);

        userService.createUserIfNotExists(request);

        verify(userRepository).save(argThat(user ->
                user.getSpotifyId().equals("spotify:user:123") &&
                        user.getName().equals("Test User") &&
                        user.getEmail().equals("user@example.com")
        ));
    }

    @Test
    @DisplayName("Should not save user if already exists")
    void shouldNotCreateUserIfExists() {
        when(userRepository.existsBySpotifyId(request.spotifyId())).thenReturn(true);

        userService.createUserIfNotExists(request);

        verify(userRepository, never()).save(any());
    }
}

