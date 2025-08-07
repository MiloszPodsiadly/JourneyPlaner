package com.milosz.podsiadly.userservice.repository;

import com.milosz.podsiadly.userservice.entity.User;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("UserRepository Integration Tests")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeAll
    static void beforeAll() {
        System.out.println("🔧 [BeforeAll] Starting UserRepository tests...");
    }

    @AfterAll
    static void afterAll() {
        System.out.println("✅ [AfterAll] UserRepository tests completed.");
    }

    @BeforeEach
    void setUp() {
        System.out.println("➡️ [BeforeEach] Setting up test data...");

        user = User.builder()
                .spotifyId("spotify:user:abc123")
                .name("Test User")
                .email("test@example.com")
                .build();
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
        System.out.println("⬅️ [AfterEach] Test finished. Cleaning up...");
    }

    @Test
    @DisplayName("Should save and retrieve User by ID")
    void shouldSaveAndFindById() {
        User saved = userRepository.save(user);

        Optional<User> found = userRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getSpotifyId()).isEqualTo("spotify:user:abc123");
        assertThat(found.get().getName()).isEqualTo("Test User");
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("Should find user by Spotify ID")
    void shouldFindBySpotifyId() {
        userRepository.save(user);

        Optional<User> found = userRepository.findBySpotifyId("spotify:user:abc123");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test User");
    }

    @Test
    @DisplayName("Should return empty when Spotify ID not found")
    void shouldReturnEmptyIfSpotifyIdNotFound() {
        Optional<User> found = userRepository.findBySpotifyId("nonexistent");

        assertThat(found).isNotPresent();
    }

    @Test
    @DisplayName("Should return true if user exists by Spotify ID")
    void shouldReturnTrueIfExistsBySpotifyId() {
        userRepository.save(user);

        boolean exists = userRepository.existsBySpotifyId("spotify:user:abc123");

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should return false if user does not exist by Spotify ID")
    void shouldReturnFalseIfNotExistsBySpotifyId() {
        boolean exists = userRepository.existsBySpotifyId("not:existing");

        assertThat(exists).isFalse();
    }
}