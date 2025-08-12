package com.milosz.podsiadly.uiservice.vaadin;

import com.milosz.podsiadly.uiservice.dto.TripPlanDto;
import com.milosz.podsiadly.uiservice.security.TokenProvider;
import com.milosz.podsiadly.uiservice.service.TripPlanClient;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.dnd.DragSource;
import com.vaadin.flow.component.dnd.DropEffect;
import com.vaadin.flow.component.dnd.DropTarget;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
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

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Route("my-trips")
public class TripPlanCreateView extends VerticalLayout {

    @Autowired private TokenProvider tokenProvider;

    private final TripPlanClient tripPlanClient = new TripPlanClient();
    private List<TripPlanDto> allPlans = List.of();

    private final Set<Long> editModePlanIds = new HashSet<>();

    private final Map<Long, List<Long>> pendingPlaceOrder = new HashMap<>();

    public TripPlanCreateView() {
        setSpacing(true);
        setPadding(true);
        setWidthFull();

        Button backButton = new Button("⬅ Back to menu", new Icon(VaadinIcon.ARROW_LEFT));
        backButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("main-menu")));

        H1 title = new H1("📌 My Travel Plans");
        title.getStyle().set("text-align", "center").set("width", "100%").set("margin-bottom", "30px");
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
                log.error("❌ Error downloading plans: {}", e.getMessage(), e);
                Notification.show("❌ Failed to load plans", 3000, Notification.Position.MIDDLE);
            }
        }

        search.addValueChangeListener(e ->
                updatePlanList(plansLayout, e.getValue(), sortSelect.getValue(), token));

        sortSelect.addValueChangeListener(e ->
                updatePlanList(plansLayout, search.getValue(), e.getValue(), token));
    }

    private void updatePlanList(VerticalLayout layout, String filter, String sort, String token) {
        layout.removeAll();

        List<TripPlanDto> filtered = allPlans.stream()
                .filter(p -> filter == null || p.name().toLowerCase().contains(filter.toLowerCase()))
                .sorted("A-Z".equals(sort)
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
                    .set("max-width", "900px")
                    .set("margin", "0 auto");
            card.getElement().addEventListener("mouseenter", e -> card.getStyle().set("transform", "scale(1.01)"));
            card.getElement().addEventListener("mouseleave", e -> card.getStyle().set("transform", "scale(1.0)"));

            H3 name = new H3(plan.name());
            name.getStyle().set("text-align", "center");
            Paragraph desc = new Paragraph(plan.description());
            desc.getStyle().set("text-align", "center");

            boolean editing = editModePlanIds.contains(plan.id());
            boolean hasPlaces = plan.places() != null && !plan.places().isEmpty();

            Button editBtn = new Button(
                    editing ? "Save & Exit" : "Edit",
                    new Icon(VaadinIcon.EDIT)
            );
            editBtn.addClickListener(ev -> {
                boolean wasEditing = editModePlanIds.contains(plan.id());

                if (wasEditing && !hasPlaces) {
                    pendingPlaceOrder.remove(plan.id());
                    editModePlanIds.remove(plan.id());
                    Notification.show("Nothing to save", 1200, Notification.Position.BOTTOM_START);
                    updatePlanList(layout, filter, sort, token);
                    return;
                }

                if (wasEditing) {
                    try {
                        List<Long> pending = pendingPlaceOrder.get(plan.id());
                        if (pending == null) {
                            pending = plan.places() == null ? List.of()
                                    : plan.places().stream().map(p -> p.id()).collect(Collectors.toList());
                        }
                        log.info("Saving place order for plan {} -> {}", plan.id(), pending);
                        tripPlanClient.reorderPlaces(plan.id(), pending, token);
                        pendingPlaceOrder.remove(plan.id());
                        Notification.show("✅ Changes saved", 1500, Notification.Position.BOTTOM_START);
                        getUI().ifPresent(ui -> ui.getPage().reload());
                    } catch (Exception ex) {
                        log.error("Failed to save changes on exit", ex);
                        Notification.show("❌ Failed to save changes", 2500, Notification.Position.MIDDLE);
                        updatePlanList(layout, filter, sort, token);
                        return;
                    }
                    editModePlanIds.remove(plan.id());
                } else {
                    editModePlanIds.add(plan.id());
                    if (plan.places() != null) {
                        List<Long> current = plan.places().stream()
                                .map(p -> p.id())
                                .collect(Collectors.toList());
                        pendingPlaceOrder.put(plan.id(), current);
                    }
                }

                updatePlanList(layout, filter, sort, token);
            });

            Button deleteBtn = new Button("Delete plan", new Icon(VaadinIcon.TRASH));
            deleteBtn.addClickListener(ev -> {
                try {
                    tripPlanClient.deletePlan(plan.id(), token);
                    getUI().ifPresent(ui -> ui.getPage().reload());
                } catch (Exception ex) {
                    log.error("Delete failed", ex);
                    Notification.show("❌ Failed to delete plan", 3000, Notification.Position.MIDDLE);
                }
            });

            HorizontalLayout actions = new HorizontalLayout(editBtn, deleteBtn);

            Button renameBtn = new Button("✏️ Change name/description");
            renameBtn.addClickListener(ev -> openEditDialog(plan, token));
            if (editing) {
                actions.add(renameBtn);
            }

            card.add(name, desc, actions);

            if (plan.places() != null && !plan.places().isEmpty()) {
                H4 placesHeader = new H4("📍 Places");
                placesHeader.getStyle().set("margin-top", "20px");
                card.add(placesHeader);

                if (editing) {
                    Span hint = new Span(pendingPlaceOrder.containsKey(plan.id())
                            ? "Drag to reorder, delete to remove. Click “Save & Exit” to persist."
                            : "Drag to reorder, delete to remove.");
                    hint.getStyle()
                            .set("font-size", "var(--lumo-font-size-s)")
                            .set("color", "var(--lumo-secondary-text-color)");
                    card.add(hint);
                }

                VerticalLayout placesLayout = new VerticalLayout();
                placesLayout.setPadding(false);
                placesLayout.setSpacing(true);
                placesLayout.setWidthFull();
                placesLayout.getStyle().set("background", "var(--lumo-contrast-5pct)");
                placesLayout.getStyle().set("border-radius", "8px");
                placesLayout.getStyle().set("padding", "6px");

                if (editing) {
                    DropTarget<VerticalLayout> listDrop = DropTarget.create(placesLayout);
                    listDrop.setDropEffect(DropEffect.MOVE);
                    listDrop.addDropListener(e -> {
                        Object data = e.getDragData().orElse(null);
                        if (!(data instanceof String draggedId)) return;

                        Component draggedRow = placesLayout.getChildren()
                                .filter(c -> draggedId.equals(c.getId().orElse(null)))
                                .findFirst().orElse(null);
                        if (draggedRow == null) return;

                        placesLayout.remove(draggedRow);
                        placesLayout.add(draggedRow);

                        List<Long> orderedIds = placesLayout.getChildren()
                                .map(c -> Long.valueOf(c.getId().orElseThrow()))
                                .collect(Collectors.toList());
                        pendingPlaceOrder.put(plan.id(), orderedIds);

                        Notification.show("Moved to position " + orderedIds.size() + " (pending)",
                                1200, Notification.Position.BOTTOM_START);
                    });
                }

                plan.places().forEach(place -> {
                    String display = place.displayName() != null ? place.displayName() : "[No place name]";

                    HorizontalLayout row = new HorizontalLayout();
                    row.setId(String.valueOf(place.id()));
                    row.setWidthFull();
                    row.setAlignItems(FlexComponent.Alignment.CENTER);
                    row.getStyle()
                            .set("background", "white")
                            .set("border-radius", "8px")
                            .set("padding", "8px 10px")
                            .set("border", "1px solid var(--lumo-contrast-10pct)");

                    Button handle = new Button(new Icon(VaadinIcon.MENU));
                    handle.getElement().getThemeList().add("tertiary");
                    handle.getStyle().set("cursor", "grab");

                    Span label = new Span(display);
                    label.getStyle().set("white-space", "normal");
                    label.getStyle().set("flex", "1");

                    Button del = new Button(new Icon(VaadinIcon.TRASH));
                    del.getElement().getThemeList().add("error tertiary-inline");

                    if (editing) {
                        del.addClickListener(e -> {
                            try {
                                tripPlanClient.deletePlace(place.id(), token);
                                getUI().ifPresent(ui -> ui.getPage().reload());
                            } catch (Exception ex) {
                                log.error("Delete place failed", ex);
                                Notification.show("❌ Failed to delete place", 2500, Notification.Position.MIDDLE);
                            }
                        });

                        DragSource<Button> drag = DragSource.create(handle);
                        drag.setDraggable(true);
                        drag.setDragData(String.valueOf(place.id()));
                        drag.addDragStartListener(e -> handle.getStyle().set("cursor", "grabbing"));
                        drag.addDragEndListener(e -> handle.getStyle().set("cursor", "grab"));

                        DropTarget<HorizontalLayout> drop = DropTarget.create(row);
                        drop.setDropEffect(DropEffect.MOVE);
                        drop.addDropListener(e -> {
                            Object data = e.getDragData().orElse(null);
                            if (!(data instanceof String draggedId)) return;

                            Component draggedRow = placesLayout.getChildren()
                                    .filter(c -> draggedId.equals(c.getId().orElse(null)))
                                    .findFirst().orElse(null);
                            if (draggedRow == null || draggedRow == row) return;

                            int from = indexOf(placesLayout, draggedRow);
                            int to   = indexOf(placesLayout, row);
                            if (from < 0 || to < 0 || from == to) return;

                            placesLayout.remove(draggedRow);
                            placesLayout.addComponentAtIndex(to, draggedRow);

                            List<Long> orderedIds = placesLayout.getChildren()
                                    .map(c -> Long.valueOf(c.getId().orElseThrow()))
                                    .collect(Collectors.toList());
                            pendingPlaceOrder.put(plan.id(), orderedIds);

                            Notification.show("Position: " + (to + 1) + " (pending)",
                                    1200, Notification.Position.BOTTOM_START);
                        });

                    } else {
                        del.setEnabled(false);
                        handle.setEnabled(false);
                        row.addClickListener(e ->
                                Notification.show(display, 1200, Notification.Position.BOTTOM_START));
                    }

                    row.add(handle, label, del);
                    placesLayout.add(row);
                });

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
                    plBtn.getStyle().set("white-space", "normal").set("text-align", "left").set("width", "100%");

                    if (editing) {
                        plBtn.setIcon(new Icon(VaadinIcon.TRASH));
                        plBtn.addClickListener(ev -> {
                            try {
                                tripPlanClient.deletePlaylist(playlist.id(), token);
                                getUI().ifPresent(ui -> ui.getPage().reload());
                            } catch (Exception ex) {
                                log.error("Delete playlist failed", ex);
                                Notification.show("❌ Failed to delete playlist", 3000, Notification.Position.MIDDLE);
                            }
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
                                log.error("❌ Error downloading songs from playlist: {}", ex.getMessage(), ex);
                            }

                            Button close = new Button("⬅ Back to plans", new Icon(VaadinIcon.ARROW_LEFT));
                            close.addClickListener(e -> dialog.close());

                            content.add(close);
                            dialog.add(content);
                            dialog.setWidth("600px");
                            dialog.open();
                        });
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
        nameField.setValue(plan.name() != null ? plan.name() : "");
        nameField.setRequired(true);
        nameField.setMaxLength(120);
        nameField.setClearButtonVisible(true);

        TextArea descField = new TextArea("Description");
        descField.setValue(plan.description() != null ? plan.description() : "");
        descField.setMaxLength(1000);

        Paragraph hint = new Paragraph("Name must be unique and not empty.");
        hint.getStyle().set("font-size", "var(--lumo-font-size-s)").set("color", "var(--lumo-secondary-text-color)");

        Button save = new Button("💾 Save", e -> {
            String newName = nameField.getValue() != null ? nameField.getValue().trim() : "";
            String newDesc = descField.getValue() != null ? descField.getValue().trim() : "";

            if (newName.isBlank()) {
                Notification.show("⚠️ Name cannot be empty.", 2500, Notification.Position.MIDDLE);
                return;
            }
            boolean duplicate = allPlans.stream()
                    .anyMatch(p -> !Objects.equals(p.id(), plan.id())
                            && p.name() != null
                            && p.name().trim().equalsIgnoreCase(newName));
            if (duplicate) {
                Notification.show("⚠️ A plan with this name already exists.", 2500, Notification.Position.MIDDLE);
                return;
            }

            try {
                tripPlanClient.updatePlan(plan.id(), newName, newDesc, token);
                dialog.close();
                getUI().ifPresent(ui -> ui.getPage().reload());
            } catch (Exception ex) {
                log.error("Save failed", ex);
                Notification.show("❌ Error saving plan", 3000, Notification.Position.MIDDLE);
            }
        });

        Button cancel = new Button("Cancel", e -> dialog.close());
        HorizontalLayout actions = new HorizontalLayout(save, cancel);

        dialog.add(new VerticalLayout(nameField, descField, hint, actions));
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

    private static int indexOf(VerticalLayout layout, Component child) {
        int idx = 0;
        for (Component c : layout.getChildren().toList()) {
            if (c.equals(child)) return idx;
            idx++;
        }
        return -1;
    }
}