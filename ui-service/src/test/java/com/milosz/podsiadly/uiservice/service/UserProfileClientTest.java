package com.milosz.podsiadly.uiservice.service;

import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.ResponseActions;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class UserProfileClientTest {

    private RestTemplate providedRt;
    private MockRestServiceServer server;
    private UserProfileClient client;

    @BeforeEach
    void setUp() {
        providedRt = new RestTemplate();
        server = MockRestServiceServer.createServer(providedRt);

        ObjectProvider<RestTemplate> provider = new ObjectProvider<>() {
            @Override public RestTemplate getIfAvailable() { return providedRt; }
            @Override public RestTemplate getIfUnique() { return providedRt; }

            @Override public RestTemplate getObject(Object... args) { throw new UnsupportedOperationException(); }

            @Override public RestTemplate getObject() { throw new UnsupportedOperationException(); }
            @Override public RestTemplate getIfAvailable(java.util.function.Supplier<RestTemplate> s) { return providedRt; }
            @Override public RestTemplate getIfUnique(java.util.function.Supplier<RestTemplate> s) { return providedRt; }
        };

        client = new UserProfileClient(provider, "http://user-service:8081");
    }

    @AfterEach
    void tearDown() {
        if (server != null) server.verify();
    }

    @Test
    @DisplayName("Encodes spotifyId once and sends JSON with only non-blank fields")
    void encodesId_and_sendsSelectiveJson() {
        String spotifyId = "user:abc+xyz@id";
        String encodedOnce = URLEncoder.encode(spotifyId, StandardCharsets.UTF_8);
        String encodedTwice = URLEncoder.encode(encodedOnce, StandardCharsets.UTF_8);

        String expectedUrl = "http://user-service:8081/api/user-profiles/" + encodedTwice + "/ensure";

        expectPOST(expectedUrl)
                .andExpect(header(HttpHeaders.ACCEPT, startsWith(MediaType.APPLICATION_JSON_VALUE)))
                .andExpect(header(HttpHeaders.CONTENT_TYPE, startsWith(MediaType.APPLICATION_JSON_VALUE)))
                .andExpect(jsonPath("$.displayName").value("Alice Wonderland"))
                .andExpect(jsonPath("$.bio").doesNotExist())
                .andExpect(jsonPath("$.avatarUrl").doesNotExist())
                .andRespond(withCreatedEntity(URI.create(expectedUrl)));

        client.createProfileIfAbsent(spotifyId, "Alice Wonderland", "   ", "   ");
    }

    @Test
    @DisplayName("When all optional fields blank/null → no JSON body (no Content-Type), only Accept header")
    void noBody_whenAllFieldsBlank() {
        String spotifyId = "plainUser";
        String encoded = URLEncoder.encode(spotifyId, StandardCharsets.UTF_8);
        String expectedUrl = "http://user-service:8081/api/user-profiles/" + encoded + "/ensure";

        expectPOST(expectedUrl)
                .andExpect(header(HttpHeaders.ACCEPT, startsWith(MediaType.APPLICATION_JSON_VALUE)))
                .andExpect(headerDoesNotExist(HttpHeaders.CONTENT_TYPE))
                .andExpect(content().string(""))
                .andRespond(withNoContent());

        client.createProfileIfAbsent(spotifyId, null, "", "   ");
    }

    @Test
    @DisplayName("Non-2xx is tolerated (warning logged), no exception thrown")
    void non2xx_isTolerated() {
        String expectedUrl = "http://user-service:8081/api/user-profiles/id123/ensure";

        expectPOST(expectedUrl)
                .andRespond(withStatus(org.springframework.http.HttpStatus.CONFLICT)); // 409

        client.createProfileIfAbsent("id123", "Bob", null, null);
    }

    @Test
    @DisplayName("Exceptions from RestTemplate are swallowed (error logged)")
    void exception_isSwallowed() {
        RestTemplate mockRt = Mockito.mock(RestTemplate.class);
        ObjectProvider<RestTemplate> provider = new ObjectProvider<>() {
            @Override public RestTemplate getIfAvailable() { return mockRt; }
            @Override public RestTemplate getIfUnique() { return mockRt; }
            @Override public RestTemplate getObject(Object... args) { return mockRt; }
            @Override public RestTemplate getObject() { return mockRt; }
            @Override public RestTemplate getIfAvailable(java.util.function.Supplier<RestTemplate> s) { return mockRt; }
            @Override public RestTemplate getIfUnique(java.util.function.Supplier<RestTemplate> s) { return mockRt; }
        };
        UserProfileClient throwingClient = new UserProfileClient(provider, "http://user-service:8081");

        doThrow(new RuntimeException("boom"))
                .when(mockRt).postForEntity(anyString(), any(), eq(Void.class));

        Assertions.assertDoesNotThrow(() ->
                throwingClient.createProfileIfAbsent("id", "n", null, null)
        );
    }

    @Test
    @DisplayName("When provider returns null, client constructs its own RestTemplate (timeouts set) and still works")
    void constructsOwnRestTemplate_whenProviderNull() {
        ObjectProvider<RestTemplate> provider = new ObjectProvider<>() {
            @Override public RestTemplate getIfAvailable() { return null; }
            @Override public RestTemplate getIfUnique() { return null; }
            @Override public RestTemplate getObject(Object... args) { return null; }
            @Override public RestTemplate getObject() { return null; }
            @Override public RestTemplate getIfAvailable(java.util.function.Supplier<RestTemplate> s) { return null; }
            @Override public RestTemplate getIfUnique(java.util.function.Supplier<RestTemplate> s) { return null; }
        };
        UserProfileClient ownRtClient = new UserProfileClient(provider, "http://user-service:8081");

        RestTemplate internalRt = (RestTemplate) ReflectionTestUtils.getField(ownRtClient, "restTemplate");
        Assertions.assertNotNull(internalRt, "Internal RestTemplate should be created when provider is null");

        MockRestServiceServer internalServer = MockRestServiceServer.createServer(internalRt);

        String expectedUrl = "http://user-service:8081/api/user-profiles/u/ensure";
        expectPOST(internalServer, expectedUrl)
                .andRespond(withSuccess());

        ownRtClient.createProfileIfAbsent("u", null, null, null);

        internalServer.verify();
    }

    private ResponseActions expectPOST(String url) {
        return expectPOST(this.server, url);
    }

    private static ResponseActions expectPOST(MockRestServiceServer server, String url) {
        return server.expect(once(), requestTo(URI.create(url)))
                .andExpect(method(HttpMethod.POST));
    }

    private static org.springframework.test.web.client.RequestMatcher headerDoesNotExist(String name) {
        return request -> {
            if (request.getHeaders().containsKey(name)) {
                throw new AssertionError("Header '" + name + "' should NOT be present");
            }
        };
    }
}