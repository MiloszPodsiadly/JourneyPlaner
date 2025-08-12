package com.milosz.podsiadly.uiservice.component;

import com.milosz.podsiadly.uiservice.dto.TripPlanDto;
import com.milosz.podsiadly.uiservice.service.TripPlanClient;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("TripPlanSelectionDialog – unit tests (no UI attach)")
class TripPlanSelectionDialogTest {

    private TripPlanClient client;

    @BeforeEach
    void setUp() {
        client = mock(TripPlanClient.class);
    }

    private static <T extends Component> T findFirst(Dialog dialog, Class<T> type) {
        Optional<Component> comp = dialog.getChildren()
                .flatMap(c -> {
                    if (type.isInstance(c)) return Optional.of(type.cast(c)).stream();
                    return c.getChildren().filter(type::isInstance);
                })
                .findFirst();
        return comp.map(type::cast).orElseThrow();
    }

    private static Button findButton(Dialog dialog, String exactText) {
        return dialog.getChildren()
                .flatMap(c -> c.getChildren())
                .filter(Button.class::isInstance)
                .map(Button.class::cast)
                .filter(b -> exactText.equals(b.getText()))
                .findFirst()
                .orElseThrow();
    }

    @Test
    @DisplayName("Selecting an existing plan invokes a consumer with the correct DTO")
    void selectingExistingPlan_callsConsumer() {
        TripPlanDto p1 = new TripPlanDto(1L, "Plan A", "d", List.of(), List.of());
        TripPlanDto p2 = new TripPlanDto(2L, "Plan B", "d2", List.of(), List.of());
        when(client.getUserPlans("s", "t")).thenReturn(List.of(p1, p2));

        AtomicReference<TripPlanDto> chosen = new AtomicReference<>();
        TripPlanSelectionDialog dialog = new TripPlanSelectionDialog("s", "t", client, chosen::set);

        Select<TripPlanDto> select = findFirst(dialog, Select.class);
        Button chooseBtn = findButton(dialog, "✅ Choose this plan");

        select.setValue(p2);
        chooseBtn.click();

        assertThat(chosen.get()).isEqualTo(p2);
        verify(client).getUserPlans("s", "t");
        verifyNoMoreInteractions(client);
    }

    @Test
    @DisplayName("Creating a new plan calls TripPlanClient.createPlan and consumer")
    void creatingNewPlan_callsClientAndConsumer() {
        // given
        when(client.getUserPlans("userX", "tok")).thenReturn(List.of());
        TripPlanDto created = new TripPlanDto(10L, "New One", "desc", List.of(), List.of());
        when(client.createPlan("userX", "New One", "desc", "tok")).thenReturn(created);

        AtomicReference<TripPlanDto> chosen = new AtomicReference<>();
        TripPlanSelectionDialog dialog = new TripPlanSelectionDialog("userX", "tok", client, chosen::set);

        TextField name = findFirst(dialog, TextField.class);
        TextField desc = dialog.getChildren()
                .flatMap(Component::getChildren)
                .filter(TextField.class::isInstance)
                .map(TextField.class::cast)
                .skip(1)
                .findFirst()
                .orElseThrow();

        Button createBtn = findButton(dialog, "🆕 Create");

        try (MockedStatic<Notification> mocked = mockStatic(Notification.class)) {
            name.setValue("New One");
            desc.setValue("desc");
            createBtn.click();

            assertThat(chosen.get()).isEqualTo(created);
            verify(client).getUserPlans("userX", "tok");
            verify(client).createPlan("userX", "New One", "desc", "tok");
            mocked.verifyNoInteractions();
        }
    }

    @Test
    @DisplayName("Duplicate name → does not call createPlan, shows Notification")
    void creatingNewPlan_withDuplicateName_showsNotification() {
        TripPlanDto existing = new TripPlanDto(1L, "City Trip", "x", List.of(), List.of());
        when(client.getUserPlans("s", "t")).thenReturn(List.of(existing));

        AtomicReference<TripPlanDto> chosen = new AtomicReference<>();
        TripPlanSelectionDialog dialog = new TripPlanSelectionDialog("s", "t", client, chosen::set);

        TextField name = findFirst(dialog, TextField.class);
        TextField desc = dialog.getChildren()
                .flatMap(Component::getChildren)
                .filter(TextField.class::isInstance)
                .map(TextField.class::cast)
                .skip(1)
                .findFirst()
                .orElseThrow();

        Button createBtn = findButton(dialog, "🆕 Create");

        try (MockedStatic<Notification> mocked = mockStatic(Notification.class)) {
            name.setValue(" city trip ");
            desc.setValue("ignored");
            createBtn.click();

            verify(client).getUserPlans("s", "t");
            verify(client, never()).createPlan(anyString(), anyString(), anyString(), anyString());
            assertThat(chosen.get()).isNull();

            mocked.verify(() ->
                    Notification.show("⚠️ A plan with this name already exists.", 3000, Notification.Position.MIDDLE)
            );
        }
    }

    @Test
    @DisplayName("No name → does not call createPlan, shows Notification")
    void creatingNewPlan_withoutName_showsNotification() {
        when(client.getUserPlans(anyString(), anyString())).thenReturn(List.of());

        AtomicReference<TripPlanDto> chosen = new AtomicReference<>();
        TripPlanSelectionDialog dialog = new TripPlanSelectionDialog("u", "tok", client, chosen::set);

        TextField name = findFirst(dialog, TextField.class);
        Button createBtn = findButton(dialog, "🆕 Create");

        try (MockedStatic<Notification> mocked = mockStatic(Notification.class)) {
            name.setValue("   ");
            createBtn.click();

            verify(client).getUserPlans("u", "tok");
            verify(client, never()).createPlan(anyString(), anyString(), anyString(), anyString());
            assertThat(chosen.get()).isNull();

            mocked.verify(() ->
                    Notification.show("⚠️ Plan name is required.", 3000, Notification.Position.MIDDLE)
            );
        }
    }

    @Test
    @DisplayName("Error creatingPlan → shows Notification with error")
    void creatingNewPlan_clientThrows_showsErrorNotification() {
        when(client.getUserPlans("u", "t")).thenReturn(List.of());
        when(client.createPlan(anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("boom"));

        AtomicReference<TripPlanDto> chosen = new AtomicReference<>();
        TripPlanSelectionDialog dialog = new TripPlanSelectionDialog("u", "t", client, chosen::set);

        TextField name = findFirst(dialog, TextField.class);
        TextField desc = dialog.getChildren()
                .flatMap(Component::getChildren)
                .filter(TextField.class::isInstance)
                .map(TextField.class::cast)
                .skip(1)
                .findFirst()
                .orElseThrow();

        Button createBtn = findButton(dialog, "🆕 Create");

        try (MockedStatic<Notification> mocked = mockStatic(Notification.class)) {
            name.setValue("X");
            desc.setValue("Y");
            createBtn.click();

            verify(client).getUserPlans("u", "t");
            verify(client).createPlan("u", "X", "Y", "t");
            assertThat(chosen.get()).isNull();

            mocked.verify(() -> Notification.show("❌ Plan creation error."));
        }
    }
}