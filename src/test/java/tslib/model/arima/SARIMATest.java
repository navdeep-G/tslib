package tslib.model.arima;

import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

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
}
