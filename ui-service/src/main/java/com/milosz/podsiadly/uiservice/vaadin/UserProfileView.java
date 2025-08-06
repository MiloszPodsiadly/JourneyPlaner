package com.milosz.podsiadly.uiservice.vaadin;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;


@Route("profile")
public class UserProfileView extends VerticalLayout {

    public UserProfileView() {
        add(new H1("⚙️ Your Profile"));
        add(new Button("⬅️ Back to menu", e -> getUI().ifPresent(ui -> ui.navigate("main-menu"))));
    }
}

