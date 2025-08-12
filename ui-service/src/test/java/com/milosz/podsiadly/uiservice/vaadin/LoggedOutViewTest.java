package com.milosz.podsiadly.uiservice.vaadin;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.page.Page;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;


@DisplayName("LoggedOutView")
class LoggedOutViewTest {

    @Test
    @DisplayName("Adds header and schedules redirect to /login via executeJs")
    void constructs_addsHeader_andSchedulesRedirect() {
        Page page = mock(Page.class);
        UI ui = mock(UI.class);
        when(ui.getPage()).thenReturn(page);

        try (MockedStatic<UI> uiStatic = Mockito.mockStatic(UI.class)) {
            uiStatic.when(UI::getCurrent).thenReturn(ui);

            LoggedOutView view = new LoggedOutView();

            Optional<H1> header = view.getChildren()
                    .filter(c -> c instanceof H1)
                    .map(c -> (H1) c)
                    .findFirst();

            assertThat(header)
                    .as("H1 header should be added")
                    .isPresent();
            assertThat(header.get().getText())
                    .isEqualTo("Zostałeś wylogowany. Przekierowanie...");
            verify(page, times(1)).executeJs(argThat(js ->
                    js != null
                            && js.contains("setTimeout(")
                            && js.contains("window.location.replace(\"/login\")")
                            && js.contains("100")
            ));
        }
    }

    @Test
    @DisplayName("Has @Route(\"logged-out\") and @PermitAll")
    void hasRouteAndPermitAllAnnotations() {
        Route route = LoggedOutView.class.getAnnotation(Route.class);
        assertThat(route).as("@Route present").isNotNull();
        assertThat(route.value()).isEqualTo("logged-out");

        PermitAll permitAll = LoggedOutView.class.getAnnotation(PermitAll.class);
        assertThat(permitAll).as("@PermitAll present").isNotNull();
    }

    @com.vaadin.flow.router.Route("dummy")
    private static class RouteImportHelper {}
}