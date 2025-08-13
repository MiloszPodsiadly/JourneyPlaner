package com.milosz.podsiadly.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.milosz.podsiadly.userservice.dto.CreateUserProfileRequest;
import com.milosz.podsiadly.userservice.service.UserProfileCreateService;
import com.milosz.podsiadly.userservice.service.UserProfileService;

import org.junit.jupiter.api.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserProfileController.class)
@DisplayName("UserProfileController -tests")
class UserProfileControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean private UserProfileCreateService userProfileCreateService;
    @MockitoBean private UserProfileService userProfileService;

    private final String spotifyId = "spotify:abc123";

    @BeforeAll
    static void beforeAll() {
        System.out.println("🔧 [BeforeAll] Starting UserProfileController tests...");
    }

    @AfterAll
    static void afterAll() {
        System.out.println("✅ [AfterAll] UserProfileController tests completed.");
    }

    @BeforeEach
    void beforeEach() {
        System.out.println("➡️ [BeforeEach] Preparing mocks and data...");
        reset(userProfileCreateService, userProfileService);
    }

    @AfterEach
    void afterEach() {
        System.out.println("⬅️ [AfterEach] Cleaning up after test...");
        clearInvocations(userProfileCreateService, userProfileService);
    }

    @Test
    @DisplayName("POST /api/user-profiles/{spotifyId}/ensure with body -> 204 and passes request")
    void ensureProfile_withBody_returns204() throws Exception {
        var req = new CreateUserProfileRequest(
                "Milo",
                "Traveler & playlist hoarder",
                "https://cdn.example.com/avatars/milo.png"
        );

        mockMvc.perform(post("/api/user-profiles/{spotifyId}/ensure", spotifyId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNoContent());

        verify(userProfileCreateService).createIfAbsentBySpotifyId(eq(spotifyId), eq(req));
    }

    @Test
    @DisplayName("POST /api/user-profiles/{spotifyId}/ensure without body -> 204 and passes null")
    void ensureProfile_withoutBody_returns204_andPassesNull() throws Exception {
        mockMvc.perform(post("/api/user-profiles/{spotifyId}/ensure", spotifyId))
                .andExpect(status().isNoContent());

        verify(userProfileCreateService).createIfAbsentBySpotifyId(eq(spotifyId), isNull());
    }

    @Test
    @DisplayName("GET /api/user-profiles/{spotifyId} -> 200 (minimal interaction check)")
    void getProfile_minimal() throws Exception {
        when(userProfileService.getProfile(spotifyId)).thenReturn(null);

        mockMvc.perform(get("/api/user-profiles/{spotifyId}", spotifyId))
                .andExpect(status().isOk());

        verify(userProfileService).getProfile(spotifyId);
    }
}