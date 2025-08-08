package com.milosz.podsiadly.uiservice.component;

import com.milosz.podsiadly.uiservice.dto.TripPlanDto;
import com.milosz.podsiadly.uiservice.service.TripPlanClient;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.function.Consumer;

@Slf4j
public class TripPlanSelectionDialog extends Dialog {

    public TripPlanSelectionDialog(String spotifyId, String token, TripPlanClient client, Consumer<TripPlanDto> onPlanSelected) {
        setHeaderTitle("📌 Choose your travel plan");

        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(true);

        List<TripPlanDto> plans = client.getUserPlans(spotifyId, token);

        Select<TripPlanDto> planSelect = new Select<>();
        planSelect.setLabel("📋 Existing plans");
        planSelect.setItems(plans);
        planSelect.setItemLabelGenerator(TripPlanDto::name);

        Button selectBtn = new Button("✅ Choose this plan", e -> {
            TripPlanDto selected = planSelect.getValue();
            if (selected != null) {
                onPlanSelected.accept(selected);
                close();
            } else {
                Notification.show("⚠️ Select a plan from the list.");
            }
        });

        TextField nameField = new TextField("📌 Name of the new plan");
        TextField descField = new TextField("📝 Plan description");

        Button createBtn = new Button("🆕 Create", e -> {
            String name = nameField.getValue() != null ? nameField.getValue().trim() : "";
            String desc = descField.getValue() != null ? descField.getValue().trim() : "";

            if (name.isBlank()) {
                Notification.show("⚠️ Plan name is required.", 3000, Notification.Position.MIDDLE);
                return;
            }

            boolean duplicate = plans.stream()
                    .anyMatch(p -> p.name() != null && p.name().trim().equalsIgnoreCase(name));
            if (duplicate) {
                Notification.show("⚠️ A plan with this name already exists.", 3000, Notification.Position.MIDDLE);
                return;
            }

            try {
                TripPlanDto newPlan = client.createPlan(spotifyId, name, desc, token);
                onPlanSelected.accept(newPlan);
                close();
            } catch (Exception ex) {
                log.error("❌ Failed to create a plan: {}", ex.getMessage(), ex);
                Notification.show("❌ Plan creation error.");
            }
        });

        layout.add(planSelect, selectBtn, nameField, descField, createBtn);
        add(layout);
    }
}
