package tslib.evaluation;

import java.util.List;

@FunctionalInterface
public interface ForecastFunction {
    /**
     * Returns either horizon future forecasts, or an in-sample series followed by the horizon forecasts.
     */
    List<Double> forecast(List<Double> trainingData, int horizon);
}
