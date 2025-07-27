package com.milosz.podsiadly.uiservice.vaadin;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.springframework.stereotype.Component;

@Route("main-menu")
@Component
public class MainMenuView extends VerticalLayout {
    public MainMenuView() {
        add(new H1("This is the Main Menu"));
    }
}

