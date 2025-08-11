package com.milosz.podsiadly.uiservice.config;

import com.milosz.podsiadly.uiservice.security.OAuth2LoginSuccessHandler;
import com.milosz.podsiadly.uiservice.security.OidcLogoutHandler;
import com.milosz.podsiadly.uiservice.security.SpotifyTokenCache;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.registration.*;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(
        classes = {
                SecurityConfig.class,
                SecurityConfigTest.TestBeans.class,
                SecurityConfigTest.DummyController.class
        }
)
@AutoConfigureMockMvc
@DisplayName("🔐 SecurityConfig Integration Tests")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @BeforeAll
    static void beforeAll() {
        System.out.println("🔧 [BeforeAll] Starting SecurityConfig tests...");
    }

    @AfterAll
    static void afterAll() {
        System.out.println("✅ [AfterAll] SecurityConfig tests completed.");
    }

    @BeforeEach
    void setUp() {
        System.out.println("➡️ [BeforeEach] Preparing case...");
    }

    @AfterEach
    void tearDown() {
        System.out.println("⬅️ [AfterEach] Cleanup done.");
    }

    @Test
    @DisplayName("🟢 Whitelisted path /logged-out is accessible without authentication (200)")
    void whitelistedEndpointAccessible() throws Exception {
        mockMvc.perform(get("/logged-out"))
                .andExpect(status().isOk())
                .andExpect(content().string("logged-out"));
    }

    @Test
    @DisplayName("🔒 Any other GET path requires auth → redirects to /login or /oauth2/authorization/spotify")
    void nonWhitelistedRequiresAuth() throws Exception {
        var mvcResult = mockMvc.perform(get("/secured").accept(MediaType.TEXT_HTML))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        String location = mvcResult.getResponse().getHeader("Location");
        assertThat(location).isNotNull();
        assertThat(location).matches(".*/(login|oauth2/authorization/spotify)$");

        mockMvc.perform(get("/login")).andExpect(status().isOk());
    }


    @Test
    @DisplayName("🔒 Any other POST path requires auth → redirects to /login or /oauth2/authorization/spotify (csrf disabled)")
    void postNonWhitelistedRequiresAuth() throws Exception {
        var res = mockMvc.perform(post("/secured").accept(MediaType.TEXT_HTML))
                .andExpect(status().is3xxRedirection())
                .andReturn()
                .getResponse();

        String location = res.getHeader("Location");
        assertThat(location).isNotNull();
        assertThat(location).matches(".*/(login|oauth2/authorization/spotify)$");
    }

    @Test
    @DisplayName("🚪 POST /logout clears cookies and redirects to /logged-out")
    void logoutClearsCookiesAndRedirects() throws Exception {
        var result = mockMvc.perform(post("/logout")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/logged-out"))
                .andReturn();

        var headers = result.getResponse().getHeaders("Set-Cookie");
        assertThat(headers).isNotEmpty();
        assertThat(headers.stream().anyMatch(h -> h.startsWith("jwt="))).isTrue();
        assertThat(headers.stream().anyMatch(h -> h.startsWith("spotify_access_token="))).isTrue();
        assertThat(headers.stream().anyMatch(h -> h.startsWith("spotify_id="))).isTrue();
        assertThat(headers.stream().anyMatch(h -> h.startsWith("JSESSIONID="))).isTrue();
    }

    @Test
    @DisplayName("🧾 Security headers are applied (Cache-Control, X-Frame-Options)")
    void securityHeadersApplied() throws Exception {
        var res = mockMvc.perform(get("/logged-out"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        assertThat(res.getHeader("X-Frame-Options")).isEqualTo("SAMEORIGIN");
        assertThat(res.getHeader("Cache-Control")).contains("no-cache");
    }

    @TestConfiguration
    static class TestBeans {

        @Bean
        HandlerMappingIntrospector mvcHandlerMappingIntrospector(ApplicationContext ctx) {
            return new HandlerMappingIntrospector();
        }

        @Bean
        ClientRegistrationRepository clientRegistrationRepository() {
            ClientRegistration spotify = ClientRegistration.withRegistrationId("spotify")
                    .clientId("test-client")
                    .clientSecret("test-secret")
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                    .scope("user-read-email")
                    .authorizationUri("https://example.com/auth")
                    .tokenUri("https://example.com/token")
                    .userInfoUri("https://example.com/userinfo")
                    .userNameAttributeName("id")
                    .clientName("spotify")
                    .build();
            return new InMemoryClientRegistrationRepository(spotify);
        }

        @Bean
        OAuth2AuthorizedClientService authorizedClientService(ClientRegistrationRepository repo) {
            return new InMemoryOAuth2AuthorizedClientService(repo);
        }

        @Bean
        OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler(
                OAuth2AuthorizedClientService clientService
        ) {
            var tokenCache = mock(SpotifyTokenCache.class);
            var jwt = mock(com.milosz.podsiadly.uiservice.config.JwtTokenUtil.class);
            var userClient = mock(com.milosz.podsiadly.uiservice.service.UserClient.class);
            var userProfileClient = mock(com.milosz.podsiadly.uiservice.service.UserProfileClient.class);

            return new com.milosz.podsiadly.uiservice.security.OAuth2LoginSuccessHandler(
                    jwt, clientService, tokenCache, userClient, userProfileClient
            );
        }

        @Bean
        OidcLogoutHandler oidcLogoutHandler() {
            return new OidcLogoutHandler(mock(SpotifyTokenCache.class));
        }
    }

    @RestController
    static class DummyController {
        @GetMapping("/logged-out")
        public String loggedOut() { return "logged-out"; }

        @GetMapping("/secured")
        public String secured() { return "secured"; }

        @GetMapping("/login")
        public String login() { return "login-page"; }
    }
}
