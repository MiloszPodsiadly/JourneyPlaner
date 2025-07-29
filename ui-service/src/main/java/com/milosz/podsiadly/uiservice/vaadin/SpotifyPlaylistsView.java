package com.milosz.podsiadly.uiservice.vaadin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.milosz.podsiadly.uiservice.dto.SpotifyPlaylistDTO;
import com.milosz.podsiadly.uiservice.dto.SpotifyTrackDTO;
import com.milosz.podsiadly.uiservice.security.TokenProvider;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinService;
import jakarta.annotation.security.PermitAll;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@Route("playlists")
@PermitAll
public class SpotifyPlaylistsView extends VerticalLayout {

    private final RestTemplate restTemplate = new RestTemplate();
    private final TokenProvider tokenProvider;
    private final String musicServiceBaseUrl = "https://api.spotify.com/v1/me";

    private final String musicServiceBaseUrl2 = "https://api.spotify.com/v1";

    public SpotifyPlaylistsView(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;

        setSpacing(true);
        setPadding(true);
        add(new H1("🎧 Twoje Playlisty Spotify"));

        Button fetchButton = new Button("🔄 Pobierz Playlisty");
        add(fetchButton);

        fetchButton.addClickListener(event -> {
            Authentication authentication = getAuthentication();

            if (authentication == null) {
                Notification.show("❌ Nie jesteś zalogowany.", 3000, Notification.Position.MIDDLE);
                return;
            }

            String token = tokenProvider.getAccessToken(authentication);
            if (token == null || token.isBlank()) {
                log.warn("⚠️ Brak tokenu – przekierowanie do logowania Spotify.");
                getUI().ifPresent(ui -> ui.getPage().setLocation("/oauth2/authorization/spotify"));
                return;
            }


            List<SpotifyPlaylistDTO> playlists = fetchPlaylists(token);
            if (playlists == null) return;

            remove(fetchButton); // Ukryj przycisk po załadowaniu
            playlists.forEach(playlist -> {
                Button playlistButton = new Button("▶️ " + playlist.name());
                playlistButton.addClickListener(e -> showTracks(token, playlist.id()));
                add(playlistButton);
            });
        });

        add(new Button("⬅️ Wróć do menu", e -> getUI().ifPresent(ui -> ui.navigate("main-menu"))));
    }

    private List<SpotifyPlaylistDTO> fetchPlaylists(String token) {
        try {
            HttpHeaders headers = buildHeadersWithToken(token);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    musicServiceBaseUrl + "/playlists",
                    HttpMethod.GET,
                    entity,
                    JsonNode.class
            );

            JsonNode body = response.getBody();
            JsonNode items = body != null ? body.get("items") : null;

            if (items == null || !items.isArray()) {
                log.warn("⚠️ Brak danych w odpowiedzi lub nieprawidłowy format.");
                return List.of();
            }

            ObjectMapper mapper = new ObjectMapper();
            SpotifyPlaylistDTO[] playlists = mapper.treeToValue(items, SpotifyPlaylistDTO[].class);

            return Arrays.asList(playlists);

        } catch (Exception ex) {
            log.error("❌ Błąd podczas pobierania playlist: {}", ex.getMessage(), ex);
            Notification.show("❌ Nie udało się pobrać playlist.", 3000, Notification.Position.MIDDLE);
            return null;
        }
    }

    private void showTracks(String token, String playlistId) {
        removeAll();
        add(new H1("🎵 Utwory w playliście"));

        try {
            // Build Authorization Header
            HttpHeaders headers = buildHeadersWithToken(token);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            // Make request to your music service
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    musicServiceBaseUrl2 + "/playlists/" + playlistId + "/tracks",
                    HttpMethod.GET,
                    entity,
                    JsonNode.class
            );

            JsonNode body = response.getBody();
            JsonNode items = body != null ? body.path("items") : null;

            if (response.getStatusCode().is2xxSuccessful() && items != null && items.isArray() && items.size() > 0) {
                for (JsonNode item : items) {
                    JsonNode trackNode = item.path("track");
                    if (trackNode.isMissingNode() || trackNode.isNull()) continue;

                    String name = trackNode.path("name").asText("Nieznany utwór");

                    List<String> artistNames = new ArrayList<>();
                    for (JsonNode artist : trackNode.path("artists")) {
                        artistNames.add(artist.path("name").asText());
                    }

                    String artists = artistNames.isEmpty() ? "Nieznany wykonawca" : String.join(", ", artistNames);

                    add(new Paragraph("• " + name + " – " + artists));
                }
            } else {
                Notification.show("❗ Brak utworów do wyświetlenia.", 3000, Notification.Position.MIDDLE);
            }

        } catch (Exception ex) {
            log.error("❌ Błąd podczas pobierania utworów: {}", ex.getMessage(), ex);
            Notification.show("❌ Nie udało się załadować utworów.", 3000, Notification.Position.MIDDLE);
        }

        // Back Button
        add(new Button("⬅️ Powrót do playlist", e -> getUI().ifPresent(ui -> ui.navigate("playlists"))));
    }

    private HttpHeaders buildHeadersWithToken(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "travel-app");
        headers.setBearerAuth(token);
        return headers;
    }

    private Authentication getAuthentication() {
        return VaadinService.getCurrentRequest().getUserPrincipal() instanceof Authentication
                ? (Authentication) VaadinService.getCurrentRequest().getUserPrincipal()
                : null;
    }
}
