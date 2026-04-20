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

public class SARIMATest {

    @Test
    public void seasonalDifferencingModelProjectsNextSeasonalCycle() {
        List<Double> data = List.of(
                10.0, 20.0, 30.0, 40.0,
                11.0, 21.0, 31.0, 41.0,
                12.0, 22.0, 32.0, 42.0);

        SARIMA model = new SARIMA(0, 0, 0, 0, 1, 0, 4).fit(data);
        List<Double> forecast = model.forecast(4);

        assertEquals(4, forecast.size());
        assertEquals(13.0, forecast.get(0), 1e-3);
        assertEquals(23.0, forecast.get(1), 1e-3);
        assertEquals(33.0, forecast.get(2), 1e-3);
        assertEquals(43.0, forecast.get(3), 1e-3);
    }

    private static List<Double> seasonalData(int periods, int m) {
        double[] pattern = new double[m];
        for (int i = 0; i < m; i++) {
            pattern[i] = 10.0 + i * 5.0;
        }
        List<Double> data = new ArrayList<>();
        for (int rep = 0; rep < periods; rep++) {
            for (int i = 0; i < m; i++) {
                data.add(pattern[i] + rep * 0.5);
            }
        }
        return data;
    }

    static Stream<Arguments> sarimaCases() {
        return Stream.of(
            // p,d,q, P,D,Q, m, horizon
            Arguments.of(0, 0, 0,  0, 1, 0,  4, 4),
            Arguments.of(1, 0, 0,  0, 1, 0,  4, 4),
            Arguments.of(0, 1, 0,  0, 1, 0,  4, 4),
            Arguments.of(0, 0, 1,  0, 1, 0,  4, 4),
            Arguments.of(1, 0, 0,  1, 0, 0,  4, 4),
            Arguments.of(0, 0, 0,  0, 1, 0, 12, 12),
            Arguments.of(1, 0, 0,  0, 1, 0, 12, 12),
            Arguments.of(0, 1, 0,  1, 1, 0,  4, 4),
            Arguments.of(1, 1, 0,  0, 1, 0,  4, 4),
            Arguments.of(0, 0, 0,  1, 1, 0,  4, 4)
        );
    }

    @ParameterizedTest(name = "SARIMA({0},{1},{2})({3},{4},{5})[{6}] forecasts {7} finite steps")
    @MethodSource("sarimaCases")
    public void sarimaDifferentOrdersProduceFiniteForecasts(
            int p, int d, int q, int P, int D, int Q, int m, int horizon) {
        List<Double> data = seasonalData(4, m);
        SARIMA model = new SARIMA(p, d, q, P, D, Q, m).fit(data);
        List<Double> forecast = model.forecast(horizon);

        assertEquals(horizon, forecast.size());
        for (int i = 0; i < forecast.size(); i++) {
            assertFalse(Double.isNaN(forecast.get(i)),
                    "Forecast step " + i + " must not be NaN");
            assertFalse(Double.isInfinite(forecast.get(i)),
                    "Forecast step " + i + " must not be infinite");
        }
    }

    @ParameterizedTest(name = "SARIMA({0},{1},{2})({3},{4},{5})[{6}] residuals are non-empty and bounded")
    @MethodSource("sarimaCases")
    public void sarimaResidualsAreNonEmptyAndBounded(
            int p, int d, int q, int P, int D, int Q, int m, int horizon) {
        List<Double> data = seasonalData(4, m);
        SARIMA model = new SARIMA(p, d, q, P, D, Q, m).fit(data);
        List<Double> residuals = model.getResiduals();
        assertFalse(residuals.isEmpty(), "Residuals must not be empty");
        assertTrue(residuals.size() <= data.size(), "Residuals must not exceed data size");
        for (int i = 0; i < residuals.size(); i++) {
            assertFalse(Double.isNaN(residuals.get(i)), "Residual at index " + i + " must not be NaN");
        }
    }
}
