package com.milosz.podsiadly.uiservice.vaadin;

import com.milosz.podsiadly.uiservice.dto.LocationDto;
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
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@Route("plan-route")
public class RoutePlannerView extends VerticalLayout {

    private final RestTemplate restTemplate = new RestTemplate();

    public RoutePlannerView() {
        setSpacing(true);
        setPadding(true);
        add(new H1("📍 Planowanie trasy"));

        TextField cityInput = new TextField("Miasto");
        cityInput.setPlaceholder("np. Warsaw, Berlin");
        cityInput.setWidth("300px");

        Select<String> categorySelect = new Select<>();
        categorySelect.setLabel("Typ miejsca");
        categorySelect.setItems("museum", "monument", "park", "bakery", "restaurant", "cafe");
        categorySelect.setPlaceholder("Wybierz kategorię");

        Button cityInfoButton = new Button("🌍 Pokaż lokalizację miasta");
        Button categorySearchButton = new Button("🌆 Szukaj wg kategorii");

        Paragraph result = new Paragraph();

        // 🌍 Pokaż lokalizację miasta
        cityInfoButton.addClickListener(e -> {
            String query = cityInput.getValue();
            if (isValidQuery(query)) {
                fetchFromNominatim(query, 1, result, false);
            } else {
                result.setText("");
                Notification.show("⚠️ Wprowadź nazwę miasta.", 3000, Notification.Position.MIDDLE);
            }
        });

        // 🌆 Pokaż top 5 wg kategorii
        categorySearchButton.addClickListener(e -> {
            String city = cityInput.getValue();
            String category = categorySelect.getValue();

            if (isValidQuery(city) && isValidQuery(category)) {
                String query = category + " in " + city;
                fetchFromNominatim(query, 5, result, true);
            } else {
                Notification.show("⚠️ Wprowadź nazwę miasta i wybierz kategorię.", 3000, Notification.Position.MIDDLE);
            }
        });

        add(cityInput, categorySelect, cityInfoButton, categorySearchButton, result);
        add(new Button("⬅️ Wróć do menu", e -> getUI().ifPresent(ui -> ui.navigate("main-menu"))));
    }

    private void fetchFromNominatim(String query, int limit, Paragraph result, boolean multiple) {
        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String url = "https://nominatim.openstreetmap.org/search?format=json&q=" + encodedQuery + "&limit=" + limit;

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "travel-app");

            String token = extractTokenFromCookie();
            if (token != null && !token.isBlank()) {
                headers.setBearerAuth(token);
            } else {
                log.warn("⚠️ Brak tokena JWT – żądanie bez autoryzacji.");
            }

            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<LocationDto[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, LocationDto[].class);
            LocationDto[] locations = response.getBody();

            if (response.getStatusCode().is2xxSuccessful() && locations != null && locations.length > 0) {
                if (multiple) {
                    StringBuilder sb = new StringBuilder("📍 Top 5 wyników:\n");
                    for (LocationDto loc : locations) {
                        sb.append("• ").append(loc.displayName())
                                .append(" (").append(loc.latitude()).append(", ").append(loc.longitude()).append(")\n");
                    }
                    result.setText(sb.toString());
                } else {
                    LocationDto loc = locations[0];
                    result.setText("📌 " + loc.displayName() +
                            " (" + loc.latitude() + ", " + loc.longitude() + ")");
                }
            } else {
                result.setText("");
                Notification.show("❗ Brak wyników dla zapytania.", 3000, Notification.Position.MIDDLE);
            }

        } catch (Exception ex) {
            log.error("❌ Błąd podczas zapytania do Nominatim: {}", ex.getMessage(), ex);
            Notification.show("❌ Błąd połączenia z serwerem Nominatim.", 3000, Notification.Position.MIDDLE);
        }
    }

    private boolean isValidQuery(String q) {
        return q != null && !q.trim().isEmpty();
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
}
