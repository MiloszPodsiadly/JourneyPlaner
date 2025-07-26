package com.milosz.podsiadly.uiservice.vaadin;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.Theme;

@Route("")
public class MainView extends VerticalLayout {
    public MainView() {
        Span greeting = new Span("Hello, Vaadin!");
        add(greeting);
    }
}

