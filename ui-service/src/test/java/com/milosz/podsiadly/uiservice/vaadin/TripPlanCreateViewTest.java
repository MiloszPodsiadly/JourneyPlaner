package com.milosz.podsiadly.uiservice.vaadin;

import com.milosz.podsiadly.uiservice.dto.TripPlanDto;
import com.milosz.podsiadly.uiservice.dto.TripPlaceDto;
import com.milosz.podsiadly.uiservice.dto.TripPlaylistDto;
import com.milosz.podsiadly.uiservice.service.TripPlanClient;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@DisplayName("TripPlanCreateView – UI-safe unit tests")
class TripPlanCreateViewTest {

    private UI ui;
    private TripPlanCreateView view;
    private TripPlanClient tripPlanClient;
    private MockedStatic<VaadinService> vaadinServiceStatic;

    @BeforeEach
    void setUp() throws Exception {
        VaadinRequest req = mock(VaadinRequest.class);
        when(req.getCookies()).thenReturn(new Cookie[0]);
        vaadinServiceStatic = mockStatic(VaadinService.class);
        vaadinServiceStatic.when(VaadinService::getCurrentRequest).thenReturn(req);

        ui = new UI();
        UI.setCurrent(ui);

        view = new TripPlanCreateView();
        ui.add(view);

        tripPlanClient = mock(TripPlanClient.class);
        Field fClient = TripPlanCreateView.class.getDeclaredField("tripPlanClient");
        fClient.setAccessible(true);
        fClient.set(view, tripPlanClient);
    }

    @AfterEach
    void tearDown() {
        if (vaadinServiceStatic != null) vaadinServiceStatic.close();
        UI.setCurrent(null);
    }

    @Test
    @DisplayName("Filtering + sorting: only matching plan is rendered, sorted Z-A")
    void filterAndSort_rendered() throws Exception {
        TripPlanDto a = plan(1L, "Alpha", "desc",
                List.of(place(11L, "X", "0", "0", null)), List.of());
        TripPlanDto b = plan(2L, "Beta", "desc",
                List.of(place(22L, "Y", "0", "0", null)), List.of());

        setAllPlans(a, b);

        VerticalLayout container = new VerticalLayout();
        callUpdatePlanList(container, "b", "Z-A", "tok");

        var titles = findAll(container, H3.class, h -> true);
        assertThat(titles).hasSize(1);
        assertThat(titles.get(0).getText()).isEqualTo("Beta");
    }

    @Test
    @DisplayName("Save & Exit triggers reorderPlaces(planId, pendingIds, token)")
    void editSave_callsReorder() throws Exception {
        TripPlanDto plan = plan(1L, "Road Trip", "desc",
                List.of(
                        place(11L, "A", "0", "0", null),
                        place(22L, "B", "0", "0", null)
                ),
                List.of());

        setAllPlans(plan);
        setEditMode(plan.id(), true);
        setPendingOrder(plan.id(), List.of(11L, 22L));

        VerticalLayout container = new VerticalLayout();
        callUpdatePlanList(container, null, "A-Z", "jwt-123");

        Button saveExit = findButtonByText(container, "Save & Exit");
        assertThat(saveExit).isNotNull();

        saveExit.click();

        verify(tripPlanClient, times(1))
                .reorderPlaces(eq(1L), eq(List.of(11L, 22L)), eq("jwt-123"));
    }

    @Test
    @DisplayName("Delete plan button calls TripPlanClient.deletePlan(planId, token)")
    void delete_callsClient() throws Exception {
        TripPlanDto plan = plan(5L, "To Delete", "d",
                List.of(place(51L, "P", "0", "0", null)), List.of());
        setAllPlans(plan);

        VerticalLayout container = new VerticalLayout();
        callUpdatePlanList(container, null, "A-Z", "jwt-token");

        Button deleteBtn = findButtonByText(container, "Delete plan");
        assertThat(deleteBtn).isNotNull();

        deleteBtn.click();

        verify(tripPlanClient, times(1)).deletePlan(5L, "jwt-token");
    }

    private TripPlanDto plan(Long id, String name, String desc,
                             List<TripPlaceDto> places, List<TripPlaylistDto> playlists) {
        return new TripPlanDto(id, name, desc, places, playlists);
    }

    private TripPlaceDto place(Long id, String name, String lat, String lon, String category) {
        return new TripPlaceDto(id, name, lat, lon, category);
    }

    private void setAllPlans(TripPlanDto... plans) throws Exception {
        Field f = TripPlanCreateView.class.getDeclaredField("allPlans");
        f.setAccessible(true);
        f.set(view, Arrays.asList(plans));
    }

    private void setEditMode(Long planId, boolean on) throws Exception {
        Field f = TripPlanCreateView.class.getDeclaredField("editModePlanIds");
        f.setAccessible(true);
        @SuppressWarnings("unchecked")
        Set<Long> s = (Set<Long>) f.get(view);
        if (on) s.add(planId); else s.remove(planId);
    }

    private void setPendingOrder(Long planId, List<Long> order) throws Exception {
        Field f = TripPlanCreateView.class.getDeclaredField("pendingPlaceOrder");
        f.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<Long, List<Long>> m = (Map<Long, List<Long>>) f.get(view);
        m.put(planId, order);
    }

    private void callUpdatePlanList(VerticalLayout container, String filter, String sort, String token) throws Exception {
        Method m = TripPlanCreateView.class.getDeclaredMethod(
                "updatePlanList", VerticalLayout.class, String.class, String.class, String.class);
        m.setAccessible(true);
        m.invoke(view, container, filter, sort, token);
        view.add(container);
    }

    private <T extends Component> List<T> findAll(Component root, Class<T> type, Predicate<T> filter) {
        return descendants(root).filter(type::isInstance).map(type::cast).filter(filter).toList();
    }

    private Button findButtonByText(Component root, String text) {
        return findAll(root, Button.class, b -> text.equals(b.getText()))
                .stream().findFirst().orElse(null);
    }

    private Stream<Component> descendants(Component c) {
        return Stream.concat(Stream.of(c), c.getChildren().flatMap(this::descendants));
    }
}