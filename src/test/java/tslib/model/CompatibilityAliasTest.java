package tslib.model;

import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public class CompatibilityAliasTest {
    @Test
    public void singleSmoothingAliasForecastsThroughCompatibilityPackage() {
        ExponentialSmoothing model = new SingleExpSmoothing(0.5);
        List<Double> out = model.forecast(List.of(10.0, 12.0, 14.0), 2);

        assertEquals(5, out.size());
        assertEquals(10.0, out.get(0), 1e-9);
        assertEquals(11.0, out.get(1), 1e-9);
        assertEquals(12.5, out.get(2), 1e-9);
        assertEquals(12.5, out.get(3), 1e-9);
        assertEquals(12.5, out.get(4), 1e-9);
    }

    @Test
    public void arimaAliasForecastsThroughCompatibilityPackage() {
        ARIMA model = new ARIMA(0, 1, 0);
        List<Double> out = model.forecast(List.of(1.0, 2.0, 3.0, 4.0), 2);

        assertEquals(6, out.size());
        assertEquals(5.0, out.get(4), 1e-6);
        assertEquals(6.0, out.get(5), 1e-6);
    }

}
