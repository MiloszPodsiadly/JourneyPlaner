package com.milosz.podsiadly.uiservice.security;

import org.junit.jupiter.api.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("TokenProvider Unit Tests")
class TokenProviderTest {

    private OAuth2AuthorizedClientService clientService;
    private TokenProvider tokenProvider;

    @BeforeAll
    static void beforeAll() {
        System.out.println("🔧 [BeforeAll] Starting TokenProvider tests...");
    }

    @AfterAll
    static void afterAll() {
        System.out.println("✅ [AfterAll] TokenProvider tests completed.");
    }

    @BeforeEach
    void setUp() {
        System.out.println("➡️ [BeforeEach] Setting up mocks...");
        clientService = mock(OAuth2AuthorizedClientService.class);
        tokenProvider = new TokenProvider(clientService);
    }

    @AfterEach
    void tearDown() {
        System.out.println("⬅️ [AfterEach] Cleaning up...");
        clientService = null;
        tokenProvider = null;
    }

    @Test
    @DisplayName("🟢 Should return access token when client and token are present")
    void shouldReturnAccessToken() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user-123");

        OAuth2AccessToken token = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                "access-token-xyz",
                Instant.now().minusSeconds(60),
                Instant.now().plusSeconds(3600)
        );

        OAuth2AuthorizedClient client = mock(OAuth2AuthorizedClient.class);
        when(client.getAccessToken()).thenReturn(token);
        when(clientService.loadAuthorizedClient("spotify", "user-123")).thenReturn(client);

        String result = tokenProvider.getAccessToken(auth);

        assertThat(result).isEqualTo("access-token-xyz");
        verify(clientService).loadAuthorizedClient("spotify", "user-123");
    }

    @Test
    @DisplayName("🟡 Should return null when authorized client is missing")
    void shouldReturnNullWhenClientMissing() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user-missing");

        when(clientService.loadAuthorizedClient("spotify", "user-missing")).thenReturn(null);

        String result = tokenProvider.getAccessToken(auth);

        assertThat(result).isNull();
        verify(clientService).loadAuthorizedClient("spotify", "user-missing");
    }

    @Test
    @DisplayName("🔴 Should throw NPE when client exists but access token is null (current behavior)")
    void shouldThrowWhenAccessTokenNull() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user-npe");

        OAuth2AuthorizedClient client = mock(OAuth2AuthorizedClient.class);
        when(client.getAccessToken()).thenReturn(null);
        when(clientService.loadAuthorizedClient("spotify", "user-npe")).thenReturn(client);

        assertThatThrownBy(() -> tokenProvider.getAccessToken(auth))
                .isInstanceOf(NullPointerException.class);
    }
}