package com.milosz.podsiadly.uiservice.vaadin;

import com.fasterxml.jackson.databind.JsonNode;
import com.milosz.podsiadly.uiservice.component.TripPlanSelectionDialog;
import com.milosz.podsiadly.uiservice.dto.LocationDto;
import com.milosz.podsiadly.uiservice.security.SpotifyTokenCache;
import com.milosz.podsiadly.uiservice.service.TripPlanClient;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinService;
import jakarta.servlet.http.Cookie;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Route("plan-route")
public class RoutePlannerView extends VerticalLayout {

    private final RestTemplate restTemplate = new RestTemplate();
    private final TripPlanClient tripPlanClient;
    private final SpotifyTokenCache spotifyTokenCache;

    public RoutePlannerView(TripPlanClient tripPlanClient, SpotifyTokenCache spotifyTokenCache) {
        this.tripPlanClient = tripPlanClient;
        this.spotifyTokenCache = spotifyTokenCache;

        setSpacing(true);
        setPadding(true);
        buildUI();
    }

    private void buildUI() {
        add(new H1("📍 Planowanie trasy"));

        TextField cityInput = new TextField("Miasto");
        cityInput.setPlaceholder("np. Warsaw, Berlin");
        cityInput.setWidth("300px");

        Select<String> categorySelect = new Select<>();
        categorySelect.setLabel("Typ miejsca");
        categorySelect.setItems("museum", "monument", "park", "bakery", "restaurant", "cafe");
        categorySelect.setPlaceholder("Wybierz kategorię");

        Button cityInfoButton = new Button("🌍 Pokaż lokalizację miasta");
        Button categorySearchButton = new Button("🏙️ Szukaj wg kategorii");

        Paragraph result = new Paragraph();

        cityInfoButton.addClickListener(e -> {
            String city = cityInput.getValue();
            if (isValidQuery(city)) {
                fetchLocations(city, 1, result, false);
            } else {
                showWarning("⚠️ Wprowadź nazwę miasta.");
            }
        });

        categorySearchButton.addClickListener(e -> {
            String city = cityInput.getValue();
            String category = categorySelect.getValue();

            if (isValidQuery(city) && isValidQuery(category)) {
                fetchLocations(category + " in " + city, 5, result, true);
            } else {
                showWarning("⚠️ Wprowadź nazwę miasta i wybierz kategorię.");
            }
        });

        add(cityInput, categorySelect, cityInfoButton, categorySearchButton, result);
        add(new Button("⬅️ Wróć do menu", e -> getUI().ifPresent(ui -> ui.navigate("main-menu"))));
    }

    private void fetchLocations(String query, int limit, Paragraph result, boolean allowAddToPlan) {
        try {
            String url = "https://nominatim.openstreetmap.org/search?format=json&q="
                    + URLEncoder.encode(query, StandardCharsets.UTF_8) + "&limit=" + limit;

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "travel-app");

            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<LocationDto[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, LocationDto[].class);

            LocationDto[] locations = response.getBody();

            if (response.getStatusCode().is2xxSuccessful() && locations != null && locations.length > 0) {
                removeAll();
                add(new H1("📍 Wyniki"));

                for (LocationDto loc : locations) {
                    Paragraph entry = new Paragraph("• " + loc.displayName() + " (" + loc.latitude() + ", " + loc.longitude() + ")");
                    add(entry);

                    if (allowAddToPlan) {
                        add(createAddButton(loc));
                    }
                }
            } else {
                result.setText("");
                showWarning("❗ Brak wyników dla zapytania.");
            }

        } catch (Exception ex) {
            log.error("❌ Błąd podczas zapytania do Nominatim: {}", ex.getMessage(), ex);
            showWarning("❌ Błąd połączenia z serwerem Nominatim.");
        }
    }

    private Button createAddButton(LocationDto location) {
        Button addButton = new Button("➕ Dodaj do planu podróży");

        addButton.addClickListener(e -> {
            try {
                String spotifyId = fetchSpotifyId();
                if (spotifyId == null) return;

                String jwt = extractTokenFromCookie();
                if (jwt == null) {
                    showWarning("❌ Brak tokena JWT do autoryzacji.");
                    return;
                }

                new TripPlanSelectionDialog(
                        spotifyId, jwt, tripPlanClient, selectedPlan -> {
                    try {
                        tripPlanClient.addPlace(
                                selectedPlan.id(),
                                location.displayName(),
                                Double.parseDouble(location.latitude()),
                                Double.parseDouble(location.longitude()),
                                jwt
                        );
                        Notification.show("✅ Dodano do planu: " + selectedPlan.name());
                    } catch (Exception ex) {
                        log.error("❌ Błąd dodania miejsca: {}", ex.getMessage(), ex);
                        showWarning("❌ Nie udało się dodać miejsca");
                    }
                }).open();

            } catch (Exception ex) {
                log.error("❌ Błąd podczas pobierania danych: {}", ex.getMessage(), ex);
                showWarning("❌ Wystąpił błąd.");
            }
        });

        return addButton;
    }

    private String fetchSpotifyId() {
        try {
            String accessToken = spotifyTokenCache.getAccessToken();

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.set("User-Agent", "travel-app");

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    "https://api.spotify.com/v1/me",
                    HttpMethod.GET,
                    entity,
                    JsonNode.class
            );

            return response.getBody().get("id").asText();

        } catch (Exception e) {
            log.error("❌ Nie udało się pobrać Spotify ID: {}", e.getMessage(), e);
            showWarning("❌ Błąd pobierania ID Spotify");
            return null;
        }
    }

    private String extractTokenFromCookie() {
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

    private boolean isValidQuery(String q) {
        return q != null && !q.trim().isEmpty();
    }

    private void showWarning(String message) {
        Notification.show(message, 3000, Notification.Position.MIDDLE);
    }
}
