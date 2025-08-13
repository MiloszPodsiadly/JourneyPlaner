package com.milosz.podsiadly.uiservice.vaadin;

import com.milosz.podsiadly.uiservice.service.JourneyClient;
import com.milosz.podsiadly.uiservice.service.TripPlanClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;


@DisplayName("JourneyView (pure helper tests)")
class JourneyViewTest {

    private JourneyView view;
    private NumberFormat nf;

    @BeforeEach
    void setUp() {
        TripPlanClient tripPlanClient = mock(TripPlanClient.class);
        JourneyClient journeyClient   = mock(JourneyClient.class);
        view = new JourneyView(tripPlanClient, journeyClient);

        nf = NumberFormat.getNumberInstance(Locale.getDefault());
        nf.setGroupingUsed(false);
        nf.setMinimumFractionDigits(1);
        nf.setMaximumFractionDigits(1);
    }

    private double km(double meters) throws Exception {
        Method m = JourneyView.class.getDeclaredMethod("km", double.class);
        m.setAccessible(true);
        return (double) m.invoke(null, meters);
    }

    private String humanizeSeconds(long seconds) throws Exception {
        Method m = JourneyView.class.getDeclaredMethod("humanizeSeconds", long.class);
        m.setAccessible(true);
        return (String) m.invoke(view, seconds);
    }

    private String round1Str(double value) throws Exception {
        Method m = JourneyView.class.getDeclaredMethod("round1", double.class);
        m.setAccessible(true);
        return (String) m.invoke(view, value);
    }

    private long adjustedDurationSeconds(double distanceMeters, String modeName, long backendSeconds) throws Exception {
        Class<?> modeEnum = Class.forName(JourneyView.class.getName() + "$TransportMode");
        Object mode = Enum.valueOf((Class<Enum>) modeEnum, modeName);
        Method m = JourneyView.class.getDeclaredMethod("adjustedDurationSeconds", double.class, modeEnum, long.class);
        m.setAccessible(true);
        return (long) m.invoke(view, distanceMeters, mode, backendSeconds);
    }

    private double parseOneDecimal(String s) throws ParseException {
        return nf.parse(s).doubleValue();
    }

    private static double round1Numeric(double v) {
        return Math.round(v * 10.0) / 10.0;
    }


    @Test
    @DisplayName("km: converts meters to kilometers")
    void km_convertsMeters() throws Exception {
        assertThat(km(0)).isEqualTo(0.0);
        assertThat(km(1234.0)).isEqualTo(1.234);
        assertThat(km(10_000.0)).isEqualTo(10.0);
    }

    @Test
    @DisplayName("humanizeSeconds: <1h shows 'X min', ≥1h shows 'Hh MMm'")
    void humanizeSeconds_formats() throws Exception {
        assertThat(humanizeSeconds(0)).isEqualTo("0 min");
        assertThat(humanizeSeconds(59 * 60)).isEqualTo("59 min");
        assertThat(humanizeSeconds(60 * 60)).isEqualTo("1h 00m");
        assertThat(humanizeSeconds(2 * 3600 + 5 * 60)).isEqualTo("2h 05m");
        assertThat(humanizeSeconds(3 * 3600 + 59 * 60)).isEqualTo("3h 59m");
    }

    @Test
    @DisplayName("round1: 1-decimal string; numeric value matches conventional rounding within tolerance")
    void round1_numericExpectation_localeAgnostic() throws Exception {

        double[] samples = {
                0.0, 1.0, 1.0000000001, 1.04, 1.0499999999, 1.05, 12.349, 12.351
        };
        for (double v : samples) {
            String s = round1Str(v);
            double parsed = parseOneDecimal(s);

            double expected = round1Numeric(v);
            assertThat(parsed).isCloseTo(expected, withinUlpOrEpsilon(expected));
            assertHasExactlyOneFractionDigit(s);
        }
    }

    @Test
    @DisplayName("adjustedDurationSeconds: DRIVING = backend; WALKING/CYCLING use 4/20 km/h")
    void adjustedDurationSeconds_behaviour() throws Exception {
        double meters = 10_000.0;

        assertThat(adjustedDurationSeconds(meters, "DRIVING", 777L)).isEqualTo(777L);

        assertThat(adjustedDurationSeconds(meters, "WALKING", 0L)).isEqualTo(9_000L);

        assertThat(adjustedDurationSeconds(meters, "CYCLING", 0L)).isEqualTo(1_800L);
    }

    @Test
    @DisplayName("ModeRow: distance rendered with exactly one decimal (locale-aware) and getters work")
    void modeRow_formatting_localeAgnostic() throws Exception {
        Class<?> modeRowCls = Class.forName(JourneyView.class.getName() + "$ModeRow");
        Constructor<?> ctor = modeRowCls.getDeclaredConstructor(String.class, double.class, String.class);
        ctor.setAccessible(true);

        double noisy = 12.345000000000001;
        Object row = ctor.newInstance("DRIVING", noisy, "1h 23m");

        Method mode = modeRowCls.getDeclaredMethod("mode");
        Method distanceKm = modeRowCls.getDeclaredMethod("distanceKm");
        Method durationHuman = modeRowCls.getDeclaredMethod("durationHuman");
        mode.setAccessible(true);
        distanceKm.setAccessible(true);
        durationHuman.setAccessible(true);

        String distanceStr = (String) distanceKm.invoke(row);

        assertThat(mode.invoke(row)).isEqualTo("DRIVING");
        assertHasExactlyOneFractionDigit(distanceStr);

        double parsed = parseOneDecimal(distanceStr);
        double expected = round1Numeric(noisy);
        assertThat(parsed).isCloseTo(expected, withinUlpOrEpsilon(expected));

        assertThat(durationHuman.invoke(row)).isEqualTo("1h 23m");
    }

    private static org.assertj.core.data.Offset<Double> withinUlpOrEpsilon(double expected) {
        double eps = Math.max(Math.ulp(expected), 1e-12);
        return org.assertj.core.data.Offset.offset(eps * 2);
    }

    private static void assertHasExactlyOneFractionDigit(String s) {
        int comma = s.indexOf(',');
        int dot = s.indexOf('.');
        int sep = (comma >= 0) ? comma : dot;
        if (sep < 0) {
            throw new AssertionError("Expected a decimal separator in '" + s + "'");
        }
        String frac = s.substring(sep + 1);
        if (frac.length() != 1) {
            throw new AssertionError("Expected exactly one fractional digit in '" + s + "', got '" + frac + "'");
        }
    }
}