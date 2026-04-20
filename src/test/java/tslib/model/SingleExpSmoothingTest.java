package tslib.model;

import tslib.model.expsmoothing.SingleExpSmoothing;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class SingleExpSmoothingTest {

    @Test
    public void forecastNISTData() {
        List<Double> y = Arrays.asList(
                362.0, 385.0, 432.0, 341.0, 382.0, 409.0, 498.0, 387.0, 473.0, 513.0, 582.0, 474.0,
                544.0, 582.0, 681.0, 557.0, 628.0, 707.0, 773.0, 592.0, 627.0, 725.0, 854.0, 661.0
        );

        int forecastSteps = 4;
        double alpha = 0.5;

        // Instantiate the smoothing model
        SingleExpSmoothing ses = new SingleExpSmoothing(alpha);
        List<Double> prediction = ses.forecast(y, forecastSteps);

        // Basic assertions
        assertEquals(y.size() + forecastSteps, prediction.size());

        // Ensure that forecasted values equal the last smoothed value
        double lastSmoothed = prediction.get(y.size() - 1);
        for (int i = y.size(); i < prediction.size(); i++) {
            assertEquals(lastSmoothed, prediction.get(i), 1e-6);
        }

        // Print forecast portion for manual inspection (optional)
        System.out.println("Forecast: " + prediction.subList(y.size(), y.size() + forecastSteps));
    }
}
