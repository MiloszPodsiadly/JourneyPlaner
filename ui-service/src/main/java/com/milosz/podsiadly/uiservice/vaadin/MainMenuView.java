package com.milosz.podsiadly.uiservice.vaadin;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.springframework.stereotype.Component;

@Route("main-menu")
@Component
public class MainMenuView extends VerticalLayout {

    public MainMenuView() {
        add(new H1("Journey Planner - Menu"));

        Button playlists = new Button("🎵 Moje Playlisty", e ->
                getUI().ifPresent(ui -> ui.navigate("playlists")));

        Button route = new Button("📍 Planuj Trasę", e ->
                getUI().ifPresent(ui -> ui.navigate("plan-route")));

        Button trips = new Button("🗺️ Moje Wycieczki", e ->
                getUI().ifPresent(ui -> ui.navigate("my-trips")));

        Button profile = new Button("⚙️ Profil", e ->
                getUI().ifPresent(ui -> ui.navigate("profile")));

        Button logout = new Button("🚪 Wyloguj się", e ->
                getUI().ifPresent(ui -> ui.getPage().setLocation("/logout")));

        add(playlists, route, trips, profile, logout);
        setAlignItems(Alignment.CENTER);
        setSpacing(true);
    }
}


