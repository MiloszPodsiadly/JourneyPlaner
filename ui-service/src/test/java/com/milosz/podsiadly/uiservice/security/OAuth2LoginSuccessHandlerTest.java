package com.milosz.podsiadly.uiservice.security;

import com.milosz.podsiadly.uiservice.config.JwtTokenUtil;
import com.milosz.podsiadly.uiservice.service.UserClient;
import com.milosz.podsiadly.uiservice.service.UserProfileClient;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.time.Instant;

import static org.mockito.Mockito.*;

@DisplayName("OAuth2LoginSuccessHandler Tests")
class OAuth2LoginSuccessHandlerTest {

    private JwtTokenUtil jwtTokenUtil;
    private OAuth2AuthorizedClientService authorizedClientService;
    private SpotifyTokenCache spotifyTokenCache;
    private UserClient userClient;
    private UserProfileClient userProfileClient;
    private OAuth2LoginSuccessHandler handler;

    @BeforeAll
    static void initAll() {
        System.out.println("🔧 [BeforeAll] Initializing OAuth2LoginSuccessHandler tests...");
    }

    @AfterAll
    static void tearDownAll() {
        System.out.println("✅ [AfterAll] All tests completed.");
    }

    @BeforeEach
    void setUp() {
        System.out.println("➡️ [BeforeEach] Setting up mocks...");
        jwtTokenUtil = mock(JwtTokenUtil.class);
        authorizedClientService = mock(OAuth2AuthorizedClientService.class);
        spotifyTokenCache = mock(SpotifyTokenCache.class);
        userClient = mock(UserClient.class);
        userProfileClient = mock(UserProfileClient.class);

        handler = new OAuth2LoginSuccessHandler(
                jwtTokenUtil,
                authorizedClientService,
                spotifyTokenCache,
                userClient,
                userProfileClient
        );
    }

    @AfterEach
    void tearDown() {
        System.out.println("⬅️ [AfterEach] Cleaning test environment...");
    }

    @Test
    @DisplayName("🟢 Should handle successful authentication with cookies and Spotify tokens")
    void shouldHandleAuthenticationSuccess() throws Exception {

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        Authentication authentication = mock(Authentication.class);
        OAuth2User user = mock(OAuth2User.class);

        when(authentication.getPrincipal()).thenReturn(user);
        when(user.getAttribute("id")).thenReturn("spotify123");
        when(user.getAttribute("email")).thenReturn("test@example.com");
        when(user.getAttribute("display_name")).thenReturn("Test User");
        when(jwtTokenUtil.generateToken(user)).thenReturn("mock-jwt-token");
        when(authentication.getName()).thenReturn("mock-name");

        OAuth2AccessToken accessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                "access-token-xyz",
                Instant.now(),
                Instant.now().plusSeconds(3600)
        );
        OAuth2RefreshToken refreshToken = new OAuth2RefreshToken("refresh-token-abc", Instant.now());

        OAuth2AuthorizedClient client = mock(OAuth2AuthorizedClient.class);
        when(client.getAccessToken()).thenReturn(accessToken);
        when(client.getRefreshToken()).thenReturn(refreshToken);
        when(authorizedClientService.loadAuthorizedClient("spotify", "mock-name")).thenReturn(client);

        handler.onAuthenticationSuccess(request, response, authentication);

        // cookies added (jwt, spotify_id, spotify_access_token)
        verify(response, atLeast(1)).addCookie(any(Cookie.class));

        // tokens cached
        verify(spotifyTokenCache).update(eq("access-token-xyz"), eq("refresh-token-abc"), anyInt());

        // user + profile created
        verify(userClient).createUserIfNotExists("spotify123", "Test User", "test@example.com");
        verify(userProfileClient).createProfileIfAbsent("spotify123", "Test User", null, null);

        // redirect to main menu
        verify(response).sendRedirect("/main-menu");
    }

    @Test
    @DisplayName("🟡 Should skip Spotify cookie and user/profile creation when ID is null")
    void shouldSkipUserCreationWhenIdIsNull() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        Authentication authentication = mock(Authentication.class);
        OAuth2User user = mock(OAuth2User.class);

        when(authentication.getPrincipal()).thenReturn(user);
        when(user.getAttribute("id")).thenReturn(null);
        when(jwtTokenUtil.generateToken(user)).thenReturn("token");
        when(authentication.getName()).thenReturn("test");
        when(authorizedClientService.loadAuthorizedClient(any(), any())).thenReturn(null);

        handler.onAuthenticationSuccess(request, response, authentication);

        verify(userClient, never()).createUserIfNotExists(any(), any(), any());
        verify(userProfileClient, never()).createProfileIfAbsent(any(), any(), any(), any());
        verify(response).sendRedirect("/main-menu");
    }
}
