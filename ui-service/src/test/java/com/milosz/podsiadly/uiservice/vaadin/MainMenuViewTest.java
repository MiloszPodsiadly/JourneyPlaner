package com.milosz.podsiadly.uiservice.vaadin;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.page.Page;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;


@DisplayName("MainMenuView – unit tests (navigation & JS)")
class MainMenuViewTest {

    private TestUI ui;
    private Page pageMock;
    private MainMenuView view;

    static class TestUI extends UI {
        String lastNavigationLocation;

        @Override
        public void navigate(String location) {
            this.lastNavigationLocation = location;
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        ui = new TestUI();
        UI.setCurrent(ui);

        pageMock = mock(Page.class);
        Field f = UI.class.getDeclaredField("page");
        f.setAccessible(true);
        f.set(ui, pageMock);

        view = new MainMenuView();
        ui.add(view);
    }

    @AfterEach
    void tearDown() {
        UI.setCurrent(null);
    }

    @Test
    @DisplayName("The view includes the title and all buttons")
    void hasTitleAndAllButtons() {
        H1 title = view.getChildren()
                .filter(c -> c instanceof H1)
                .map(c -> (H1) c)
                .findFirst()
                .orElse(null);

        assertThat(title).isNotNull();
        assertThat(title.getText()).contains("Journey Planner");

        for (String caption : new String[]{
                "🎵 My Playlists",
                "📍 Plan Your Route",
                "🗺️ My Trips",
                "🚗 Create Journey Plan",
                "⚙️ Profil",
                "🚪 Log out"
        }) {
            assertThat(findButtonByText(caption)).as("button '%s' exists", caption).isNotNull();
        }
    }

    @Test
    @DisplayName("Clicking the navigation buttons takes you to the correct routes")
    void navigationButtons_navigateToExpectedRoutes() {
        Map<String, String> mapping = Map.of(
                "🎵 My Playlists", "playlists",
                "📍 Plan Your Route", "plan-route",
                "🗺️ My Trips", "my-trips",
                "🚗 Create Journey Plan", "journey",
                "⚙️ Profil", "profile"
        );

        mapping.forEach((caption, route) -> {
            Button b = findButtonByText(caption);
            assertThat(b).as("button '%s'", caption).isNotNull();

            b.click();

            assertThat(ui.lastNavigationLocation)
                    .as("navigation after clicking '%s'", caption)
                    .isEqualTo(route);
        });
    }

    @Test
    @DisplayName("Clicking 'Log out' calls Page.executeJs(...) with fetch('/logout')")
    void logoutButton_executesJs() {
        Button logout = findButtonByText("🚪 Log out");
        assertThat(logout).isNotNull();

        logout.click();

        ArgumentCaptor<String> jsCaptor = ArgumentCaptor.forClass(String.class);
        verify(pageMock, times(1)).executeJs(jsCaptor.capture());

        String script = jsCaptor.getValue();
        assertThat(script)
                .as("logout JS should call fetch('/logout')")
                .contains("fetch('/logout'");
        assertThat(script).contains("document.cookie = 'jwt=");
        assertThat(script).contains("window.location.href = 'https://accounts.spotify.com/logout'");
    }

    @Test
    @DisplayName("Layout Properties: CENTER alignment and spacing enabled")
    void layoutProperties() {
        assertThat(view.getAlignItems()).isEqualTo(FlexComponent.Alignment.CENTER);
        assertThat(view.isSpacing()).isTrue();
    }

    private Button findButtonByText(String text) {
        return view.getChildren()
                .filter(c -> c instanceof Button)
                .map(c -> (Button) c)
                .filter(b -> text.equals(b.getText()))
                .findFirst()
                .orElse(null);
    }
}