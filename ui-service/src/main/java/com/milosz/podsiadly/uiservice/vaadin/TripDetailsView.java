package com.milosz.podsiadly.uiservice.vaadin;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.springframework.stereotype.Component;

@Route("trip-details")
@Component
public class TripDetailsView extends VerticalLayout {

    public TripDetailsView() {
        add(new H1("🔍 Szczegóły wycieczki"));
        add(new Button("⬅️ Wróć do menu", e -> getUI().ifPresent(ui -> ui.navigate("main-menu"))));
    }
}

