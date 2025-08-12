package com.milosz.podsiadly.uiservice.vaadin;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.page.Page;
import com.vaadin.flow.router.Route;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@DisplayName("LoginView")
class LoginViewTest {

    @Test
    @DisplayName("Constructs UI: has logo, heading and Spotify login button")
    void constructs_hasBasicComponents() {
        ClientRegistrationRepository repo = mock(ClientRegistrationRepository.class);

        UI ui = mock(UI.class);
        Page page = mock(Page.class);
        when(ui.getPage()).thenReturn(page);

        try (MockedStatic<UI> uiStatic = Mockito.mockStatic(UI.class)) {
            uiStatic.when(UI::getCurrent).thenReturn(ui);

            LoginView view = new LoginView(repo);

            Optional<H1> heading = view.getChildren()
                    .filter(c -> c instanceof H1)
                    .map(c -> (H1) c)
                    .findFirst();
            assertThat(heading).isPresent();
            assertThat(heading.get().getText()).isEqualTo("Journey Planner");

            Optional<Button> btn = view.getChildren()
                    .filter(c -> c instanceof Button)
                    .map(c -> (Button) c)
                    .findFirst();
            assertThat(btn).isPresent();
            assertThat(btn.get().getText()).contains("Login with Spotify");
        }
    }

    @Test
    @DisplayName("Clicking login button with Spotify client configured redirects to /oauth2/authorization/spotify")
    void click_withSpotifyRegistration_redirects() {
        ClientRegistrationRepository repo = mock(ClientRegistrationRepository.class);
        when(repo.findByRegistrationId("spotify")).thenReturn(mock(ClientRegistration.class));

        UI ui = mock(UI.class);
        Page page = mock(Page.class);
        when(ui.getPage()).thenReturn(page);

        try (MockedStatic<UI> uiStatic = Mockito.mockStatic(UI.class)) {
            uiStatic.when(UI::getCurrent).thenReturn(ui);

            LoginView view = new LoginView(repo);

            Button btn = view.getChildren()
                    .filter(c -> c instanceof Button)
                    .map(c -> (Button) c)
                    .findFirst()
                    .orElseThrow();

            btn.click();

            verify(repo, times(1)).findByRegistrationId("spotify");
            verify(page, times(1)).setLocation("/oauth2/authorization/spotify");
        }
    }

    @Test
    @DisplayName("Clicking login button without Spotify client shows notification (no redirect)")
    void click_withoutSpotifyRegistration_noRedirect() {
        ClientRegistrationRepository repo = mock(ClientRegistrationRepository.class);
        when(repo.findByRegistrationId("spotify")).thenReturn(null);

        UI ui = mock(UI.class);
        Page page = mock(Page.class);
        when(ui.getPage()).thenReturn(page);

        try (MockedStatic<UI> uiStatic = Mockito.mockStatic(UI.class)) {
            uiStatic.when(UI::getCurrent).thenReturn(ui);

            LoginView view = new LoginView(repo);

            Button btn = view.getChildren()
                    .filter(c -> c instanceof Button)
                    .map(c -> (Button) c)
                    .findFirst()
                    .orElseThrow();

            btn.click();

            verify(repo, times(1)).findByRegistrationId("spotify");
            verify(page, never()).setLocation(anyString());
        }
    }

    @Test
    @DisplayName("Has @Route(\"login-home\")")
    void hasRouteAnnotation() {
        Route route = LoginView.class.getAnnotation(Route.class);
        assertThat(route).isNotNull();
        assertThat(route.value()).isEqualTo("login-home");
    }
}