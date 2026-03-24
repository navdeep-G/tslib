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


    @Test
    public void sarimaAliasForecastsThroughCompatibilityPackage() {
        SARIMA model = new SARIMA(0, 0, 0, 0, 1, 0, 4);
        List<Double> out = model.forecast(List.of(
                10.0, 20.0, 30.0, 40.0,
                11.0, 21.0, 31.0, 41.0,
                12.0, 22.0, 32.0, 42.0), 2);

        assertEquals(14, out.size());
        assertEquals(13.0, out.get(12), 1e-3);
        assertEquals(23.0, out.get(13), 1e-3);
    }


    @Test
    public void arimaxAliasForecastsThroughCompatibilityPackage() {
        ARIMAX model = new ARIMAX(0, 0, 0).fit(
                List.of(7.0, 9.0, 11.0, 13.0),
                new double[][] {{1.0}, {2.0}, {3.0}, {4.0}});

        List<Double> forecast = model.forecast(new double[][] {{5.0}, {6.0}});
        assertEquals(2, forecast.size());
        assertEquals(15.0, forecast.get(0), 1.0);
    }

    @Test
    public void localLevelAliasExposesStatespaceModel() {
        LocalLevelModel model = new LocalLevelModel().fit(List.of(10.0, 10.1, 9.9, 10.0));

        List<Double> forecast = model.forecast(2);
        assertEquals(2, forecast.size());
        assertEquals(model.getFilteredStates().get(model.getFilteredStates().size() - 1), forecast.get(0), 1e-9);
    }

}
