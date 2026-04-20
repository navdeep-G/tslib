package tslib.evaluation;

import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ForecastMetricsTest {
    @Test
    public void computesCommonMetrics() {
        List<Double> actual = List.of(10.0, 12.0, 14.0);
        List<Double> forecast = List.of(9.0, 12.0, 15.0);

        assertEquals(2.0 / 3.0, ForecastMetrics.mae(actual, forecast), 1e-9);
        assertEquals(Math.sqrt(2.0 / 3.0), ForecastMetrics.rmse(actual, forecast), 1e-9);
        assertTrue(ForecastMetrics.mape(actual, forecast) > 0.0);
        assertTrue(ForecastMetrics.smape(actual, forecast) > 0.0);
        assertTrue(ForecastMetrics.mase(actual, forecast, List.of(8.0, 10.0, 12.0, 14.0), 1) > 0.0);
    }
}
