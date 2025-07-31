package com.milosz.podsiadly.uiservice.component;



import com.milosz.podsiadly.uiservice.service.TripPlanClient;
import com.milosz.podsiadly.uiservice.dto.TripPlanDto;
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
        setHeaderTitle("📌 Wybierz plan podróży");

        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(true);

        // Load existing plans
        List<TripPlanDto> plans = client.getUserPlans(spotifyId, token);
        Select<TripPlanDto> planSelect = new Select<>();
        planSelect.setLabel("📋 Istniejące plany");
        planSelect.setItems(plans);
        planSelect.setItemLabelGenerator(TripPlanDto::name);

        Button selectBtn = new Button("✅ Wybierz ten plan", e -> {
            TripPlanDto selected = planSelect.getValue();
            if (selected != null) {
                onPlanSelected.accept(selected);
                close();
            } else {
                Notification.show("⚠️ Wybierz plan z listy.");
            }
        });

        // New plan
        TextField nameField = new TextField("📌 Nazwa nowego planu");
        TextField descField = new TextField("📝 Opis planu");
        Button createBtn = new Button("🆕 Utwórz i wybierz", e -> {
            String name = nameField.getValue();
            String desc = descField.getValue();
            if (name.isBlank()) {
                Notification.show("⚠️ Nazwa planu jest wymagana.");
                return;
            }

            try {
                TripPlanDto newPlan = client.createPlan(spotifyId, name, desc, token);
                onPlanSelected.accept(newPlan);
                close();
            } catch (Exception ex) {
                log.error("❌ Nie udało się stworzyć planu: {}", ex.getMessage());
                Notification.show("❌ Błąd tworzenia planu.");
            }
        });

        layout.add(planSelect, selectBtn, nameField, descField, createBtn);
        add(layout);
    }
}

