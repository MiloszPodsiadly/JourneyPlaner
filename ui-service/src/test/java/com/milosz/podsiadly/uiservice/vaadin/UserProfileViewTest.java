package com.milosz.podsiadly.uiservice.vaadin;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinService;

import jakarta.servlet.http.Cookie;

import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.springframework.http.*;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@DisplayName("UserProfileView – tests")
class UserProfileViewTest {

    private UI ui;
    private UserProfileView view;

    private RestTemplate rt;
    private MockRestServiceServer server;

    private VaadinRequest requestMock;
    private MockedStatic<VaadinService> vaadinServiceStatic;

    @BeforeEach
    void setUp() throws Exception {
        requestMock = mock(VaadinRequest.class);
        when(requestMock.getCookies()).thenReturn(null);

        vaadinServiceStatic = mockStatic(VaadinService.class);
        vaadinServiceStatic.when(VaadinService::getCurrentRequest).thenReturn(requestMock);

        ui = new UI();
        UI.setCurrent(ui);

        view = new UserProfileView();

        rt = new RestTemplate();
        server = MockRestServiceServer.createServer(rt);
        Field f = UserProfileView.class.getDeclaredField("rest");
        f.setAccessible(true);
        f.set(view, rt);
    }

    @AfterEach
    void tearDown() {
        if (server != null) server.verify();
        if (vaadinServiceStatic != null) vaadinServiceStatic.close();
        UI.setCurrent(null);
    }

    @Test
    @DisplayName("loadProfile(): with cookie + 200 JSON → fields & avatar populated")
    void loadProfile_populatesUi() throws Exception {
        Cookie[] cookies = { new Cookie("spotify_id", "user:abc+xyz@id") };
        when(requestMock.getCookies()).thenReturn(cookies);

        String encoded = UriUtils.encodePathSegment("user:abc+xyz@id", StandardCharsets.UTF_8);
        String url = "http://user-service:8081/api/user-profiles/" + encoded;

        String body = """
        {
          "id": 10,
          "userId": 20,
          "displayName": "Alice",
          "bio": "Travel lover",
          "avatarUrl": "http://cdn.example.com/avatars/fox.png"
        }
        """;

        server.expect(once(), requestTo(url))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

        invoke(view, "loadProfile");

        TextField name = get(view, "displayName");
        TextArea  bio  = get(view, "bio");
        Image     img  = get(view, "currentAvatar");

        assertThat(name.getValue()).isEqualTo("Alice");
        assertThat(bio.getValue()).isEqualTo("Travel lover");
        assertThat(img.getSrc()).isEqualTo("http://cdn.example.com/avatars/fox.png");
    }

    @Test
    @DisplayName("loadProfile(): missing cookie → default avatar, no blow-ups")
    void loadProfile_noCookie_usesDefault() throws Exception {
        when(requestMock.getCookies()).thenReturn(null);

        invoke(view, "loadProfile");

        Image img = get(view, "currentAvatar");
        assertThat(img.getSrc()).isEqualTo("/images/avatars/owl.png");

        TextField name = get(view, "displayName");
        TextArea  bio  = get(view, "bio");
        assertThat(name.getValue()).isEmpty();
        assertThat(bio.getValue()).isEmpty();
    }

    @Test
    @DisplayName("saveProfile(): valid form + absolute avatar URL → PUT with JSON sent")
    void saveProfile_putsJson() throws Exception {
        Cookie[] cookies = { new Cookie("spotify_id", "user:abc+xyz@id") };
        when(requestMock.getCookies()).thenReturn(cookies);

        TextField name = get(view, "displayName");
        TextArea  bio  = get(view, "bio");
        name.setValue("Alice Updated");
        bio.setValue("New bio");

        set(view, "selectedAvatarUrl", "http://cdn.example.com/avatars/owl.png");
        set(view, "profileId", 10L);
        set(view, "userId", 20L);

        String encoded = UriUtils.encodePathSegment("user:abc+xyz@id", StandardCharsets.UTF_8);
        String url = "http://user-service:8081/api/user-profiles/" + encoded;

        server.expect(once(), requestTo(url))
                .andExpect(method(HttpMethod.PUT))
                .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.userId").value(20))
                .andExpect(jsonPath("$.displayName").value("Alice Updated"))
                .andExpect(jsonPath("$.bio").value("New bio"))
                .andExpect(jsonPath("$.avatarUrl").value("http://cdn.example.com/avatars/owl.png"))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        invoke(view, "saveProfile");
    }

    @SuppressWarnings("unchecked")
    private static <T> T get(Object target, String field) throws Exception {
        Field f = target.getClass().getDeclaredField(field);
        f.setAccessible(true);
        return (T) f.get(target);
    }

    private static void set(Object target, String field, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(field);
        f.setAccessible(true);
        f.set(target, value);
    }

    private static void invoke(Object target, String method) throws Exception {
        Method m = target.getClass().getDeclaredMethod(method);
        m.setAccessible(true);
        m.invoke(target);
    }
}