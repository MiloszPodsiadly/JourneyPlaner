package com.milosz.podsiadly.musicservice.service;

import com.milosz.podsiadly.musicservice.dto.SpotifyPlaylistDTO;
import org.junit.jupiter.api.*;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@DisplayName("SpotifyPlaylistService – tests with MockRestServiceServer")
class SpotifyPlaylistServiceTest {

    private SpotifyPlaylistService service;
    private RestTemplate restTemplate;
    private MockRestServiceServer server;

    private static final String API = "https://api.spotify.com/v1/me/playlists";
    private static final String ACCESS_TOKEN = "test-access-token";

    @BeforeAll
    static void beforeAll() {
        System.out.println("🔧 [BeforeAll] Starting SpotifyPlaylistService tests...");
    }

    @AfterAll
    static void afterAll() {
        System.out.println("✅ [AfterAll] SpotifyPlaylistService tests completed.");
    }

    @BeforeEach
    void setUp() {
        System.out.println("➡️ [BeforeEach] Setting up test context...");

        restTemplate = new RestTemplate();
        server = MockRestServiceServer.createServer(restTemplate);

        RestTemplateBuilder builder = new RestTemplateBuilder() {
            @Override
            public RestTemplate build() {
                return restTemplate;
            }
        };
        service = new SpotifyPlaylistService(builder);
    }

    @AfterEach
    void tearDown() {
        System.out.println("⬅️ [AfterEach] Verifying server expectations & cleaning up...");
        server.verify();
        service = null;
        restTemplate = null;
        server = null;
    }

    @Test
    @DisplayName("🟢 getUserPlaylists – happy path: parses multiple playlists")
    void getUserPlaylists_returnsParsedList() {
        String json = "{"
                + "\"items\":["
                + "  {"
                + "    \"id\":\"pl1\","
                + "    \"name\":\"Road Trip\","
                + "    \"description\":\"Best for driving\","
                + "    \"external_urls\":{\"spotify\":\"https://open.spotify.com/playlist/pl1\"},"
                + "    \"owner\":{\"display_name\":\"Alice\"},"
                + "    \"tracks\":{\"total\":42},"
                + "    \"public\":true,"
                + "    \"collaborative\":false,"
                + "    \"images\":[{\"url\":\"https://img/1.jpg\"}],"
                + "    \"snapshot_id\":\"snap-1\""
                + "  },"
                + "  {"
                + "    \"id\":\"pl2\","
                + "    \"name\":\"Chill\","
                + "    \"description\":\"\","
                + "    \"external_urls\":{\"spotify\":\"https://open.spotify.com/playlist/pl2\"},"
                + "    \"owner\":{\"display_name\":\"Bob\"},"
                + "    \"tracks\":{\"total\":10},"
                + "    \"public\":false,"
                + "    \"collaborative\":true,"
                + "    \"images\":[],"
                + "    \"snapshot_id\":\"snap-2\""
                + "  }"
                + "]"
                + "}";

        server.expect(ExpectedCount.once(), requestTo(API))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer " + ACCESS_TOKEN))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        List<SpotifyPlaylistDTO> result = service.getUserPlaylists(ACCESS_TOKEN);

        assertThat(result).hasSize(2);

        SpotifyPlaylistDTO p1 = result.get(0);
        assertThat(p1.id()).isEqualTo("pl1");
        assertThat(p1.name()).isEqualTo("Road Trip");
        assertThat(p1.description()).isEqualTo("Best for driving");
        assertThat(p1.url()).isEqualTo("https://open.spotify.com/playlist/pl1");
        assertThat(p1.ownerName()).isEqualTo("Alice");
        assertThat(p1.totalTracks()).isEqualTo(42);
        assertThat(p1.isPublic()).isTrue();
        assertThat(p1.isCollaborative()).isFalse();
        assertThat(p1.imageUrl()).isEqualTo("https://img/1.jpg");
        assertThat(p1.snapshotId()).isEqualTo("snap-1");

        SpotifyPlaylistDTO p2 = result.get(1);
        assertThat(p2.id()).isEqualTo("pl2");
        assertThat(p2.name()).isEqualTo("Chill");
        assertThat(p2.description()).isEqualTo("");
        assertThat(p2.url()).isEqualTo("https://open.spotify.com/playlist/pl2");
        assertThat(p2.ownerName()).isEqualTo("Bob");
        assertThat(p2.totalTracks()).isEqualTo(10);
        assertThat(p2.isPublic()).isFalse();
        assertThat(p2.isCollaborative()).isTrue();
        assertThat(p2.imageUrl()).isNull();
        assertThat(p2.snapshotId()).isEqualTo("snap-2");
    }

    @Test
    @DisplayName("🟡 getUserPlaylists – returns empty list when items is empty")
    void getUserPlaylists_returnsEmptyList_whenItemsEmpty() {
        String json = "{\"items\":[]}";

        server.expect(requestTo(API))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer " + ACCESS_TOKEN))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        List<SpotifyPlaylistDTO> result = service.getUserPlaylists(ACCESS_TOKEN);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("🔴 getUserPlaylists – 401 Unauthorized bubbles up as HttpClientErrorException")
    void getUserPlaylists_unauthorized_throws() {
        String err = "{\"error\":\"unauthorized\"}";

        server.expect(requestTo(API))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(err));

        assertThatThrownBy(() -> service.getUserPlaylists("bad-token"))
                .isInstanceOf(HttpClientErrorException.Unauthorized.class);
    }

    @Test
    @DisplayName("🧱 getUserPlaylists – defensive: missing images/owner fields are handled")
    void getUserPlaylists_handlesMissingOptionalFields() {
        String json = "{"
                + "\"items\":["
                + "  {"
                + "    \"id\":\"plX\","
                + "    \"name\":\"No Extras\","
                + "    \"external_urls\":{},"
                + "    \"tracks\":{},"
                + "    \"public\":false,"
                + "    \"collaborative\":false"
                + "  }"
                + "]"
                + "}";

        server.expect(requestTo(API))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        List<SpotifyPlaylistDTO> result = service.getUserPlaylists(ACCESS_TOKEN);

        assertThat(result).hasSize(1);
        SpotifyPlaylistDTO p = result.get(0);

        assertThat(p.id()).isEqualTo("plX");
        assertThat(p.name()).isEqualTo("No Extras");
        assertThat(p.description()).isEqualTo("");
        assertThat(p.url()).isEqualTo("");
        assertThat(p.ownerName()).isEqualTo("");
        assertThat(p.totalTracks()).isEqualTo(0);
        assertThat(p.isPublic()).isFalse();
        assertThat(p.isCollaborative()).isFalse();
        assertThat(p.imageUrl()).isNull();
        assertThat(p.snapshotId()).isEqualTo("");
    }
}
