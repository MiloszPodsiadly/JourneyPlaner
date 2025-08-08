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
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinService;
import jakarta.servlet.http.Cookie;

import java.time.Duration;
import java.util.List;

@Route("journey")
public class JourneyView extends VerticalLayout {

    private final TripPlanClient tripPlanClient;
    private final JourneyClient journeyClient;

    public JourneyView(TripPlanClient tripPlanClient, JourneyClient journeyClient) {
        this.tripPlanClient = tripPlanClient;
        this.journeyClient = journeyClient;
        buildUI();
    }

    private void buildUI() {
        setSpacing(true);
        setPadding(true);
        add(new H1("🚗 Create Journey Plan"));

        Select<TripPlanDto> planSelect = new Select<>();
        planSelect.setLabel("Select Trip Plan");
        planSelect.setItemLabelGenerator(TripPlanDto::name);
        planSelect.setWidth("320px");

        Button loadPlansBtn = new Button("📂 Load My Trip Plans", e -> loadPlans(planSelect));
        Button keepOrderBtn = new Button("🧭 Create Route (keep order)", e -> createRoute(planSelect, false));
        Button optimizeBtn  = new Button("✨ Optimize Route (TSP)", e -> createRoute(planSelect, true));

        keepOrderBtn.getElement().getThemeList().add("primary");
        optimizeBtn.getElement().getThemeList().add("success");

        HorizontalLayout controls = new HorizontalLayout(planSelect, keepOrderBtn, optimizeBtn);
        controls.setAlignItems(FlexComponent.Alignment.END);
        controls.setSpacing(true);
        controls.setWidthFull();

        HorizontalLayout top = new HorizontalLayout(loadPlansBtn);
        top.setWidthFull();
        top.setJustifyContentMode(JustifyContentMode.START);

        Grid<TripPlaceDto> placesGrid = new Grid<>(TripPlaceDto.class, false);
        placesGrid.addColumn(TripPlaceDto::displayName).setHeader("Place").setAutoWidth(true).setFlexGrow(1);
        placesGrid.addColumn(TripPlaceDto::lat).setHeader("Lat").setAutoWidth(true);
        placesGrid.addColumn(TripPlaceDto::lon).setHeader("Lon").setAutoWidth(true);

        planSelect.addValueChangeListener(ev -> {
            TripPlanDto selected = ev.getValue();
            placesGrid.setItems(selected == null ? List.of() : selected.places());
        });

        add(top, controls, placesGrid);
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
        if (selected == null) {
            showWarningDialog("No trip plan selected", "Please choose a trip plan first.");
            return;
        }

        String jwt = extractCookie("jwt");
        if (jwt == null) {
            showWarningDialog("Missing JWT", "I need your JWT cookie to call backend services.");
            return;
        }

        try {
            RouteResponse resp = journeyClient.createRoute(selected.id(), optimize, jwt);
            showResultDialog(selected, resp, optimize);
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
            content.add(new Paragraph("Order: " + resp.orderedIds()));
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

    // ---- Helpers ----

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

    private String round1(double value) {
        return String.format("%.1f", value);
    }
}
