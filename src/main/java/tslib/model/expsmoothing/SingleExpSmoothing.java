package tslib.model.expsmoothing;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import tslib.evaluation.ForecastIntervals;
import tslib.evaluation.IntervalForecast;

public class SingleExpSmoothing implements ExponentialSmoothing, Serializable {

    private static final long serialVersionUID = 1L;

    private final double alpha;

    private boolean fitted;
    private double lastSmoothed;
    private List<Double> residuals;

    public SingleExpSmoothing(double alpha) {
        if (alpha <= 0 || alpha > 1) {
            throw new IllegalArgumentException("Alpha must be in (0, 1]");
        }
        this.alpha = alpha;
    }

    @Override
    public SingleExpSmoothing fit(List<Double> data) {
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("Input data must not be null or empty.");
        }
        int n = data.size();
        double smoothed = data.get(0);
        List<Double> res = new ArrayList<>(n - 1);
        for (int i = 1; i < n; i++) {
            res.add(data.get(i) - smoothed);
            smoothed = alpha * data.get(i) + (1 - alpha) * smoothed;
        }
        this.lastSmoothed = smoothed;
        this.residuals = res;
        this.fitted = true;
        return this;
    }

    @Override
    public List<Double> forecast(int steps) {
        requireFitted();
        if (steps < 0) {
            throw new IllegalArgumentException("Steps must be >= 0");
        }
        List<Double> result = new ArrayList<>(steps);
        for (int i = 0; i < steps; i++) {
            result.add(lastSmoothed);
        }
        return result;
    }

    @Override
    public IntervalForecast forecastWithIntervals(int steps, double confidenceLevel) {
        requireFitted();
        List<Double> fc = forecast(steps);
        double mse = residuals.isEmpty() ? 1.0
                : residuals.stream().mapToDouble(e -> e * e).average().orElse(1.0);
        return ForecastIntervals.wrap(fc,
                ForecastIntervals.normalIntervals(fc, mse, confidenceLevel, true));
    }

    @Override
    public List<Double> forecast(List<Double> data, int steps) {
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("Input data must not be null or empty.");
        }
        int n = data.size();
        List<Double> result = new ArrayList<>(n + steps);
        double smoothed = data.get(0);
        result.add(smoothed);
        for (int i = 1; i < n; i++) {
            smoothed = alpha * data.get(i) + (1 - alpha) * smoothed;
            result.add(smoothed);
        }
        for (int i = 0; i < steps; i++) {
            result.add(smoothed);
        }
        return result;
    }

    private void requireFitted() {
        if (!fitted) {
            throw new IllegalStateException("Model must be fitted before forecasting");
        }
    }
}
