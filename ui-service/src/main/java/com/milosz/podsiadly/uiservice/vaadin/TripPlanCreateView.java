package com.milosz.podsiadly.uiservice.vaadin;

import com.milosz.podsiadly.uiservice.dto.TripPlanDto;
import com.milosz.podsiadly.uiservice.security.TokenProvider;
import com.milosz.podsiadly.uiservice.service.TripPlanClient;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinService;
import jakarta.servlet.http.Cookie;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Route("my-trips")
public class TripPlanCreateView extends VerticalLayout {

    @Autowired
    private TokenProvider tokenProvider;
    private final TripPlanClient tripPlanClient = new TripPlanClient();
    private List<TripPlanDto> allPlans = List.of();
    private final Set<Long> editModePlanIds = new HashSet<>();

    public TripPlanCreateView() {
        setSpacing(true);
        setPadding(true);
        setWidthFull();

        Button backButton = new Button("⬅ Back to menu", new Icon(VaadinIcon.ARROW_LEFT));
        backButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("main-menu")));

        H1 title = new H1("📌 My Travel Plans");
        title.getStyle()
                .set("text-align", "center")
                .set("width", "100%")
                .set("margin-bottom", "30px");
        add(title);

        TextField search = new TextField();
        search.setPlaceholder("Search plan...");
        search.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        search.setWidth("300px");

        Select<String> sortSelect = new Select<>();
        sortSelect.setItems("A-Z", "Z-A");
        sortSelect.setValue("A-Z");
        sortSelect.setWidth("100px");

        HorizontalLayout filters = new HorizontalLayout();
        filters.setWidthFull();
        filters.setJustifyContentMode(JustifyContentMode.CENTER);
        filters.setAlignItems(Alignment.CENTER);
        filters.setSpacing(true);
        filters.getStyle().set("margin-bottom", "30px");

        filters.add(backButton, search, sortSelect);
        add(filters);

        VerticalLayout plansLayout = new VerticalLayout();
        plansLayout.setWidthFull();
        add(plansLayout);

        String token = extractJwtFromCookie();
        String spotifyId = extractSpotifyIdFromCookie();

        if (spotifyId != null && token != null) {
            try {
                allPlans = tripPlanClient.getUserPlans(spotifyId, token);
                updatePlanList(plansLayout, search.getValue(), sortSelect.getValue(), token);
            } catch (Exception e) {
                log.error("❌ Error downloading plans: {}", e.getMessage());
            }
        }

        search.addValueChangeListener(e ->
                updatePlanList(plansLayout, e.getValue(), sortSelect.getValue(), token));

        sortSelect.addValueChangeListener(e ->
                updatePlanList(plansLayout, search.getValue(), e.getValue(), token));

        Button backBottom = new Button("⬅ Back to menu", new Icon(VaadinIcon.ARROW_LEFT));
        backBottom.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("main-menu")));
        backBottom.getStyle().set("margin-top", "30px");
        add(backBottom);
    }

    private void updatePlanList(VerticalLayout layout, String filter, String sort, String token) {
        layout.removeAll();

        List<TripPlanDto> filtered = allPlans.stream()
                .filter(p -> filter == null || p.name().toLowerCase().contains(filter.toLowerCase()))
                .sorted(sort.equals("A-Z")
                        ? Comparator.comparing(TripPlanDto::name)
                        : Comparator.comparing(TripPlanDto::name).reversed())
                .collect(Collectors.toList());

        for (TripPlanDto plan : filtered) {

            VerticalLayout card = new VerticalLayout();
            card.setWidth("100%");
            card.getStyle()
                    .set("border", "1px solid #ccc")
                    .set("border-radius", "12px")
                    .set("padding", "20px")
                    .set("box-shadow", "0 2px 8px rgba(0, 0, 0, 0.05)")
                    .set("margin-bottom", "25px")
                    .set("background-color", "#ffffff")
                    .set("transition", "transform 0.2s ease")
                    .set("cursor", "pointer")
                    .set("max-width", "800px")
                    .set("margin", "0 auto");

            card.getElement().addEventListener("mouseenter", e -> card.getStyle().set("transform", "scale(1.01)"));
            card.getElement().addEventListener("mouseleave", e -> card.getStyle().set("transform", "scale(1.0)"));

            H3 name = new H3(plan.name());
            name.getStyle().set("text-align", "center");
            Paragraph desc = new Paragraph(plan.description());
            desc.getStyle().set("text-align", "center");

            Button editBtn = new Button("Edytuj", new Icon(VaadinIcon.EDIT));
            editBtn.addClickListener(ev -> {
                if (editModePlanIds.contains(plan.id())) {
                    editModePlanIds.remove(plan.id());
                } else {
                    editModePlanIds.add(plan.id());
                }
                updatePlanList(layout, filter, sort, token);
            });

            Button deleteBtn = new Button("Delete plan", new Icon(VaadinIcon.TRASH));
            deleteBtn.addClickListener(ev -> {
                tripPlanClient.deletePlan(plan.id(), token);
                getUI().ifPresent(ui -> ui.getPage().reload());
            });

            HorizontalLayout actions = new HorizontalLayout(editBtn, deleteBtn);
            Button renameBtn = new Button("✏️ Change name and description");
            if (editModePlanIds.contains(plan.id())) {
                renameBtn.addClickListener(ev -> openEditDialog(plan, token));
                actions.add(renameBtn);
            }
            card.add(name, desc, actions);

            if (plan.places() != null && !plan.places().isEmpty()) {
                H4 placesHeader = new H4("📍 Places");
                placesHeader.getStyle().set("margin-top", "20px");
                card.add(placesHeader);

                VerticalLayout placesLayout = new VerticalLayout();
                placesLayout.setPadding(false);
                placesLayout.setSpacing(true);
                placesLayout.setWidthFull();

                for (var place : plan.places()) {
                    String display = place.displayName() != null ? place.displayName() : "[No place name]";
                    Button placeBtn = new Button(display);
                    placeBtn.getStyle()
                            .set("white-space", "normal")
                            .set("text-align", "left")
                            .set("width", "100%");

                    if (editModePlanIds.contains(plan.id())) {
                        placeBtn.setIcon(new Icon(VaadinIcon.TRASH));
                        placeBtn.addClickListener(ev -> {
                            tripPlanClient.deletePlace(place.id(), token);
                            getUI().ifPresent(ui -> ui.getPage().reload());
                        });
                    }
                    placesLayout.add(placeBtn);

                }
                card.add(placesLayout);
            }

            if (plan.playlists() != null && !plan.playlists().isEmpty()) {
                H4 playlistsHeader = new H4("🎵 Playlists");
                playlistsHeader.getStyle().set("margin-top", "20px");
                card.add(playlistsHeader);

                VerticalLayout playlistsLayout = new VerticalLayout();
                playlistsLayout.setPadding(false);
                playlistsLayout.setSpacing(true);
                playlistsLayout.setWidthFull();

                for (var playlist : plan.playlists()) {
                    String namePl = playlist.name() != null ? playlist.name() : "[No playlist name]";
                    Button plBtn = new Button(namePl);
                    plBtn.getStyle()
                            .set("white-space", "normal")
                            .set("text-align", "left")
                            .set("width", "100%");

                    if (editModePlanIds.contains(plan.id())) {
                        plBtn.setIcon(new Icon(VaadinIcon.TRASH));
                        plBtn.addClickListener(ev -> {
                            tripPlanClient.deletePlaylist(playlist.id(), token);
                            getUI().ifPresent(ui -> ui.getPage().reload());
                        });
                    } else {
                        plBtn.addClickListener(ev -> {
                            Dialog dialog = new Dialog();
                            dialog.setHeaderTitle("🎵 " + namePl);

                            VerticalLayout content = new VerticalLayout();
                            content.setPadding(true);
                            content.setSpacing(true);

                            try {
                                String spotifyPlaylistId = playlist.playlistId();
                                log.warn("🎧 Download songs from Spotify ID playlist: {}", spotifyPlaylistId);

                                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                                String spotifyAccessToken = tokenProvider.getAccessToken(auth);
                                var songs = tripPlanClient.getPlaylistTracks(spotifyPlaylistId, spotifyAccessToken);

                                if (songs == null || songs.isEmpty()) {
                                    content.add(new Paragraph("There are no songs in this playlist."));
                                } else {
                                    UnorderedList songList = new UnorderedList();
                                    songs.forEach(song -> {
                                        String line = String.join(", ", song.artists()) + " – " + song.name();
                                        songList.add(new ListItem(line));
                                    });
                                    content.add(songList);
                                }
                            } catch (Exception ex) {
                                content.add(new Paragraph("❌ Error loading songs."));
                                log.error("❌ Error downloading songs from playlist: {}", ex.getMessage());
                            }

                            Button close = new Button("⬅ Back to plans", new Icon(VaadinIcon.ARROW_LEFT));
                            close.addClickListener(e -> dialog.close());

                            content.add(close);
                            dialog.add(content);
                            dialog.setWidth("600px");
                            dialog.open();
                        });
                    }
                    if (editModePlanIds.contains(plan.id())) {
                        renameBtn.addClickListener(ev -> openEditDialog(plan, token));
                        actions.add(renameBtn);
                    }
                    playlistsLayout.add(plBtn);
                }
                card.add(playlistsLayout);
            }
            layout.add(card);
        }
    }


    private void openEditDialog(TripPlanDto plan, String token) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("✏️ Edit plan");

        TextField nameField = new TextField("Name");
        nameField.setValue(plan.name());
        TextArea descField = new TextArea("Description");
        descField.setValue(plan.description() != null ? plan.description() : "");

        Button save = new Button("💾 Save", e -> {
            try {
                tripPlanClient.updatePlan(plan.id(), nameField.getValue(), descField.getValue(), token);
                dialog.close();
                getUI().ifPresent(ui -> ui.getPage().reload());
            } catch (Exception ex) {
                Notification.show("❌ Error save");
            }
        });

        dialog.add(nameField, descField, save);
        dialog.open();
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
}