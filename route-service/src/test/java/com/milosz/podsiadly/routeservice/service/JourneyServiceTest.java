package com.milosz.podsiadly.routeservice.service;

import com.milosz.podsiadly.routeservice.dto.RouteResponse;
import com.milosz.podsiadly.routeservice.dto.TripPlaceView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@DisplayName("JourneyService unit tests")
class JourneyServiceTest {

    private UserServiceClient userServiceClient;
    private OsrmClient osrmClient;
    private JourneyService service;

    @BeforeEach
    void setUp() {
        userServiceClient = mock(UserServiceClient.class);
        osrmClient = mock(OsrmClient.class);
        service = new JourneyService(userServiceClient, osrmClient);
    }

    private TripPlaceView place(long id, double lat, double lon) {
        try {
            if (!TripPlaceView.class.isRecord()) {
                throw new IllegalStateException("TripPlaceView is not a record – fit helper.");
            }
            RecordComponent[] comps = TripPlaceView.class.getRecordComponents();
            Class<?>[] ctorTypes = new Class<?>[comps.length];
            Object[] args = new Object[comps.length];
            for (int i = 0; i < comps.length; i++) {
                ctorTypes[i] = comps[i].getType();
                String nm = comps[i].getName().toLowerCase();
                Class<?> t = comps[i].getType();

                if (nm.equals("id") && (t == Long.class || t == long.class)) {
                    args[i] = id;
                } else if ((nm.equals("lat") || nm.equals("latitude")) && (t == double.class || t == Double.class)) {
                    args[i] = lat;
                } else if ((nm.equals("lon") || nm.equals("lng") || nm.equals("longitude")) && (t == double.class || t == Double.class)) {
                    args[i] = lon;
                } else {
                    if (t == String.class) args[i] = null;
                    else if (t == Integer.class || t == int.class) args[i] = 0;
                    else if (t == Long.class || t == long.class) args[i] = 0L;
                    else if (t == Double.class || t == double.class) args[i] = 0.0d;
                    else if (t == Boolean.class || t == boolean.class) args[i] = false;
                    else args[i] = null;
                }
            }
            Constructor<TripPlaceView> ctor = TripPlaceView.class.getDeclaredConstructor(ctorTypes);
            ctor.setAccessible(true);
            return ctor.newInstance(args);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot to construct TripPlaceView – update helper to define record.", e);
        }
    }

    private OsrmClient.Waypoint waypoint(int idx) {
        OsrmClient.Waypoint w = new OsrmClient.Waypoint();
        w.waypoint_index = idx;
        return w;
    }

    private OsrmClient.Result result(double distance, double duration, OsrmClient.GeoJson geometry, List<OsrmClient.Waypoint> waypoints) {
        return new OsrmClient.Result(distance, duration, geometry, waypoints);
    }


    @Test
    @DisplayName("routeByTripPlan(opt=false) → calls routeKeepOrder and keeps the ID order")
    void routeByTripPlan_keepOrder() {
        String jwt = "tok";
        List<TripPlaceView> places = List.of(
                place(10L, 50.0, 19.9),
                place(11L, 50.1, 20.0),
                place(12L, 50.2, 20.1)
        );
        when(userServiceClient.getPlacesForTripPlan(7L, jwt)).thenReturn(places);

        OsrmClient.Result res = result(123.4, 56.7, null, null);
        when(osrmClient.routeKeepOrder(any())).thenReturn(res);

        RouteResponse out = service.routeByTripPlan(7L, false, jwt);

        verify(userServiceClient).getPlacesForTripPlan(7L, jwt);
        verify(osrmClient).routeKeepOrder(any());

        assertThat(out.distanceMeters()).isEqualTo(123.4);
        assertThat(out.durationSeconds()).isEqualTo(56.7);
        assertThat(out.geometry()).isNull();
        assertThat(out.orderedPlaceIds()).containsExactly(10L, 11L, 12L);
    }

    @Test
    @DisplayName("routeByTripPlan(opt=true) → when waypoints=null, fallback to original order")
    void routeByTripPlan_optimize_fallbackToOriginalOrder() {
        List<TripPlaceView> places = List.of(
                place(1L, 1.0, 1.0),
                place(2L, 2.0, 2.0)
        );
        when(userServiceClient.getPlacesForTripPlan(1L, "j")).thenReturn(places);

        OsrmClient.Result res = result(10.0, 20.0, null, null);
        when(osrmClient.tripOptimize(any())).thenReturn(res);

        RouteResponse out = service.routeByTripPlan(1L, true, "j");

        verify(osrmClient).tripOptimize(any());
        assertThat(out.orderedPlaceIds()).containsExactly(1L, 2L);
    }

    @Test
    @DisplayName("routeByTripPlan(opt=true) → re-order after waypoint_index")
    void routeByTripPlan_optimize_reordersByWaypointIndex() {
        List<TripPlaceView> places = List.of(
                place(100L, 1.0, 1.0),  // idx 0
                place(200L, 2.0, 2.0),  // idx 1
                place(300L, 3.0, 3.0)   // idx 2
        );
        when(userServiceClient.getPlacesForTripPlan(5L, null)).thenReturn(places);

        List<OsrmClient.Waypoint> wps = new ArrayList<>();
        wps.add(waypoint(2));
        wps.add(waypoint(0));
        wps.add(waypoint(1));
        OsrmClient.Result res = result(42.0, 7.0, null, wps);

        when(osrmClient.tripOptimize(any())).thenReturn(res);

        RouteResponse out = service.routeByTripPlan(5L, true, null);

        verify(osrmClient).tripOptimize(any());
        assertThat(out.distanceMeters()).isEqualTo(42.0);
        assertThat(out.durationSeconds()).isEqualTo(7.0);
        assertThat(out.orderedPlaceIds()).containsExactly(300L, 100L, 200L);
    }

    @Test
    @DisplayName("routeByTripPlan: <2 places → IllegalArgumentException")
    void routeByTripPlan_tooFewPlaces() {
        when(userServiceClient.getPlacesForTripPlan(9L, "x"))
                .thenReturn(List.of(place(1L, 1.0, 1.0)));

        assertThatThrownBy(() -> service.routeByTripPlan(9L, false, "x"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least 2");

        verifyNoInteractions(osrmClient);
    }


    @Test
    @DisplayName("routeDrivingByTripPlan → uses routeKeepOrderDriving, returns ID in original order")
    void routeDriving() {
        List<TripPlaceView> places = List.of(
                place(10L, 1.0, 1.0),
                place(20L, 2.0, 2.0)
        );
        when(userServiceClient.getPlacesForTripPlan(2L, "j")).thenReturn(places);
        when(osrmClient.routeKeepOrderDriving(any()))
                .thenReturn(result(5.5, 6.6, null, null));

        RouteResponse out = service.routeDrivingByTripPlan(2L, "j");

        verify(osrmClient).routeKeepOrderDriving(any());
        assertThat(out.orderedPlaceIds()).containsExactly(10L, 20L);
        assertThat(out.distanceMeters()).isEqualTo(5.5);
        assertThat(out.durationSeconds()).isEqualTo(6.6);
        assertThat(out.geometry()).isNull();
    }

    @Test
    @DisplayName("routeWalkingByTripPlan → usues routeKeepOrderWalking")
    void routeWalking() {
        List<TripPlaceView> places = List.of(place(1L, 0.0, 0.0), place(2L, 1.0, 1.0));
        when(userServiceClient.getPlacesForTripPlan(3L, "t")).thenReturn(places);
        when(osrmClient.routeKeepOrderWalking(any()))
                .thenReturn(result(1.2, 3.4, null, null));

        RouteResponse out = service.routeWalkingByTripPlan(3L, "t");

        verify(osrmClient).routeKeepOrderWalking(any());
        assertThat(out.orderedPlaceIds()).containsExactly(1L, 2L);
        assertThat(out.distanceMeters()).isEqualTo(1.2);
        assertThat(out.durationSeconds()).isEqualTo(3.4);
    }

    @Test
    @DisplayName("routeCyclingByTripPlan → uses routeKeepOrderCycling")
    void routeCycling() {
        List<TripPlaceView> places = List.of(place(7L, 0.0, 0.0), place(8L, 1.0, 1.0));
        when(userServiceClient.getPlacesForTripPlan(4L, "jwt")).thenReturn(places);
        when(osrmClient.routeKeepOrderCycling(any()))
                .thenReturn(result(9.9, 8.8, null, null));

        RouteResponse out = service.routeCyclingByTripPlan(4L, "jwt");

        verify(osrmClient).routeKeepOrderCycling(any());
        assertThat(out.orderedPlaceIds()).containsExactly(7L, 8L);
    }

    @Test
    @DisplayName("simple modes: <2 places → IllegalArgumentException")
    void simpleModes_tooFewPlaces() {
        when(userServiceClient.getPlacesForTripPlan(77L, "jj"))
                .thenReturn(List.of(place(1L, 0.0, 0.0)));

        assertThatThrownBy(() -> service.routeDrivingByTripPlan(77L, "jj"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.routeWalkingByTripPlan(77L, "jj"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.routeCyclingByTripPlan(77L, "jj"))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(osrmClient);
    }

    @Test
    @DisplayName("JWT is propagated to UserServiceClient.getPlacesForTripPlan")
    void jwtIsPropagated() {
        when(userServiceClient.getPlacesForTripPlan(1L, "XYZ"))
                .thenReturn(List.of(place(1L, 0.0, 0.0), place(2L, 1.0, 1.0)));
        when(osrmClient.routeKeepOrder(any()))
                .thenReturn(result(0.0, 0.0, null, null));

        service.routeByTripPlan(1L, false, "XYZ");

        verify(userServiceClient).getPlacesForTripPlan(1L, "XYZ");
    }
}