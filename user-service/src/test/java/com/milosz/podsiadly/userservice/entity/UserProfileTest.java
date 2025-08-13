package com.milosz.podsiadly.userservice.entity;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import org.junit.jupiter.api.*;

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("UserProfile @MapsId JPA mapping")
class UserProfileTest {

    @PersistenceContext
    EntityManager em;

    @BeforeAll
    static void beforeAll() {
        System.out.println("🔧 [BeforeAll] Starting UserProfile JPA mapping tests...");
    }

    @AfterAll
    static void afterAll() {
        System.out.println("✅ [AfterAll] UserProfile JPA mapping tests completed.");
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
    @Transactional
    @Rollback
    @DisplayName("Persists User then UserProfile with @MapsId; fetch by id returns linked entities")
    void persistsAndFetchesProfileWithMapsId() {
        User user = new User();
        em.persist(user);
        em.flush();
        Long userId = user.getId();
        assertThat(userId).as("user.id should be generated").isNotNull();

        UserProfile profile = UserProfile.builder()
                .id(userId)
                .user(user)
                .displayName("Milo Podsiadly")
                .bio("Traveler & playlist hoarder")
                .avatarUrl("https://cdn.example.com/avatars/milo.png")
                .build();

        em.persist(profile);
        em.flush();
        em.clear();

        UserProfile found = em.find(UserProfile.class, userId);
        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(userId);
        assertThat(found.getDisplayName()).isEqualTo("Milo Podsiadly");
        assertThat(found.getBio()).isEqualTo("Traveler & playlist hoarder");
        assertThat(found.getAvatarUrl()).isEqualTo("https://cdn.example.com/avatars/milo.png");
        assertThat(found.getUser()).as("UserProfile.user should be linked").isNotNull();
        assertThat(found.getUser().getId()).isEqualTo(userId);
    }

    @Test
    @Transactional
    @Rollback
    @DisplayName("@MapsId enforces shared primary key (profile id follows user id)")
    void mapsIdSharesPrimaryKey() {
        User u = new User();
        em.persist(u);
        em.flush();
        Long uid = u.getId();

        UserProfile p = new UserProfile();
        p.setUser(u);
        p.setId(uid);
        p.setDisplayName("Shared PK");
        em.persist(p);
        em.flush();
        em.clear();

        UserProfile reloaded = em.find(UserProfile.class, uid);
        assertThat(reloaded).isNotNull();
        assertThat(reloaded.getUser()).isNotNull();
        assertThat(reloaded.getId()).isEqualTo(reloaded.getUser().getId());
    }
}