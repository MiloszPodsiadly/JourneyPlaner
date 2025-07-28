package com.milosz.podsiadly.uiservice.vaadin;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.springframework.stereotype.Component;

@Route("my-trips")
@Component
public class UserTripsView extends VerticalLayout {

    public UserTripsView() {
        add(new H1("🗺️ Moje wycieczki"));
        add(new Button("⬅️ Wróć do menu", e -> getUI().ifPresent(ui -> ui.navigate("main-menu"))));
    }
}

