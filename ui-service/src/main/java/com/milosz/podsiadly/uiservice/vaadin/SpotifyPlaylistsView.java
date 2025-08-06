package com.milosz.podsiadly.uiservice.vaadin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.milosz.podsiadly.uiservice.component.TripPlanSelectionDialog;
import com.milosz.podsiadly.uiservice.dto.SpotifyPlaylistDTO;
import com.milosz.podsiadly.uiservice.security.SpotifyTokenCache;
import com.milosz.podsiadly.uiservice.service.TripPlanClient;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Route("playlists")
@PermitAll
public class SpotifyPlaylistsView extends VerticalLayout implements BeforeEnterObserver {

    private final RestTemplate restTemplate = new RestTemplate();
    private final SpotifyTokenCache spotifyTokenCache;
    private final String musicServiceBaseUrl = "https://api.spotify.com/v1/me";
    private final String musicServiceBaseUrl2 = "https://api.spotify.com/v1";

    private boolean playlistsLoaded = false;

    public SpotifyPlaylistsView(SpotifyTokenCache spotifyTokenCache) {
        this.spotifyTokenCache = spotifyTokenCache;

        setSpacing(true);
        setPadding(true);
        add(new H1("🎧 Your Spotify Playlists"));

        add(new Button("⬅️ Back to menu", e -> getUI().ifPresent(ui -> ui.navigate("main-menu"))));
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!playlistsLoaded) {
            loadPlaylists();
        }
    }

    private void loadPlaylists() {
        String token = spotifyTokenCache.getAccessToken();

        if (token == null || token.isBlank()) {
            Notification.show("❌ No token - please log in again.");
            getUI().ifPresent(ui -> ui.getPage().setLocation("/oauth2/authorization/spotify"));
            return;
        }

        List<SpotifyPlaylistDTO> playlists = fetchPlaylists(token);
        if (playlists == null) return;

        playlistsLoaded = true;
        removeAll();
        add(new H1("🎧 Your Spotify Playlists"));

        playlists.forEach(playlist -> {
            Button playlistButton = new Button("▶️ " + playlist.name());
            playlistButton.addClickListener(e -> showTracks(token, playlist.id()));
            add(playlistButton);

            Button addToPlanButton = new Button("➕ Add to trip plan");
            addToPlanButton.addClickListener(e -> {
                new TripPlanSelectionDialog(
                        getSpotifyId(token), token, new TripPlanClient(), selectedPlan -> {
                    try {
                        new TripPlanClient().addPlaylist(selectedPlan.id(), playlist.id(), playlist.name(), token);
                        Notification.show("🎉 Playlist added to plan: " + selectedPlan.name());
                    } catch (Exception ex) {
                        Notification.show("❌ Failed to add playlist.");
                    }
                }).open();
            });

            add(addToPlanButton);
        });

        add(new Button("⬅️ Back to menu", e -> getUI().ifPresent(ui -> ui.navigate("main-menu"))));
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
                log.warn("⚠️ Missing data in response or incorrect format.");
                return List.of();
            }

            ObjectMapper mapper = new ObjectMapper();
            SpotifyPlaylistDTO[] playlists = mapper.treeToValue(items, SpotifyPlaylistDTO[].class);

            return Arrays.asList(playlists);

        } catch (Exception ex) {
            log.error("❌ Error downloading playlists: {}", ex.getMessage(), ex);
            Notification.show("❌ Failed to download playlists.", 3000, Notification.Position.MIDDLE);
            return null;
        }
    }

    private void showTracks(String token, String playlistId) {
        removeAll();
        add(new H1("🎵 Songs in the playlist"));

        try {
            HttpHeaders headers = buildHeadersWithToken(token);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

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

                    String name = trackNode.path("name").asText("Unknown song");

                    List<String> artistNames = new ArrayList<>();
                    for (JsonNode artist : trackNode.path("artists")) {
                        artistNames.add(artist.path("name").asText());
                    }

                    String artists = artistNames.isEmpty() ? "Unknown artist" : String.join(", ", artistNames);

                    add(new Paragraph("• " + name + " – " + artists));
                }
            } else {
                Notification.show("❗ No songs to display.", 3000, Notification.Position.MIDDLE);
            }

        } catch (Exception ex) {
            log.error("❌ Error while downloading songs: {}", ex.getMessage(), ex);
            Notification.show("❌ Failed to load songs.", 3000, Notification.Position.MIDDLE);
        }

        add(new Button("⬅️ Back to playlists", e -> {
            playlistsLoaded = false; // Ensure reload
            getUI().ifPresent(ui -> ui.navigate("playlists"));
        }));
    }

    private HttpHeaders buildHeadersWithToken(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "travel-app");
        headers.setBearerAuth(token);
        return headers;
    }

    private String getSpotifyId(String token) {
        try {
            if (token == null || token.isBlank()) {
                log.warn("⚠️ No Spotify Token - Cannot Get ID.");
                Notification.show("❌ No valid Spotify token.");
                return null;
            }

            log.info("🔐 Using token: {}", token.substring(0, Math.min(token.length(), 10)) + "...");

            HttpHeaders headers = buildHeadersWithToken(token);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    "https://api.spotify.com/v1/me",
                    HttpMethod.GET,
                    entity,
                    JsonNode.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode body = response.getBody();
                log.info("✅ Spotify ID: {}", body.get("id").asText());
                return body.get("id").asText();
            } else {
                log.warn("❌ Spotify /me API error: {}", response.getStatusCode());
                Notification.show("❌ Error connecting to Spotify: " + response.getStatusCode());
                return null;
            }

        } catch (Exception ex) {
            log.error("❌ Exception when calling Spotify /me: {}", ex.getMessage(), ex);
            Notification.show("❌ Failed to get Spotify user ID.");
            return null;
        }
    }
}
