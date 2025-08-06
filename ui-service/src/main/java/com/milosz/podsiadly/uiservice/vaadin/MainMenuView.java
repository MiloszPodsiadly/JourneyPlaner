package com.milosz.podsiadly.uiservice.vaadin;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route("main-menu")
public class MainMenuView extends VerticalLayout {

    public MainMenuView() {
        add(new H1("Journey Planner - Menu"));

        Button playlists = new Button("🎵 My Playlists", e ->
                getUI().ifPresent(ui -> ui.navigate("playlists")));

        Button route = new Button("📍 Plan Your Route", e ->
                getUI().ifPresent(ui -> ui.navigate("plan-route")));

        Button trips = new Button("🗺️ My Trips", e ->
                getUI().ifPresent(ui -> ui.navigate("my-trips")));

        Button profile = new Button("⚙️ Profil", e ->
                getUI().ifPresent(ui -> ui.navigate("profile")));

        Button logout = new Button("🚪 Log out", e -> {
            UI.getCurrent().getPage().executeJs("""
        fetch('/logout', {
            method: 'POST',
            credentials: 'same-origin'
        }).then(() => {
            document.cookie = 'jwt=; Max-Age=0; path=/';
            document.cookie = 'spotify_access_token=; Max-Age=0; path=/';
            document.cookie = 'spotify_id=; Max-Age=0; path=/';
            window.location.href = 'https://accounts.spotify.com/logout';
        });
    """);
        });
        add(playlists, route, trips, profile, logout);
        setAlignItems(Alignment.CENTER);
        setSpacing(true);
    }
}
