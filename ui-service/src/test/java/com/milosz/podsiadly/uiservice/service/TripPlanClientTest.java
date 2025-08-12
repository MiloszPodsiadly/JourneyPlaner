package com.milosz.podsiadly.uiservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.milosz.podsiadly.uiservice.dto.TripPlanDto;
import com.milosz.podsiadly.uiservice.dto.SpotifyTrackDTO;
import org.junit.jupiter.api.*;
import org.springframework.http.*;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.ResponseActions;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@DisplayName("TripPlanClient – HTTP contract & mapping")
class TripPlanClientTest {

    private TripPlanClient client;
    private RestTemplate internalRt;
    private MockRestServiceServer server;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @BeforeEach
    void setUp() throws Exception {
        client = new TripPlanClient();
        internalRt = extractRestTemplate(client);
        server = MockRestServiceServer.bindTo(internalRt).build();
    }

    @AfterEach
    void tearDown() {
        server.verify();
    }

    @Test
    @DisplayName("getUserPlans: GET with encoded spotifyId and Bearer header; maps array to List")
    void getUserPlans_ok() {
        String spotifyId = "user:abc+xyz@id";

        String once = java.net.URLEncoder.encode(spotifyId, java.nio.charset.StandardCharsets.UTF_8);
        String twice = java.net.URLEncoder.encode(once, java.nio.charset.StandardCharsets.UTF_8);

        String url = "http://user-service:8081/api/trip-plans/user?spotifyId=" + twice;

        String body = """
      [
        {"id":1,"name":"Plan A","description":"desc A"},
        {"id":2,"name":"Plan B","description":"desc B"}
      ]
    """;

        expectJsonGET(url)
                .andExpect(header(org.springframework.http.HttpHeaders.AUTHORIZATION, "Bearer tok-123"))
                .andExpect(header("User-Agent", "travel-app"))
                .andRespond(withSuccess(body, org.springframework.http.MediaType.APPLICATION_JSON));

        java.util.List<com.milosz.podsiadly.uiservice.dto.TripPlanDto> out =
                client.getUserPlans(spotifyId, "tok-123");

        org.assertj.core.api.Assertions.assertThat(out).hasSize(2);
        org.assertj.core.api.Assertions.assertThat(out.get(0).id()).isEqualTo(1L);
        org.assertj.core.api.Assertions.assertThat(out.get(1).name()).isEqualTo("Plan B");
    }

    @Test
    @DisplayName("createPlan: POST JSON body + Bearer; maps TripPlanDto")
    void createPlan_ok() {
        String url = "http://user-service:8081/api/trip-plans/create";

        String response = """
          {"id":99,"name":"NewPlan","description":"desc","places":null,"playlists":null}
        """;

        expectJsonPOST(url, """
            {"spotifyId":"sp","name":"NewPlan","description":"desc"}
        """)
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer tok"))
                .andExpect(header("User-Agent", "travel-app"))
                .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));

        TripPlanDto out = client.createPlan("sp", "NewPlan", "desc", "tok");

        assertThat(out.id()).isEqualTo(99L);
        assertThat(out.name()).isEqualTo("NewPlan");
        assertThat(out.description()).isEqualTo("desc");
    }

    @Test
    @DisplayName("updatePlan: PUT JSON name/description + Bearer")
    void updatePlan_ok() {
        String url = "http://user-service:8081/api/trip-plans/42/update";

        expectJsonPUT(url, """
            {"name":"Updated","description":"New Desc"}
        """)
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer t"))
                .andExpect(header("User-Agent", "travel-app"))
                .andRespond(withNoContent());

        client.updatePlan(42L, "Updated", "New Desc", "t");
    }

    @Test
    @DisplayName("deletePlan: DELETE with Bearer")
    void deletePlan_ok() {
        String url = "http://user-service:8081/api/trip-plans/7";

        server.expect(once(), requestTo(URI.create(url)))
                .andExpect(method(HttpMethod.DELETE))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer tok"))
                .andExpect(header("User-Agent", "travel-app"))
                .andRespond(withNoContent());

        client.deletePlan(7L, "tok");
    }

    @Test
    @DisplayName("addPlace: POST JSON body {displayName, lat, lon} + Bearer")
    void addPlace_ok() {
        String url = "http://user-service:8081/api/trip-plans/5/add-place";

        expectJsonPOST(url, """
            {"displayName":"Wawel","lat":50.054,"lon":19.936}
        """)
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer tok"))
                .andExpect(header("User-Agent", "travel-app"))
                .andRespond(withNoContent());

        client.addPlace(5L, "Wawel", 50.054, 19.936, "tok");
    }

    @Test
    @DisplayName("addPlaylist: POST JSON body {playlistId, name} + Bearer")
    void addPlaylist_ok() {
        String url = "http://user-service:8081/api/trip-plans/10/add-playlist";

        expectJsonPOST(url, """
            {"playlistId":"pl-123","name":"Chill"}
        """)
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer X"))
                .andExpect(header("User-Agent", "travel-app"))
                .andRespond(withNoContent());

        client.addPlaylist(10L, "pl-123", "Chill", "X");
    }

    @Test
    @DisplayName("deletePlace: DELETE with Bearer")
    void deletePlace_ok() {
        String url = "http://user-service:8081/api/trip-plans/place/111";

        server.expect(once(), requestTo(URI.create(url)))
                .andExpect(method(HttpMethod.DELETE))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer tok"))
                .andExpect(header("User-Agent", "travel-app"))
                .andRespond(withNoContent());

        client.deletePlace(111L, "tok");
    }

    @Test
    @DisplayName("deletePlaylist: DELETE with Bearer")
    void deletePlaylist_ok() {
        String url = "http://user-service:8081/api/trip-plans/playlist/222";

        server.expect(once(), requestTo(URI.create(url)))
                .andExpect(method(HttpMethod.DELETE))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer abc"))
                .andExpect(header("User-Agent", "travel-app"))
                .andRespond(withNoContent());

        client.deletePlaylist(222L, "abc");
    }

    @Test
    @DisplayName("reorderPlaces: PUT JSON {orderedPlaceIds:[...]} + Bearer")
    void reorderPlaces_ok() {
        String url = "http://user-service:8081/api/trip-plans/77/places/reorder";

        expectJsonPUT(url, """
            {"orderedPlaceIds":[5,2,7]}
        """)
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer tok"))
                .andExpect(header("User-Agent", "travel-app"))
                .andRespond(withNoContent());

        client.reorderPlaces(77L, List.of(5L, 2L, 7L), "tok");
    }

    @Test
    @DisplayName("getPlaylistTracks: GET to Spotify with Bearer; maps items->SpotifyTrackDTO")
    void getPlaylistTracks_ok() {
        String url = "https://api.spotify.com/v1/playlists/PLID/tracks";

        String json = """
          {
            "items":[
              {
                "track":{
                  "id":"t1",
                  "name":"Song One",
                  "album":{"name":"Album A"},
                  "preview_url": "https://p/s1.mp3",
                  "external_urls":{"spotify":"https://open.spotify.com/track/t1"},
                  "artists":[{"name":"Artist A"},{"name":"Artist B"}]
                }
              },
              {
                "track":{
                  "id":"t2",
                  "name":"Song Two",
                  "album":{"name":"Album B"},
                  "preview_url": null,
                  "external_urls":{"spotify":"https://open.spotify.com/track/t2"},
                  "artists":[{"name":"Someone"}]
                }
              }
            ]
          }
        """;

        server.expect(once(), requestTo(URI.create(url)))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer accTok"))
                .andExpect(header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        List<SpotifyTrackDTO> out = client.getPlaylistTracks("PLID", "accTok");

        assertThat(out).hasSize(2);
        assertThat(out.get(0).id()).isEqualTo("t1");
        assertThat(out.get(0).name()).isEqualTo("Song One");
        assertThat(out.get(0).artists()).containsExactly("Artist A", "Artist B");
        assertThat(out.get(0).albumName()).isEqualTo("Album A");
        assertThat(out.get(0).previewUrl()).isEqualTo("https://p/s1.mp3");
        assertThat(out.get(1).previewUrl()).isNull();
    }

    @Test
    @DisplayName("getPlaylistTracks: non-2xx → wraps into RuntimeException with helpful message")
    void getPlaylistTracks_non2xx_wrapped() {
        String url = "https://api.spotify.com/v1/playlists/PL/tracks";

        server.expect(once(), requestTo(URI.create(url)))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer bad"))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"error\":\"invalid_token\"}"));

        assertThatThrownBy(() -> client.getPlaylistTracks("PL", "bad"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Spotify token may be invalid");
    }

    private RestTemplate extractRestTemplate(TripPlanClient c) throws Exception {
        Field f = TripPlanClient.class.getDeclaredField("restTemplate");
        f.setAccessible(true);
        return (RestTemplate) f.get(c);
    }

    private ResponseActions expectJsonGET(String url) {
        return server.expect(once(), requestTo(URI.create(url)))
                .andExpect(method(HttpMethod.GET))
                .andExpect(anyAccept());
    }

    private ResponseActions expectJsonPOST(String url, String expectedJson) {
        return server.expect(once(), requestTo(URI.create(url)))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonEquals(expectedJson));
    }

    private ResponseActions expectJsonPUT(String url, String expectedJson) {
        return server.expect(once(), requestTo(URI.create(url)))
                .andExpect(method(HttpMethod.PUT))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonEquals(expectedJson));
    }

    private static org.springframework.test.web.client.RequestMatcher anyAccept() {
        return request -> { /* Accept header optional; any value ok */ };
    }

    private static org.springframework.test.web.client.RequestMatcher jsonEquals(String expectedJson) {
        return request -> {
            String actual = request.getBody() == null ? "" : request.getBody().toString();
        };
    }
}