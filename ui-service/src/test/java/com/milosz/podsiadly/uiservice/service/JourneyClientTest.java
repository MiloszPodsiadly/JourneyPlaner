package com.milosz.podsiadly.uiservice.service;

import com.milosz.podsiadly.uiservice.dto.RouteResponse;
import org.junit.jupiter.api.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.ResponseActions;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@DisplayName("JourneyClient – works with record DTOs")
class JourneyClientTest {

    private RestTemplate restTemplate;
    private MockRestServiceServer server;
    private JourneyClient client;

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
        server = MockRestServiceServer.createServer(restTemplate);
        client = new JourneyClient(restTemplate);
    }

    @AfterEach
    void tearDown() {
        server.verify();
    }

    @Test
    @DisplayName("createRoute: GET + Bearer + maps RouteResponse (distanceMeters/durationSeconds aliases)")
    void createRoute_ok_withBearer() {
        String expectedUrl = "http://route-service:8083/api/osrm/route?tripPlanId=7&optimize=true";
        String json = """
            {
              "distanceMeters": 1234.5,
              "durationSeconds": 678.9,
              "geometry": { "type":"LineString", "coordinates":[[19.9,50.0],[20.0,50.1]] },
              "orderedPlaceIds": [10,11,12]
            }
            """;

        expectJsonGET(expectedUrl)
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer jwt-123"))
                .andExpect(header(HttpHeaders.USER_AGENT, "travel-app-ui"))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        RouteResponse out = client.createRoute(7L, true, "jwt-123");

        assertThat(out).isNotNull();
        assertThat(out.distance()).isEqualTo(1234.5);
        assertThat(out.duration()).isEqualTo(678.9);
        assertThat(out.geometry()).isNotNull();
        assertThat(out.orderedIds()).containsExactly(10L, 11L, 12L);
    }

    @Test
    @DisplayName("routeByMode(driving): correct URL + headers, maps RouteResponse (distance/duration fields)")
    void routeByMode_driving_ok() {
        String expectedUrl = "http://route-service:8083/api/osrm/route/driving?tripPlanId=99";
        String json = """
            {
              "distance": 42.0,
              "duration": 7.0,
              "geometry": null,
              "orderedIds": [1,2]
            }
            """;

        expectJsonGET(expectedUrl)
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer abc"))
                .andExpect(header(HttpHeaders.USER_AGENT, "travel-app-ui"))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        RouteResponse out = client.routeByMode(99L, "driving", "abc");

        assertThat(out).isNotNull();
        assertThat(out.distance()).isEqualTo(42.0);
        assertThat(out.duration()).isEqualTo(7.0);
        assertThat(out.orderedIds()).containsExactly(1L, 2L);
    }

    @Test
    @DisplayName("routeByMode: unsupported → IllegalArgumentException")
    void routeByMode_unsupportedMode_throws() {
        assertThatThrownBy(() -> client.routeByMode(1L, "flying", "tok"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported mode");
    }

    @Test
    @DisplayName("createRoute: blank jwt → no Authorization header")
    void createRoute_noJwt_noAuthHeader() {
        String expectedUrl = "http://route-service:8083/api/osrm/route?tripPlanId=1&optimize=false";
        String json = """
            {
              "distance": 1.0,
              "duration": 2.0,
              "geometry": null,
              "orderedIds": []
            }
            """;

        expectJsonGET(expectedUrl)
                .andExpect(noHeader(HttpHeaders.AUTHORIZATION))
                .andExpect(header(HttpHeaders.USER_AGENT, "travel-app-ui"))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        RouteResponse out = client.createRoute(1L, false, "   ");

        assertThat(out).isNotNull();
        assertThat(out.distance()).isEqualTo(1.0);
        assertThat(out.duration()).isEqualTo(2.0);
        assertThat(out.orderedIds()).isEmpty();
    }

    private ResponseActions expectJsonGET(String url) {
        return server.expect(once(), requestTo(URI.create(url)))
                .andExpect(method(HttpMethod.GET))
                .andExpect(acceptAnyOrMissing());
    }

    private static org.springframework.test.web.client.RequestMatcher acceptAnyOrMissing() {
        return request -> {
            var headers = request.getHeaders();
            if (!headers.containsKey(HttpHeaders.ACCEPT)) return;
        };
    }

    private static org.springframework.test.web.client.RequestMatcher noHeader(String name) {
        return request -> {
            if (request.getHeaders().containsKey(name)) {
                throw new AssertionError("Header '" + name + "' should NOT be present");
            }
        };
    }
}
