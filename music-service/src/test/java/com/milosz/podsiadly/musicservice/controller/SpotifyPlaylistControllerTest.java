package com.milosz.podsiadly.musicservice.controller;

import com.milosz.podsiadly.musicservice.dto.SpotifyPlaylistDTO;
import com.milosz.podsiadly.musicservice.service.SpotifyPlaylistService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SpotifyPlaylistController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("SpotifyPlaylistController WebMvc tests")
class SpotifyPlaylistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SpotifyPlaylistService playlistService;

    @TestConfiguration
    static class MockConfig {
        @Bean
        SpotifyPlaylistService playlistService() {
            return mock(SpotifyPlaylistService.class);
        }
    }

    @BeforeAll
    static void beforeAll() {
        System.out.println("🔧 [BeforeAll] Starting SpotifyPlaylistController tests...");
    }

    @AfterAll
    static void afterAll() {
        System.out.println("✅ [AfterAll] SpotifyPlaylistController tests completed.");
    }

    @BeforeEach
    void setUp() {
        System.out.println("➡️ [BeforeEach] Setting up test data...");
        clearInvocations(playlistService);
        reset(playlistService);
    }

    @AfterEach
    void tearDown() {
        System.out.println("⬅️ [AfterEach] Cleaning up after test...");
        clearInvocations(playlistService);
        reset(playlistService);
    }

    @Test
    @DisplayName("🟢 GET /api/playlists with Bearer token returns playlists and calls service with raw token")
    void shouldReturnPlaylistsWhenAuthorized() throws Exception {
        var dto1 = new SpotifyPlaylistDTO(
                "pl1","Road Trip","desc",
                "https://open.spotify.com/playlist/pl1","Owner",
                25,true,false,"https://img/1.jpg","snap1"
        );
        var dto2 = new SpotifyPlaylistDTO(
                "pl2","Focus","",
                "https://open.spotify.com/playlist/pl2","Me",
                10,false,false,null,"snap2"
        );
        when(playlistService.getUserPlaylists("token-123"))
                .thenReturn(List.of(dto1, dto2));

        mockMvc.perform(get("/api/playlists")
                        .header("Authorization", "Bearer token-123")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("pl1"))
                .andExpect(jsonPath("$[0].name").value("Road Trip"))
                .andExpect(jsonPath("$[0].totalTracks").value(25))
                .andExpect(jsonPath("$[1].id").value("pl2"));

        verify(playlistService, times(1)).getUserPlaylists("token-123");
    }

    @Test
    @DisplayName("🟡 GET /api/playlists with non-Bearer auth header returns 401 and does not call service")
    void shouldReturnUnauthorizedWhenHeaderNotBearer() throws Exception {
        mockMvc.perform(get("/api/playlists")
                        .header("Authorization", "Basic abc123"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(playlistService);
    }

    @Test
    @DisplayName("🟡 GET /api/playlists without Authorization header returns 400 (missing header) and does not call service")
    void shouldReturnBadRequestWhenHeaderMissing() throws Exception {
        mockMvc.perform(get("/api/playlists"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(playlistService);
    }

    @Test
    @DisplayName("⚪ GET /api/playlists returns empty list when service returns empty")
    void shouldReturnEmptyList() throws Exception {
        when(playlistService.getUserPlaylists("tok")).thenReturn(List.of());

        var mvcResult = mockMvc.perform(get("/api/playlists")
                        .header("Authorization", "Bearer tok")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        var json = mvcResult.getResponse().getContentAsString();
        assertThat(json).isEqualTo("[]");

        verify(playlistService).getUserPlaylists("tok");
    }
}