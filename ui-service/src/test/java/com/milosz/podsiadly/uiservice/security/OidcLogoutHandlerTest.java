package com.milosz.podsiadly.uiservice.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.springframework.security.core.Authentication;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@DisplayName("OidcLogoutHandler Tests")
class OidcLogoutHandlerTest {

    private SpotifyTokenCache spotifyTokenCache;
    private OidcLogoutHandler handler;

    @BeforeAll
    static void beforeAll() {
        System.out.println("🔧 [BeforeAll] Starting OidcLogoutHandler tests...");
    }

    @AfterAll
    static void afterAll() {
        System.out.println("✅ [AfterAll] OidcLogoutHandler tests completed.");
    }

    @BeforeEach
    void setUp() {
        System.out.println("➡️ [BeforeEach] Setting up test data...");
        spotifyTokenCache = mock(SpotifyTokenCache.class);
        handler = new OidcLogoutHandler(spotifyTokenCache);
    }

    @AfterEach
    void tearDown() {
        System.out.println("⬅️ [AfterEach] Cleaning up after test...");
        spotifyTokenCache = null;
        handler = null;
    }

    @Test
    @DisplayName("🟢 Should clear cache, delete cookies and redirect on logout success")
    void shouldClearCacheDeleteCookiesAndRedirect() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        Authentication authentication = mock(Authentication.class);

        ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);

        handler.onLogoutSuccess(request, response, authentication);

        verify(spotifyTokenCache).clear();
        verify(response, times(4)).addCookie(cookieCaptor.capture());
        verify(response).sendRedirect("/logged-out");

        List<Cookie> cookies = cookieCaptor.getAllValues();
        assertThat(cookies).hasSize(4);

        assertThat(cookies)
                .extracting(Cookie::getName)
                .containsExactlyInAnyOrder("jwt", "spotify_access_token", "spotify_id", "JSESSIONID");

        cookies.forEach(c -> {
            assertThat(c.getValue()).isNull();
            assertThat(c.getMaxAge()).isZero();
            assertThat(c.getPath()).isEqualTo("/");
            assertThat(c.getSecure()).isFalse();
            assertThat(c.isHttpOnly()).isFalse();
        });
    }

    @Test
    @DisplayName("🟡 Should be idempotent when called multiple times")
    void shouldBeIdempotentOnMultipleCalls() throws IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        Authentication authentication = mock(Authentication.class);

        handler.onLogoutSuccess(request, response, authentication);
        handler.onLogoutSuccess(request, response, authentication);


        verify(spotifyTokenCache, times(2)).clear();
        verify(response, times(8)).addCookie(any(Cookie.class));
        verify(response, times(2)).sendRedirect("/logged-out");
    }
}
