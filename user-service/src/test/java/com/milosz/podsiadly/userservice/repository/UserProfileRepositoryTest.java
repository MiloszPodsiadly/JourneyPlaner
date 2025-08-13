package com.milosz.podsiadly.userservice.repository;

import com.milosz.podsiadly.userservice.entity.User;
import com.milosz.podsiadly.userservice.entity.UserProfile;

import org.junit.jupiter.api.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("UserProfileRepository JPA Tests")
class UserProfileRepositoryTest {

    @Autowired UserProfileRepository userProfileRepository;
    @Autowired UserRepository userRepository;

    @BeforeAll
    static void beforeAll() {
        System.out.println("🔧 [BeforeAll] Starting UserProfileRepository tests...");
    }

    @AfterAll
    static void afterAll() {
        System.out.println("✅ [AfterAll] UserProfileRepository tests completed.");
    }

    @BeforeEach
    void beforeEach() {
        System.out.println("➡️ [BeforeEach] Preparing test data...");
    }

    @AfterEach
    void afterEach() {
        System.out.println("⬅️ [AfterEach] Cleaning up...");
    }

    @Test
    @DisplayName("Save and find UserProfile by id using @MapsId")
    void saveAndFindById() {
        User user = new User();
        userRepository.save(user);
        Long uid = user.getId();

        UserProfile profile = UserProfile.builder()
                .user(user)
                .displayName("Milo")
                .bio("Traveler & playlist hoarder")
                .avatarUrl("https://cdn.example.com/avatars/milo.png")
                .build();

        userProfileRepository.save(profile);

        var found = userProfileRepository.findById(uid).orElseThrow();
        assertThat(found.getId()).isEqualTo(uid);
        assertThat(found.getUser()).isNotNull();
        assertThat(found.getUser().getId()).isEqualTo(uid);
        assertThat(found.getDisplayName()).isEqualTo("Milo");
        assertThat(found.getBio()).isEqualTo("Traveler & playlist hoarder");
        assertThat(found.getAvatarUrl()).isEqualTo("https://cdn.example.com/avatars/milo.png");
    }

    @Test
    @DisplayName("Delete UserProfile by id")
    void deleteById_removesProfile() {
        User user = new User();
        userRepository.save(user);
        Long uid = user.getId();

        UserProfile profile = UserProfile.builder()
                .user(user)
                .displayName("To Delete")
                .build();

        userProfileRepository.save(profile);

        userProfileRepository.deleteById(uid);

        assertThat(userProfileRepository.findById(uid)).isEmpty();
    }
}