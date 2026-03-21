package tslib.model.arima;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
}
