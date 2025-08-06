package com.milosz.podsiadly.uiservice.vaadin;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

@Route("logged-out")
@PermitAll
public class LoggedOutView extends VerticalLayout {

    public LoggedOutView() {
        add(new H1("Zostałeś wylogowany. Przekierowanie..."));

        UI.getCurrent().getPage().executeJs("""
            setTimeout(() => {
                window.location.replace("/login");
            }, 100);
        """);
    }
}
