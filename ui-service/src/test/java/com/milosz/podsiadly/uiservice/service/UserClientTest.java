package com.milosz.podsiadly.uiservice.service;

import org.junit.jupiter.api.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.ResponseActions;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@DisplayName("UserClientTest class")
class UserClientTest {

    private RestTemplate restTemplate;
    private MockRestServiceServer server;
    private UserClient client;

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
        server = MockRestServiceServer.createServer(restTemplate);

        client = new UserClient();
        ReflectionTestUtils.setField(client, "restTemplate", restTemplate);
    }

    @AfterEach
    void tearDown() {
        server.verify();
    }

    @Test
    @DisplayName("POSTs JSON to /api/users/create with provided name/email")
    void postsJsonWithProvidedValues() {
        String expectedUrl = "http://user-service:8081/api/users/create";

        expectJsonPOST(expectedUrl)
                .andExpect(header(HttpHeaders.CONTENT_TYPE, startsWith(MediaType.APPLICATION_JSON_VALUE)))
                .andExpect(jsonPath("$.spotifyId").value("sp:123"))
                .andExpect(jsonPath("$.name").value("Alice"))
                .andExpect(jsonPath("$.email").value("a@example.com"))
                .andRespond(withCreatedEntity(URI.create(expectedUrl)));

        client.createUserIfNotExists("sp:123", "Alice", "a@example.com");
    }

    @Test
    @DisplayName("Null name/email -> defaults used in JSON (\"Unknown\", \"unknown@example.com\")")
    void defaultsWhenNulls() {
        String expectedUrl = "http://user-service:8081/api/users/create";

        expectJsonPOST(expectedUrl)
                .andExpect(jsonPath("$.spotifyId").value("sp:xyz"))
                .andExpect(jsonPath("$.name").value("Unknown"))
                .andExpect(jsonPath("$.email").value("unknown@example.com"))
                .andRespond(withSuccess());

        client.createUserIfNotExists("sp:xyz", null, null);
    }

    @Test
    @DisplayName("Server error is swallowed (method does not throw)")
    void serverErrorIsSwallowed() {
        String expectedUrl = "http://user-service:8081/api/users/create";

        expectJsonPOST(expectedUrl)
                .andRespond(withServerError());

        client.createUserIfNotExists("sp:oops", "Bob", "b@example.com");
    }

    private ResponseActions expectJsonPOST(String url) {
        return server.expect(once(), requestTo(URI.create(url)))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.ACCEPT, startsWith("application/json")))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }
}