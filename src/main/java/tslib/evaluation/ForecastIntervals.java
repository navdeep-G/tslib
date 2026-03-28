package tslib.evaluation;

import java.util.ArrayList;
import java.util.List;
import tslib.math.Probability;

/**
 * Helpers for normal-approximation prediction intervals.
 */
public final class ForecastIntervals {

    private ForecastIntervals() {}

    public static List<PredictionInterval> normalIntervals(
            List<Double> forecasts,
            double baseVariance,
            double confidenceLevel,
            boolean increasingUncertainty) {
        if (baseVariance < 0.0) {
            throw new IllegalArgumentException("Base variance must be >= 0");
        }
        if (!(confidenceLevel > 0.0 && confidenceLevel < 1.0)) {
            throw new IllegalArgumentException("Confidence level must be in (0, 1)");
        }
        double z = Probability.inverseNormalCdf(0.5 + confidenceLevel / 2.0);
        List<PredictionInterval> result = new ArrayList<>(forecasts.size());
        for (int i = 0; i < forecasts.size(); i++) {
            int step = i + 1;
            double varianceScale = increasingUncertainty ? step : (1.0 + 0.25 * i);
            double variance = Math.max(1e-12, baseVariance) * varianceScale;
            double std = Math.sqrt(variance);
            double point = forecasts.get(i);
            result.add(new PredictionInterval(step, point, point - z * std, point + z * std, confidenceLevel));
        }
        return result;
    }

    public static List<PredictionInterval> normalIntervals(
            List<Double> forecasts,
            List<Double> variances,
            double confidenceLevel) {
        if (forecasts.size() != variances.size()) {
            throw new IllegalArgumentException("Forecast and variance lengths must match");
        }
        double z = Probability.inverseNormalCdf(0.5 + confidenceLevel / 2.0);
        List<PredictionInterval> result = new ArrayList<>(forecasts.size());
        for (int i = 0; i < forecasts.size(); i++) {
            double point = forecasts.get(i);
            double std = Math.sqrt(Math.max(1e-12, variances.get(i)));
            result.add(new PredictionInterval(i + 1, point, point - z * std, point + z * std, confidenceLevel));
        }
        return result;
    }

    public static IntervalForecast wrap(List<Double> forecasts, List<PredictionInterval> intervals) {
        return new IntervalForecast(forecasts, intervals);
    }
}
