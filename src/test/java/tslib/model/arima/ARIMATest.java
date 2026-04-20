package tslib.model.arima;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ARIMATest {

    @Test
    public void arima010CapturesRandomWalkWithDrift() {
        List<Double> data = List.of(1.0, 2.0, 3.0, 4.0, 5.0, 6.0);
        ARIMA model = new ARIMA(0, 1, 0).fit(data);

        List<Double> future = model.forecast(3);

        assertEquals(1.0, model.getIntercept(), 1e-6);
        assertEquals(3, future.size());
        assertEquals(7.0, future.get(0), 1e-6);
        assertEquals(8.0, future.get(1), 1e-6);
        assertEquals(9.0, future.get(2), 1e-6);
    }

    @Test
    public void arima100RecoversAutoregressiveSignal() {
        List<Double> data = new ArrayList<>();
        double value = 10.0;
        data.add(value);
        for (int i = 1; i < 60; i++) {
            value = 2.0 + 0.75 * value;
            data.add(value);
        }

        ARIMA model = new ARIMA(1, 0, 0).fit(data);
        double[] ar = model.getArCoefficients();
        List<Double> future = model.forecast(1);
        double expectedNext = 2.0 + 0.75 * data.get(data.size() - 1);

        assertEquals(1, ar.length);
        assertEquals(0.75, ar[0], 1e-3);
        assertEquals(2.0, model.getIntercept(), 1e-3);
        assertEquals(expectedNext, future.get(0), 1e-3);
    }

    @Test
    public void convenienceForecastIncludesFittedHistoryAndFuture() {
        List<Double> data = List.of(5.0, 6.0, 7.0, 8.0, 9.0, 10.0);
        ARIMA model = new ARIMA(0, 1, 0);

        List<Double> combined = model.forecast(data, 2);

        assertEquals(data.size() + 2, combined.size());
        assertEquals(11.0, combined.get(combined.size() - 2), 1e-6);
        assertEquals(12.0, combined.get(combined.size() - 1), 1e-6);
        assertTrue(model.getInnovationVariance() >= 0.0);
    }

    static Stream<Arguments> arimaOrders() {
        return Stream.of(
            Arguments.of(0, 1, 0),
            Arguments.of(1, 0, 0),
            Arguments.of(0, 0, 1),
            Arguments.of(1, 1, 0),
            Arguments.of(0, 1, 1),
            Arguments.of(2, 0, 0),
            Arguments.of(1, 0, 1),
            Arguments.of(0, 2, 0),
            Arguments.of(2, 1, 0),
            Arguments.of(1, 1, 1)
        );
    }

    @ParameterizedTest(name = "ARIMA({0},{1},{2}) forecasts finite values")
    @MethodSource("arimaOrders")
    public void arimaFitsAndForecastsFiniteValues(int p, int d, int q) {
        List<Double> data = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            data.add((double) i + (i % 3) * 0.5);
        }
        ARIMA model = new ARIMA(p, d, q).fit(data);
        List<Double> future = model.forecast(3);

        assertEquals(3, future.size());
        for (double f : future) {
            assertFalse(Double.isNaN(f), "Forecast must not be NaN for ARIMA(" + p + "," + d + "," + q + ")");
            assertFalse(Double.isInfinite(f), "Forecast must not be infinite for ARIMA(" + p + "," + d + "," + q + ")");
        }
    }

    @ParameterizedTest(name = "ARIMA({0},{1},{2}) residuals are non-empty and bounded")
    @MethodSource("arimaOrders")
    public void arimaResidualsAreNonEmptyAndBounded(int p, int d, int q) {
        List<Double> data = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            data.add((double) i);
        }
        ARIMA model = new ARIMA(p, d, q).fit(data);
        List<Double> residuals = model.getResiduals();
        assertFalse(residuals.isEmpty(), "Residuals must not be empty");
        assertTrue(residuals.size() <= data.size(), "Residuals must not exceed data size");
        for (double r : residuals) {
            assertFalse(Double.isNaN(r), "Residual must not be NaN");
        }
    }

    @ParameterizedTest(name = "ARIMA({0},{1},{2}) innovation variance is non-negative")
    @MethodSource("arimaOrders")
    public void arimaInnovationVarianceIsNonNegative(int p, int d, int q) {
        List<Double> data = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            data.add((double) i + Math.sin(i * 0.5));
        }
        ARIMA model = new ARIMA(p, d, q).fit(data);
        assertTrue(model.getInnovationVariance() >= 0.0);
    }
}
