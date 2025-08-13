package com.milosz.podsiadly.uiservice.vaadin;

import com.milosz.podsiadly.uiservice.security.SpotifyTokenCache;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import org.junit.jupiter.api.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@DisplayName("SpotifyPlaylistsView – loading playlists & tracks via RestTemplate")
class SpotifyPlaylistsViewTest {

    private SpotifyTokenCache tokenCache;
    private SpotifyPlaylistsView view;

    private RestTemplate rtInsideView;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() throws Exception {
        tokenCache = mock(SpotifyTokenCache.class);
        view = new SpotifyPlaylistsView(tokenCache);

        Field f = SpotifyPlaylistsView.class.getDeclaredField("restTemplate");
        f.setAccessible(true);
        rtInsideView = (RestTemplate) f.get(view);
        server = MockRestServiceServer.createServer(rtInsideView);
    }

    @AfterEach
    void tearDown() {
        server.verify();
    }

    @Test
    @DisplayName("beforeEnter: loads playlists and renders buttons ▶️ and ➕")
    void beforeEnter_loadsPlaylists_rendersButtons() {
        when(tokenCache.getAccessToken()).thenReturn("tok-123");

        server.expect(once(), requestTo("https://api.spotify.com/v1/me/playlists"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer tok-123"))
                .andExpect(header(HttpHeaders.USER_AGENT, "travel-app"))
                .andRespond(withSuccess("""
                    {
                      "items": [
                        { "id": "p1", "name": "Rock" },
                        { "id": "p2", "name": "Chill" }
                      ]
                    }
                    """, MediaType.APPLICATION_JSON));

        view.beforeEnter(null);

        assertThat(find(H1.class).map(H1::getText)).hasValue("🎧 Your Spotify Playlists");
        assertThat(findButtonByText("▶️ Rock")).isPresent();
        assertThat(findButtonByText("▶️ Chill")).isPresent();

        assertThat(findAll(Button.class, b -> "➕ Add to trip plan".equals(b.getText()))).hasSize(2);
    }

    @Test
    @DisplayName("Clicking ▶️ on a playlist downloads and renders the songs '• NAME – Artist1, Artist2'")
    void clickingPlaylistButton_showsTracks() {
        when(tokenCache.getAccessToken()).thenReturn("tok-xyz");

        server.expect(once(), requestTo("https://api.spotify.com/v1/me/playlists"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer tok-xyz"))
                .andExpect(header(HttpHeaders.USER_AGENT, "travel-app"))
                .andRespond(withSuccess("""
                {
                  "items": [
                    { "id": "p1", "name": "MyList" }
                  ]
                }
                """, MediaType.APPLICATION_JSON));

        server.expect(once(), requestTo("https://api.spotify.com/v1/playlists/p1/tracks"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer tok-xyz"))
                .andExpect(header(HttpHeaders.USER_AGENT, "travel-app"))
                .andRespond(withSuccess("""
                {
                  "items": [
                    {
                      "track": {
                        "name": "Song A",
                        "artists": [
                          {"name": "Artist One"},
                          {"name": "Artist Two"}
                        ]
                      }
                    },
                    {
                      "track": {
                        "name": "Song B",
                        "artists": [
                          {"name": "Solo Artist"}
                        ]
                      }
                    }
                  ]
                }
                """, MediaType.APPLICATION_JSON));

        view.beforeEnter(null);

        findButtonByText("▶️ MyList").orElseThrow().click();

        assertThat(find(H1.class).map(H1::getText)).hasValue("🎵 Songs in the playlist");
        assertThat(findAll(Paragraph.class, p -> p.getText().contains("• Song A – Artist One, Artist Two"))).hasSize(1);
        assertThat(findAll(Paragraph.class, p -> p.getText().contains("• Song B – Solo Artist"))).hasSize(1);
    }

    @Test
    @DisplayName("getSpotifyId(token): returns the user ID from /v1/me and sets the correct headers")
    void getSpotifyId_returnsId() throws Exception {
        server.expect(once(), requestTo("https://api.spotify.com/v1/me"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer tok-777"))
                .andExpect(header(HttpHeaders.USER_AGENT, "travel-app"))
                .andRespond(withSuccess("""
                    { "id": "user-777" }
                    """, MediaType.APPLICATION_JSON));

        Method m = SpotifyPlaylistsView.class.getDeclaredMethod("getSpotifyId", String.class);
        m.setAccessible(true);
        String out = (String) m.invoke(view, "tok-777");

        assertThat(out).isEqualTo("user-777");
    }

    @Test
    @DisplayName("fetchPlaylists: when there are no 'items' in the response → returns an empty list (no exception)")
    void fetchPlaylists_missingItems_returnsEmptyList() throws Exception {
        server.expect(once(), requestTo("https://api.spotify.com/v1/me/playlists"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer tok-empty"))
                .andExpect(header(HttpHeaders.USER_AGENT, "travel-app"))
                .andRespond(withSuccess("""
                    { "href": "https://api.spotify.com/v1/me/playlists" }
                    """, MediaType.APPLICATION_JSON));

        Method m = SpotifyPlaylistsView.class.getDeclaredMethod("fetchPlaylists", String.class);
        m.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<?> result = (List<?>) m.invoke(view, "tok-empty");

        assertThat(result).isNotNull().isEmpty();
    }

    private Stream<Component> descendants(Component c) {
        return Stream.concat(Stream.of(c), c.getChildren().flatMap(this::descendants));
    }

    private <T extends Component> Optional<T> find(Class<T> cls) {
        return descendants(view).filter(cls::isInstance).map(cls::cast).findFirst();
    }

    private <T extends Component> List<T> findAll(Class<T> cls, Predicate<T> pred) {
        return descendants(view)
                .filter(cls::isInstance)
                .map(cls::cast)
                .filter(pred)
                .toList();
    }

    private Optional<Button> findButtonByText(String text) {
        return findAll(Button.class, b -> text.equals(b.getText())).stream().findFirst();
    }
}