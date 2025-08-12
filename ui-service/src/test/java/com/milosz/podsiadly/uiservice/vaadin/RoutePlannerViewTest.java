package com.milosz.podsiadly.uiservice.vaadin;

import com.milosz.podsiadly.uiservice.security.SpotifyTokenCache;
import com.milosz.podsiadly.uiservice.service.TripPlanClient;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinService;
import jakarta.servlet.http.Cookie;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
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

@DisplayName("RoutePlannerView - tests")
class RoutePlannerViewTest {

    private TripPlanClient tripPlanClient;
    private SpotifyTokenCache tokenCache;
    private RoutePlannerView view;

    private RestTemplate rtInsideView;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() throws Exception {
        tripPlanClient = mock(TripPlanClient.class);
        tokenCache = mock(SpotifyTokenCache.class);

        view = new RoutePlannerView(tripPlanClient, tokenCache);

        Field f = RoutePlannerView.class.getDeclaredField("restTemplate");
        f.setAccessible(true);
        rtInsideView = (RestTemplate) f.get(view);
        server = MockRestServiceServer.createServer(rtInsideView);
    }

    @AfterEach
    void tearDown() {
        server.verify();
    }

    @Test
    @DisplayName("City info: 'London', limit=3 ⟶ renders Results without add buttons")
    void cityInfo_limit3_andRenders_noAddButtons() throws Exception {
        server.expect(once(),
                        requestTo(Matchers.containsString("q=London")))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.USER_AGENT, "travel-app"))
                .andRespond(withSuccess(
                        """
                        [
                          { "display_name": "Big Ben", "lat": "51.5007", "lon": "-0.1246" }
                        ]
                        """,
                        MediaType.APPLICATION_JSON
                ));

        invokeFetch("London", 3, false);

        assertThat(find(H1.class).map(H1::getText)).hasValue("📍 Results");
        assertThat(findAll(Paragraph.class, p -> p.getText().contains("Big Ben"))).hasSize(1);
        assertThat(findAll(Button.class, b -> "➕ Add to plan trip".equals(b.getText()))).isEmpty();
    }

    @Test
    @DisplayName("Category: 'museum in Warsaw', limit=10 ⟶ renders Results + add buttons")
    void category_limit10_andRenders_withAddButtons() throws Exception {
        server.expect(once(),
                        requestTo(Matchers.containsString("q=museum+in+Warsaw")))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.USER_AGENT, "travel-app"))
                .andRespond(withSuccess(
                        """
                        [
                          { "display_name": "POLIN Museum",    "lat": "52.251", "lon": "20.999" },
                          { "display_name": "National Museum", "lat": "52.231", "lon": "21.022" }
                        ]
                        """,
                        MediaType.APPLICATION_JSON
                ));

        invokeFetch("museum in Warsaw", 10, true);

        assertThat(find(H1.class).map(H1::getText)).hasValue("📍 Results");
        assertThat(findAll(Paragraph.class, p -> p.getText().contains("POLIN Museum"))).hasSize(1);
        assertThat(findAll(Paragraph.class, p -> p.getText().contains("National Museum"))).hasSize(1);
        assertThat(findAll(Button.class, b -> "➕ Add to plan trip".equals(b.getText()))).hasSize(2);
    }

    @Test
    @DisplayName("extractTokenFromCookie: reads cookie 'jwt' via VaadinService.getCurrentRequest()")
    void extractTokenFromCookie_readsJwt() throws Exception {
        Cookie[] cookies = {
                new Cookie("x", "1"),
                new Cookie("jwt", "jwt-123"),
                new Cookie("y", "2")
        };
        VaadinRequest req = mock(VaadinRequest.class);
        when(req.getCookies()).thenReturn(cookies);

        try (MockedStatic<VaadinService> mocked = mockStatic(VaadinService.class)) {
            mocked.when(VaadinService::getCurrentRequest).thenReturn(req);

            Method m = RoutePlannerView.class.getDeclaredMethod("extractTokenFromCookie");
            m.setAccessible(true);
            String jwt = (String) m.invoke(view);

            assertThat(jwt).isEqualTo("jwt-123");
        }
    }

    private void invokeFetch(String query, int limit, boolean allowAddToPlan) throws Exception {
        Method m = RoutePlannerView.class.getDeclaredMethod("fetchLocations", String.class, int.class, boolean.class);
        m.setAccessible(true);
        m.invoke(view, query, limit, allowAddToPlan);
    }

    private Stream<Component> descendants(Component c) {
        return Stream.concat(Stream.of(c), c.getChildren().flatMap(this::descendants));
    }

    private <T extends Component> Optional<T> find(Class<T> cls) {
        return descendants(view).filter(cls::isInstance).map(cls::cast).findFirst();
    }

    private <T extends Component> List<T> findAll(Class<T> cls, Predicate<T> pred) {
        return descendants(view).filter(cls::isInstance).map(cls::cast).filter(pred).toList();
    }
}