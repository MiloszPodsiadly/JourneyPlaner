package com.milosz.podsiadly.musicservice.service;

import com.milosz.podsiadly.musicservice.dto.SpotifyTrackDTO;
import org.junit.jupiter.api.*;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@DisplayName("SpotifyTrackService – tests with MockRestServiceServer")
class SpotifyTrackServiceTest {

    private SpotifyTrackService service;
    private RestTemplate restTemplate;
    private MockRestServiceServer server;

    private static final String TOKEN = "test-token";
    private static final String PLAYLIST_ID = "pl123";
    private static final String URL = "https://api.spotify.com/v1/playlists/" + PLAYLIST_ID + "/tracks";

    @BeforeAll
    static void beforeAll() {
        System.out.println("🔧 [BeforeAll] Starting SpotifyTrackService tests...");
    }

    @AfterAll
    static void afterAll() {
        System.out.println("✅ [AfterAll] SpotifyTrackService tests completed.");
    }

    @BeforeEach
    void setUp() {
        System.out.println("➡️ [BeforeEach] Bootstrapping test context...");
        restTemplate = new RestTemplate();
        server = MockRestServiceServer.createServer(restTemplate);
        RestTemplateBuilder builder = new RestTemplateBuilder() {
            @Override
            public RestTemplate build() {
                return restTemplate;
            }
        };
        service = new SpotifyTrackService(builder);
    }

    @AfterEach
    void tearDown() {
        System.out.println("⬅️ [AfterEach] Verifying server & cleaning up...");
        server.verify();
        service = null;
        restTemplate = null;
        server = null;
    }

    @Test
    @DisplayName("🟢 getPlaylistTracks – parses multiple tracks with artists and URLs")
    void getPlaylistTracks_parsesTracks() {
        String json =
                "{ \"items\": [" +
                        "  { \"track\": {" +
                        "      \"id\":\"t1\"," +
                        "      \"name\":\"Track One\"," +
                        "      \"album\": {\"name\":\"Album A\"}," +
                        "      \"preview_url\":\"https://p/1.mp3\"," +
                        "      \"external_urls\": {\"spotify\":\"https://open.spotify.com/track/t1\"}," +
                        "      \"artists\":[{\"name\":\"A1\"},{\"name\":\"A2\"}]" +
                        "  }}," +
                        "  { \"track\": {" +
                        "      \"id\":\"t2\"," +
                        "      \"name\":\"Track Two\"," +
                        "      \"album\": {\"name\":\"Album B\"}," +
                        "      \"preview_url\": null," +
                        "      \"external_urls\": {\"spotify\":\"https://open.spotify.com/track/t2\"}," +
                        "      \"artists\":[{\"name\":\"B1\"}]" +
                        "  }}" +
                        "]}";

        server.expect(ExpectedCount.once(), requestTo(URL))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer " + TOKEN))
                .andExpect(header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        List<SpotifyTrackDTO> tracks = service.getPlaylistTracks(PLAYLIST_ID, TOKEN);

        assertThat(tracks).hasSize(2);

        SpotifyTrackDTO t1 = tracks.get(0);
        assertThat(t1.id()).isEqualTo("t1");
        assertThat(t1.name()).isEqualTo("Track One");
        assertThat(t1.albumName()).isEqualTo("Album A");
        assertThat(t1.previewUrl()).isEqualTo("https://p/1.mp3");
        assertThat(t1.externalUrl()).isEqualTo("https://open.spotify.com/track/t1");
        assertThat(t1.artists()).containsExactly("A1", "A2");

        SpotifyTrackDTO t2 = tracks.get(1);
        assertThat(t2.id()).isEqualTo("t2");
        assertThat(t2.name()).isEqualTo("Track Two");
        assertThat(t2.albumName()).isEqualTo("Album B");
        assertThat(t2.previewUrl()).isNull(); // null w JSON → null
        assertThat(t2.externalUrl()).isEqualTo("https://open.spotify.com/track/t2");
        assertThat(t2.artists()).containsExactly("B1");
    }

    @Test
    @DisplayName("🟡 getPlaylistTracks – returns empty list when items is empty")
    void getPlaylistTracks_emptyItems() {
        String json = "{ \"items\": [] }";

        server.expect(requestTo(URL))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        List<SpotifyTrackDTO> tracks = service.getPlaylistTracks(PLAYLIST_ID, TOKEN);

        assertThat(tracks).isEmpty();
    }

    @Test
    @DisplayName("🔴 getPlaylistTracks – 401 Unauthorized bubbles as HttpClientErrorException")
    void getPlaylistTracks_unauthorized() {
        String err = "{\"error\":\"unauthorized\"}";

        server.expect(requestTo(URL))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(err));

        assertThatThrownBy(() -> service.getPlaylistTracks(PLAYLIST_ID, "bad-token"))
                .isInstanceOf(HttpClientErrorException.Unauthorized.class);
    }

    @Test
    @DisplayName("🧱 getPlaylistTracks – skips items without track and handles missing optional fields")
    void getPlaylistTracks_defensiveParsing() {
        String json =
                "{ \"items\": [" +
                        "  { }, " +
                        "  { \"track\": null }, " +
                        "  { \"track\": {" +
                        "      \"id\":\"tX\"," +
                        "      \"name\":\"No Extras\"," +
                        "      \"album\": {}, " +
                        "      \"external_urls\": {}, " +
                        "      \"artists\": []" +
                        "  }}" +
                        "]}";

        server.expect(requestTo(URL))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        List<SpotifyTrackDTO> tracks = service.getPlaylistTracks(PLAYLIST_ID, TOKEN);

        assertThat(tracks).hasSize(1);
        SpotifyTrackDTO t = tracks.get(0);
        assertThat(t.id()).isEqualTo("tX");
        assertThat(t.name()).isEqualTo("No Extras");
        assertThat(t.albumName()).isEqualTo("");
        assertThat(t.previewUrl()).isNull();
        assertThat(t.externalUrl()).isEqualTo("");
        assertThat(t.artists()).isEmpty();
    }
}