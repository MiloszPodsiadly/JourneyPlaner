package com.milosz.podsiadly.uiservice.vaadin;

import com.milosz.podsiadly.uiservice.dto.TripPlanDto;
import com.milosz.podsiadly.uiservice.service.TripPlanClient;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinService;
import jakarta.servlet.http.Cookie;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;

import java.util.List;

@Slf4j
@Route("my-trips")
public class TripPlanCreateView extends VerticalLayout {

    private final TripPlanClient tripPlanClient = new TripPlanClient();

    public TripPlanCreateView() {
        setSpacing(true);
        setPadding(true);
        add(new H1("📌 Moje Plany Podróży"));

        String token = extractJwtFromCookie();
        String spotifyId = extractSpotifyIdFromCookie();

        if (spotifyId != null && token != null) {
            try {
                List<TripPlanDto> plans = tripPlanClient.getUserPlans(spotifyId, token);
                for (TripPlanDto plan : plans) {
                    VerticalLayout planBox = new VerticalLayout();
                    planBox.add(new H3(plan.name()));
                    planBox.add(new Paragraph(plan.description()));

                    // Przycisk edycji planu
                    planBox.add(new Button("✏️ Edytuj", ev -> openEditDialog(plan, token)));

                    // Przycisk usuwania planu
                    planBox.add(new Button("🗑️ Usuń plan", ev -> {
                        tripPlanClient.deletePlan(plan.id(), token);
                        getUI().ifPresent(ui -> ui.getPage().reload());
                    }));

                    addDeleteButtons(planBox, plan, token);
                    add(planBox);
                }
            } catch (Exception e) {
                log.error("❌ Błąd pobierania planów: {}", e.getMessage());
            }
        }

        add(new Button("⬅️ Wróć do menu", e -> getUI().ifPresent(ui -> ui.navigate("main-menu"))));
    }

    private String extractSpotifyIdFromCookie() {
        Cookie[] cookies = VaadinService.getCurrentRequest().getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("spotify_id".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private String extractJwtFromCookie() {
        Cookie[] cookies = VaadinService.getCurrentRequest().getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("jwt".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private void openEditDialog(TripPlanDto plan, String token) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("✏️ Edytuj plan");

        TextField nameField = new TextField("Nazwa");
        nameField.setValue(plan.name());
        TextArea descField = new TextArea("Opis");
        descField.setValue(plan.description() != null ? plan.description() : "");

        Button save = new Button("💾 Zapisz", e -> {
            try {
                tripPlanClient.updatePlan(plan.id(), nameField.getValue(), descField.getValue(), token);
                dialog.close();
                getUI().ifPresent(ui -> ui.getPage().reload());
            } catch (Exception ex) {
                Notification.show("❌ Błąd zapisu");
            }
        });

        dialog.add(nameField, descField, save);
        dialog.open();
    }

    private void addDeleteButtons(VerticalLayout planBox, TripPlanDto plan, String token) {
        if (plan.places() != null) {
            for (var place : plan.places()) {
                String display = place.displayName() != null ? place.displayName() : "[Brak nazwy miejsca]";
                Button delPlaceBtn = new Button("🗑️ " + display, ev -> {
                    tripPlanClient.deletePlace(place.id(), token);
                    getUI().ifPresent(ui -> ui.getPage().reload());
                });
                planBox.add(delPlaceBtn);
            }
        }

        if (plan.playlists() != null) {
            for (var playlist : plan.playlists()) {
                String name = playlist.name() != null ? playlist.name() : "[Brak nazwy playlisty]";
                Button delPlBtn = new Button("🗑️ Playlista: " + name, ev -> {
                    tripPlanClient.deletePlaylist(playlist.id(), token);
                    getUI().ifPresent(ui -> ui.getPage().reload());
                });
                planBox.add(delPlBtn);
            }
        }
    }
}
