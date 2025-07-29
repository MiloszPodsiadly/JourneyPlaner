package com.milosz.podsiadly.uiservice.vaadin;

import com.milosz.podsiadly.uiservice.dto.LocationDto;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinService;
import jakarta.servlet.http.Cookie;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import lombok.extern.slf4j.Slf4j;
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

        TextField cityInput = new TextField("Miasto / Miejsce");
        cityInput.setPlaceholder("np. Warsaw, Berlin, Gdańsk");
        cityInput.setWidth("300px");

        Button searchButton = new Button("🔍 Szukaj");
        Paragraph result = new Paragraph();

        searchButton.addClickListener(e -> {
            String query = cityInput.getValue();
            if (query != null && !query.trim().isEmpty()) {
                try {
                    String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
                    String url = "https://nominatim.openstreetmap.org/search?format=json&q=" + encodedQuery;

                    // ✅ Pobierz token JWT z ciasteczka
                    String token = extractTokenFromCookie();
                    log.info("Token JWT: {}", token);

                    if (token == null || token.isBlank()) {
                        Notification.show("🔒 Brak tokena JWT. Zaloguj się ponownie.", 3000, Notification.Position.MIDDLE);
                        return;
                    }

                    HttpHeaders headers = new HttpHeaders();
                    headers.setBearerAuth(token);
                    HttpEntity<Void> entity = new HttpEntity<>(headers);

                    ResponseEntity<LocationDto[]> response =
                            restTemplate.exchange(url, HttpMethod.GET, entity, LocationDto[].class);

                    LocationDto[] locations = response.getBody();

                    if (response.getStatusCode().is2xxSuccessful() && locations != null && locations.length > 0) {
                        LocationDto location = locations[0];
                        result.setText("📌 " + location.displayName() +
                                " (" + location.latitude() + ", " + location.longitude() + ")");
                    } else {
                        result.setText("");
                        Notification.show("❗ Nie znaleziono lokalizacji.", 3000, Notification.Position.MIDDLE);
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                    result.setText("");
                    Notification.show("❌ Błąd podczas zapytania do serwera.", 3000, Notification.Position.MIDDLE);
                }
            } else {
                result.setText("");
                Notification.show("⚠️ Wprowadź nazwę miasta lub miejsca.", 3000, Notification.Position.MIDDLE);
            }
        });

        add(cityInput, searchButton, result);
        add(new Button("⬅️ Wróć do menu", e -> getUI().ifPresent(ui -> ui.navigate("main-menu"))));
    }

    /**
     * Pobiera JWT z ciasteczka o nazwie "jwt"
     */
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
