package com.milosz.podsiadly.musicservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.milosz.podsiadly.musicservice.dto.SpotifyTrackDTO;
import com.milosz.podsiadly.musicservice.service.SpotifyTrackService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SpotifyTrackController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("SpotifyTrackController WebMvc Tests")
class SpotifyTrackControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SpotifyTrackService trackService;

    @TestConfiguration
    static class MockConfig {
        @Bean
        SpotifyTrackService trackService() {
            return mock(SpotifyTrackService.class);
        }
    }

    @BeforeAll
    static void beforeAll() {
        System.out.println("🔧 [BeforeAll] Starting SpotifyTrackController tests...");
    }

    @AfterAll
    static void afterAll() {
        System.out.println("✅ [AfterAll] SpotifyTrackController tests completed.");
    }

    @BeforeEach
    void setUp() {
        System.out.println("➡️ [BeforeEach] Setting up test data...");
        clearInvocations(trackService);
        reset(trackService);
    }

    @AfterEach
    void tearDown() {
        System.out.println("⬅️ [AfterEach] Cleaning up after test...");
    }

    @Test
    @DisplayName("🟢 200 with tracks when Authorization is Bearer")
    void shouldReturnTracksWhenAuthorized() throws Exception {
        String playlistId = "pl-123";
        String token = "token-123";

        var t1 = new SpotifyTrackDTO("t1", "Song A", List.of("A1", "A2"), "Album X", null, "https://s/1");
        var t2 = new SpotifyTrackDTO("t2", "Song B", List.of("B1"), "Album Y", "https://p/2", "https://s/2");

        when(trackService.getPlaylistTracks(eq(playlistId), eq(token)))
                .thenReturn(List.of(t1, t2));

        mockMvc.perform(get("/api/spotify/playlists/{pid}/tracks", playlistId)
                        .header("Authorization", "Bearer " + token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("t1"))
                .andExpect(jsonPath("$[0].name").value("Song A"))
                .andExpect(jsonPath("$[0].artists[0]").value("A1"))
                .andExpect(jsonPath("$[1].previewUrl").value("https://p/2"))
                .andExpect(jsonPath("$[1].externalUrl").value("https://s/2"));

        verify(trackService).getPlaylistTracks(eq(playlistId), eq(token));
    }

    @Test
    @DisplayName("🟢 200 and empty array when service returns no tracks")
    void shouldReturnEmptyArrayWhenNoTracks() throws Exception {
        String playlistId = "empty";
        String token = "tok";

        when(trackService.getPlaylistTracks(eq(playlistId), eq(token)))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/spotify/playlists/{pid}/tracks", playlistId)
                        .header("Authorization", "Bearer " + token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(trackService).getPlaylistTracks(eq(playlistId), eq(token));
    }

    @Test
    @DisplayName("🟡 401 when Authorization header has wrong scheme")
    void shouldReturnUnauthorizedWhenHeaderInvalid() throws Exception {
        mockMvc.perform(get("/api/spotify/playlists/{pid}/tracks", "pl-xyz")
                        .header("Authorization", "Tok abc")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(trackService);
    }

    @Test
    @DisplayName("🟡 400 when Authorization header is missing (required header)")
    void shouldReturnBadRequestWhenHeaderMissing() throws Exception {
        mockMvc.perform(get("/api/spotify/playlists/{pid}/tracks", "pl-xyz")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(trackService);
    }

    @Test
    @DisplayName("🧪 Service called with exact playlistId and token")
    void shouldPassCorrectParamsToService() throws Exception {
        String playlistId = "abc123";
        String token = "zzz";

        when(trackService.getPlaylistTracks(eq(playlistId), eq(token)))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/spotify/playlists/{pid}/tracks", playlistId)
                        .header("Authorization", "Bearer " + token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(trackService, times(1)).getPlaylistTracks(eq(playlistId), eq(token));
        verifyNoMoreInteractions(trackService);
    }
}