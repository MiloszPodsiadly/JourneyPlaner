package com.milosz.podsiadly.uiservice.security;

import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("SpotifyTokenCache Unit Tests")
class SpotifyTokenCacheTest {

    private SpotifyTokenCache cache;
    private RestTemplate restTemplateMock;


    @BeforeAll
    static void beforeAll() {
        System.out.println("🔧 [BeforeAll] Starting SpotifyTokenCache tests...");
    }

    @AfterAll
    static void afterAll() {
        System.out.println("✅ [AfterAll] SpotifyTokenCache tests completed.");
    }

    @BeforeEach
    void setUp() {
        System.out.println("➡️ [BeforeEach] Setting up test data...");
        cache = new SpotifyTokenCache();
        restTemplateMock = mock(RestTemplate.class);
        ReflectionTestUtils.setField(cache, "restTemplate", restTemplateMock);
    }

    @AfterEach
    void tearDown() {
        cache = null;
        restTemplateMock = null;
        System.out.println("⬅️ [AfterEach] Cleaning up after test...");
    }

    @Test
    @DisplayName("🟢 update() sets the tokens and getAccessToken returns them if they haven't expired")
    void updateStoresTokensAndGetWithoutRefresh() {
        cache.update("access-A", "refresh-R", 3600);

        String token = cache.getAccessToken();

        assertThat(token).isEqualTo("access-A");
    }

    @Test
    @DisplayName("🟢 getAccessToken() refreshes the token when it has expired and is refresh_token")
    void getAccessTokenRefreshesWhenExpired() {
        cache.update("old-access", "refresh-123", 10);

        Map<String, Object> body = new HashMap<>();
        body.put("access_token", "new-access");
        body.put("expires_in", 1800);
        ResponseEntity<Map> okResponse = new ResponseEntity<>(body, HttpStatus.OK);

        when(restTemplateMock.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(okResponse);

        String token = cache.getAccessToken();

        assertThat(token).isEqualTo("new-access");
        verify(restTemplateMock, times(1))
                .exchange(eq("https://accounts.spotify.com/api/token"), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class));
    }

    @Test
    @DisplayName("🟡 getAccessToken() throws IllegalStateException when refresh_token is missing")
    void getAccessTokenThrowsWhenNoRefreshToken() {
        assertThatThrownBy(() -> cache.getAccessToken())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Brak refresh_token");

        verifyNoInteractions(restTemplateMock);
    }

    @Test
    @DisplayName("🔴 refreshAccessToken() throws an error and clears the accessToken when Spotify returns an error")
    void refreshAccessTokenFailsOnNon2xx() {
        cache.update("old", "refresh-zzz", 10); // natychmiast wygasły

        ResponseEntity<Map> badResp = new ResponseEntity<>(HttpStatus.BAD_GATEWAY);
        when(restTemplateMock.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(badResp);

        assertThatThrownBy(() -> cache.getAccessToken())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to refresh token");

        when(restTemplateMock.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(badResp);
        assertThatThrownBy(() -> cache.getAccessToken())
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("🧼 clear() clears accessToken, refreshToken and expiresAt")
    void clearResetsAll() {
        cache.update("a", "r", 3600);
        cache.clear();

        assertThatThrownBy(() -> cache.getAccessToken())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("🧩 refreshAccessToken sends correct headers and bodies to Spotify")
    void refreshSendsCorrectHeadersAndBody() {
        cache.update("old", "refresh-xyz", 10); // natychmiast wygasły

        Map<String, Object> body = new HashMap<>();
        body.put("access_token", "newer");
        body.put("expires_in", 1200);

        ArgumentCaptor<HttpEntity<String>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        when(restTemplateMock.exchange(eq("https://accounts.spotify.com/api/token"), eq(HttpMethod.POST),
                entityCaptor.capture(), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(body));

        String token = cache.getAccessToken();
        assertThat(token).isEqualTo("newer");

        HttpEntity<String> sent = entityCaptor.getValue();
        assertThat(sent).isNotNull();

        assertThat(sent.getBody()).isEqualTo("grant_type=refresh_token&refresh_token=refresh-xyz");

        HttpHeaders headers = sent.getHeaders();
        assertThat(headers.getContentType()).isEqualTo(MediaType.APPLICATION_FORM_URLENCODED);

        String auth = headers.getFirst(HttpHeaders.AUTHORIZATION);
        assertThat(auth).startsWith("Basic ");

        String base64 = auth.substring("Basic ".length());
        assertThatCode(() -> Base64.getDecoder().decode(base64.getBytes(StandardCharsets.UTF_8)))
                .doesNotThrowAnyException();
    }
}