package tslib.model;

import org.junit.jupiter.api.Test;
import tslib.model.expsmoothing.TripleExpSmoothing;

import java.util.List;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class TripleExpSmoothingTest {

    @Test
    public void forecastNISTData() {
        List<Double> y = Arrays.asList(
                362.0, 385.0, 432.0, 341.0, 382.0, 409.0, 498.0, 387.0, 473.0, 513.0, 582.0, 474.0,
                544.0, 582.0, 681.0, 557.0, 628.0, 707.0, 773.0, 592.0, 627.0, 725.0, 854.0, 661.0
        );

        int period = 4;
        int forecastSteps = 4;
        double alpha = 0.5;
        double beta = 0.4;
        double gamma = 0.6;
        boolean debug = false;

        TripleExpSmoothing model = new TripleExpSmoothing(alpha, beta, gamma, period, debug);
        List<Double> forecast = model.forecast(y, forecastSteps);

        // Verify length
        assertEquals(y.size() + forecastSteps, forecast.size());

        // Verify forecast values are positive and not just zero padding
        for (int i = y.size(); i < forecast.size(); i++) {
            assertTrue(forecast.get(i) > 0.0, "Forecasted value should be greater than zero");
        }

        // Optionally print for manual inspection
        System.out.println("Triple Exp Forecast: " + forecast.subList(y.size(), forecast.size()));
    }
}
