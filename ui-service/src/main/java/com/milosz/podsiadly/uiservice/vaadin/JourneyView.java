package com.milosz.podsiadly.uiservice.vaadin;

import com.milosz.podsiadly.uiservice.dto.RouteResponse;
import com.milosz.podsiadly.uiservice.dto.TripPlaceDto;
import com.milosz.podsiadly.uiservice.dto.TripPlanDto;
import com.milosz.podsiadly.uiservice.service.JourneyClient;
import com.milosz.podsiadly.uiservice.service.TripPlanClient;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinService;

import jakarta.servlet.http.Cookie;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Route("journey")
public class JourneyView extends VerticalLayout {

    private final TripPlanClient tripPlanClient;
    private final JourneyClient journeyClient;

    private static final double SPEED_WALK_KMPH  = 4.0;
    private static final double SPEED_CYCLE_KMPH = 20.0;

    private Double lastDistanceMeters = null;
    private Long   lastDrivingSeconds = null;
    private boolean lastWasOptimized  = false;

    private final Grid<ModeRow> modesGrid = new Grid<>(ModeRow.class, false);

    public JourneyView(TripPlanClient tripPlanClient, JourneyClient journeyClient) {
        this.tripPlanClient = tripPlanClient;
        this.journeyClient = journeyClient;
        buildUI();
    }

    private void buildUI() {
        setSpacing(true);
        setPadding(true);
        setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);
        getStyle().set("max-width", "980px");
        getStyle().set("margin", "0 auto");

        H1 title = new H1("🚗 Create Journey Plan");
        title.getStyle().set("text-align", "center");
        title.setWidthFull();
        add(title);

        Button backBtn = new Button("⬅️ Back to menu", e -> getUI().ifPresent(ui -> ui.navigate("main-menu")));

        Select<TripPlanDto> planSelect = new Select<>();
        planSelect.setLabel("Select Trip Plan");
        planSelect.setItemLabelGenerator(TripPlanDto::name);
        planSelect.setWidth("320px");

        Select<TransportMode> modeSelect = new Select<>();
        modeSelect.setLabel("Transport");
        modeSelect.setItems(TransportMode.values());
        modeSelect.setValue(TransportMode.DRIVING);
        modeSelect.setWidth("180px");

        Button loadPlansBtn = new Button("📂 Load My Trip Plans", e -> loadPlans(planSelect));

        Button keepOrderBtn = new Button("🧭 Create Route (keep order)", e -> createRoute(planSelect, false));
        Button optimizeBtn  = new Button("✨ Optimize Route (TSP)", e -> createRoute(planSelect, true));
        keepOrderBtn.getElement().getThemeList().add("primary");
        optimizeBtn.getElement().getThemeList().add("success");

        Button calcModeBtn = new Button("📏 Calculate (selected mode)",
                e -> calculateByMode(planSelect, modeSelect.getValue()));
        Button calcAllBtn  = new Button("📊 Show all modes",
                e -> calculateAllModes(planSelect));

        keepOrderBtn.setEnabled(false);
        optimizeBtn.setEnabled(false);
        calcModeBtn.setEnabled(false);
        calcAllBtn.setEnabled(false);

        HorizontalLayout rightControls = new HorizontalLayout(
                loadPlansBtn, planSelect, modeSelect, calcModeBtn, calcAllBtn, keepOrderBtn, optimizeBtn
        );
        rightControls.setAlignItems(FlexComponent.Alignment.END);
        rightControls.setSpacing(true);
        rightControls.setFlexGrow(1);
        rightControls.getStyle().set("flex-wrap", "wrap");
        rightControls.getStyle().set("gap", "8px");

        HorizontalLayout topBar = new HorizontalLayout();
        topBar.setWidthFull();
        topBar.setJustifyContentMode(JustifyContentMode.BETWEEN);
        topBar.setAlignItems(FlexComponent.Alignment.END);
        topBar.add(backBtn, rightControls);
        setHorizontalComponentAlignment(Alignment.STRETCH, topBar);

        Grid<TripPlaceDto> placesGrid = new Grid<>(TripPlaceDto.class, false);
        placesGrid.addColumn(TripPlaceDto::displayName).setHeader("Place").setAutoWidth(true).setFlexGrow(1);
        placesGrid.addColumn(TripPlaceDto::lat).setHeader("Lat").setAutoWidth(true);
        placesGrid.addColumn(TripPlaceDto::lon).setHeader("Lon").setAutoWidth(true);

        planSelect.addValueChangeListener(ev -> {
            TripPlanDto selected = ev.getValue();
            List<TripPlaceDto> items = (selected == null) ? List.of() : selected.places();
            placesGrid.setItems(items);

            boolean hasTwoOrMore = items != null && items.size() >= 2;
            keepOrderBtn.setEnabled(hasTwoOrMore);
            optimizeBtn.setEnabled(hasTwoOrMore);
            calcModeBtn.setEnabled(hasTwoOrMore);
            calcAllBtn.setEnabled(hasTwoOrMore);

            if (!hasTwoOrMore) {
                lastDistanceMeters = null;
                lastDrivingSeconds = null;
                lastWasOptimized = false;
                modesGrid.setItems();
                modesGrid.setVisible(false);
            }
        });

        modesGrid.addColumn(ModeRow::mode).setHeader("Mode").setAutoWidth(true);
        modesGrid.addColumn(ModeRow::distanceKm).setHeader("Distance [km]").setAutoWidth(true);
        modesGrid.addColumn(ModeRow::durationHuman).setHeader("Duration").setAutoWidth(true);
        modesGrid.setVisible(false);

        add(topBar, placesGrid, modesGrid);
    }

    private void calculateByMode(Select<TripPlanDto> planSelect, TransportMode mode) {
        TripPlanDto selected = planSelect.getValue();
        if (!ensurePlanWithTwoPlaces(selected)) return;
        if (mode == null) {
            showWarningDialog("No transport mode", "Please choose DRIVING, WALKING or CYCLING.");
            return;
        }
        String jwt = extractCookie("jwt");
        if (jwt == null) {
            showWarningDialog("Missing JWT", "I need your JWT cookie to call backend services.");
            return;
        }

        try {
            if (lastDistanceMeters != null) {
                long adj = adjustedDurationSeconds(lastDistanceMeters, mode,
                        lastDrivingSeconds == null ? 0L : lastDrivingSeconds);
                ModeRow row = new ModeRow(mode.name(), km(lastDistanceMeters), humanizeSeconds(adj));
                modesGrid.setItems(row);
                modesGrid.setVisible(true);
                return;
            }
            RouteResponse r = journeyClient.routeByMode(selected.id(), mode.name().toLowerCase(), extractCookie("jwt"));
            long adj = adjustedDurationSeconds(r.distance(), mode, (long) r.duration());
            modesGrid.setItems(new ModeRow(mode.name(), km(r.distance()), humanizeSeconds(adj)));
            modesGrid.setVisible(true);
            Notification.show("Calculated for " + mode);
        } catch (Exception ex) {
            showWarningDialog("Failed to calculate", ex.getMessage());
        }
    }

    private void calculateAllModes(Select<TripPlanDto> planSelect) {
        TripPlanDto selected = planSelect.getValue();
        if (!ensurePlanWithTwoPlaces(selected)) return;
        String jwt = extractCookie("jwt");
        if (jwt == null) {
            showWarningDialog("Missing JWT", "I need your JWT cookie to call backend services.");
            return;
        }

        try {
            if (lastDistanceMeters != null) {
                double km = km(lastDistanceMeters);
                long dSec = (lastDrivingSeconds == null ? 0L : lastDrivingSeconds);
                long wSec = Math.round((km / SPEED_WALK_KMPH) * 3600.0);
                long cSec = Math.round((km / SPEED_CYCLE_KMPH) * 3600.0);
                modesGrid.setItems(
                        new ModeRow("DRIVING", km, humanizeSeconds(dSec)),
                        new ModeRow("WALKING", km, humanizeSeconds(wSec)),
                        new ModeRow("CYCLING", km, humanizeSeconds(cSec))
                );
                modesGrid.setVisible(true);
                return;
            }

            var multi = journeyClient.routeAllModes(selected.id(), jwt);
            double dKm = km(multi.driving().distanceMeters());
            double wKm = km(multi.walking().distanceMeters());
            double cKm = km(multi.cycling().distanceMeters());

            long dSec = (long) multi.driving().durationSeconds();
            long wSec = Math.round((wKm / SPEED_WALK_KMPH) * 3600.0);
            long cSec = Math.round((cKm / SPEED_CYCLE_KMPH) * 3600.0);

            modesGrid.setItems(
                    new ModeRow("DRIVING", dKm, humanizeSeconds(dSec)),
                    new ModeRow("WALKING", wKm, humanizeSeconds(wSec)),
                    new ModeRow("CYCLING", cKm, humanizeSeconds(cSec))
            );
            modesGrid.setVisible(true);
        } catch (Exception ex) {
            showWarningDialog("Failed to calculate", ex.getMessage());
        }
    }

    private void loadPlans(Select<TripPlanDto> planSelect) {
        String spotifyId = extractCookie("spotify_id");
        String jwt = extractCookie("jwt");
        if (spotifyId == null || jwt == null) {
            showWarningDialog("Missing data", """
                    I couldn't find required cookies.
                    Make sure you're logged in so I can read <b>spotify_id</b> and <b>jwt</b>.
                    """);
            return;
        }
        try {
            List<TripPlanDto> plans = tripPlanClient.getUserPlans(spotifyId, jwt);
            planSelect.setItems(plans);
            if (!plans.isEmpty()) planSelect.setValue(plans.get(0));
            Notification.show("Loaded " + plans.size() + " plans");
        } catch (Exception ex) {
            showWarningDialog("Failed to load plans", ex.getMessage());
        }
    }

    private void createRoute(Select<TripPlanDto> planSelect, boolean optimize) {
        TripPlanDto selected = planSelect.getValue();
        if (!ensurePlanWithTwoPlaces(selected)) return;

        String jwt = extractCookie("jwt");
        if (jwt == null) {
            showWarningDialog("Missing JWT", "I need your JWT cookie to call backend services.");
            return;
        }

        try {
            RouteResponse resp = journeyClient.createRoute(selected.id(), optimize, jwt);
            showResultDialog(selected, resp, optimize);

            lastDistanceMeters = resp.distance();
            lastDrivingSeconds = (long) resp.duration();
            lastWasOptimized   = optimize;

            double km = lastDistanceMeters / 1000.0;
            long dSec = lastDrivingSeconds;
            long wSec = Math.round((km / SPEED_WALK_KMPH) * 3600.0);
            long cSec = Math.round((km / SPEED_CYCLE_KMPH) * 3600.0);

            modesGrid.setItems(
                    new ModeRow("DRIVING", km, humanizeSeconds(dSec)),
                    new ModeRow("WALKING", km, humanizeSeconds(wSec)),
                    new ModeRow("CYCLING", km, humanizeSeconds(cSec))
            );
            modesGrid.setVisible(true);

        } catch (Exception ex) {
            showWarningDialog("Failed to create route", ex.getMessage());
        }
    }

    private void showResultDialog(TripPlanDto plan, RouteResponse resp, boolean optimized) {
        Dialog dlg = new Dialog();
        dlg.setHeaderTitle(optimized ? "✨ Optimized Route" : "🧭 Route (keep order)");

        double km = resp.distance() / 1000.0;
        String time = humanizeSeconds((long) resp.duration());

        VerticalLayout content = new VerticalLayout();
        content.setSpacing(false);
        content.setPadding(false);
        content.add(new H3(plan.name()));
        content.add(new Paragraph("Distance: " + round1(km) + " km"));
        content.add(new Paragraph("Duration: " + time));

        if (resp.orderedIds() != null && !resp.orderedIds().isEmpty()) {
            Map<Long, String> idToName = plan.places().stream()
                    .collect(Collectors.toMap(TripPlaceDto::id, TripPlaceDto::displayName));

            String prettyOrder = IntStream.range(0, resp.orderedIds().size())
                    .mapToObj(i -> (i + 1) + ". " + idToName.getOrDefault(resp.orderedIds().get(i), "Unknown"))
                    .collect(Collectors.joining("  →  "));

            content.add(new Paragraph("Order: " + prettyOrder));
        }

        dlg.add(content);

        Button close = new Button("Close", e -> dlg.close());
        dlg.getFooter().add(close);
        dlg.open();
    }

    private void showWarningDialog(String title, String message) {
        Dialog dlg = new Dialog();
        dlg.setHeaderTitle("⚠️ " + title);
        VerticalLayout content = new VerticalLayout(new Paragraph(message));
        content.setPadding(false);
        dlg.add(content);
        Button ok = new Button("OK", e -> dlg.close());
        dlg.getFooter().add(ok);
        dlg.open();
    }

    private boolean ensurePlanWithTwoPlaces(TripPlanDto selected) {
        if (selected == null) {
            showWarningDialog("No trip plan selected", "Please choose a trip plan first.");
            return false;
        }
        if (selected.places() == null || selected.places().size() < 2) {
            showWarningDialog("Not enough places", "You need at least 2 places.");
            return false;
        }
        return true;
    }

    private String extractCookie(String name) {
        Cookie[] cookies = VaadinService.getCurrentRequest().getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if (name.equals(c.getName())) return c.getValue();
            }
        }
        return null;
    }

    private String humanizeSeconds(long seconds) {
        Duration d = Duration.ofSeconds(seconds);
        long h = d.toHours();
        long m = d.minusHours(h).toMinutes();
        return (h > 0) ? String.format("%dh %02dm", h, m) : String.format("%d min", m);
    }

    private static double km(double meters) {
        return meters / 1000.0;
    }

    private long adjustedDurationSeconds(double distanceMeters, TransportMode mode, long backendSeconds) {
        double distKm = km(distanceMeters);
        return switch (mode) {
            case DRIVING -> backendSeconds;
            case WALKING -> Math.round((distKm / SPEED_WALK_KMPH) * 3600.0);
            case CYCLING -> Math.round((distKm / SPEED_CYCLE_KMPH) * 3600.0);
        };
    }

    private String round1(double value) {
        return String.format("%.1f", value);
    }

    private enum TransportMode { DRIVING, WALKING, CYCLING }

    private static class ModeRow {
        private final String mode;
        private final String distanceKm;
        private final String durationHuman;

        ModeRow(String mode, double distanceKm, String durationHuman) {
            this.mode = mode;
            this.distanceKm = String.format("%.1f", distanceKm);
            this.durationHuman = durationHuman;
        }

        public String mode() { return mode; }
        public String distanceKm() { return distanceKm; }
        public String durationHuman() { return durationHuman; }
    }
}