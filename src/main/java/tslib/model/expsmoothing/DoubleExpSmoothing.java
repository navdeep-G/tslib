package tslib.model.expsmoothing;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import tslib.evaluation.ForecastIntervals;
import tslib.evaluation.IntervalForecast;

public class DoubleExpSmoothing implements ExponentialSmoothing, Serializable {

    private static final long serialVersionUID = 1L;

    private final double alpha;
    private final double gamma;
    private final int initializationMethod;

    private boolean fitted;
    private double lastLevel;
    private double lastTrend;
    private List<Double> residuals;

    public DoubleExpSmoothing(double alpha, double gamma, int initializationMethod) {
        if (alpha <= 0 || alpha > 1 || gamma <= 0 || gamma > 1) {
            throw new IllegalArgumentException("Alpha and gamma must be in (0, 1].");
        }
        if (initializationMethod < 0 || initializationMethod > 2) {
            throw new IllegalArgumentException("Initialization method must be 0, 1, or 2.");
        }
        this.alpha = alpha;
        this.gamma = gamma;
        this.initializationMethod = initializationMethod;
    }

    @Override
    public DoubleExpSmoothing fit(List<Double> data) {
        if (data == null || data.size() < 2) {
            throw new IllegalArgumentException("Data must contain at least 2 points.");
        }
        int n = data.size();
        double[] s = new double[n];
        double[] b = new double[n];
        s[0] = data.get(0);
        switch (initializationMethod) {
            case 0 -> b[0] = data.get(1) - data.get(0);
            case 1 -> b[0] = (n > 4) ? (data.get(3) - data.get(0)) / 3 : data.get(1) - data.get(0);
            case 2 -> b[0] = (data.get(n - 1) - data.get(0)) / (n - 1);
        }
        List<Double> res = new ArrayList<>(n - 1);
        for (int i = 1; i < n; i++) {
            double predicted = s[i - 1] + b[i - 1];
            res.add(data.get(i) - predicted);
            s[i] = alpha * data.get(i) + (1 - alpha) * predicted;
            b[i] = gamma * (s[i] - s[i - 1]) + (1 - gamma) * b[i - 1];
        }
        this.lastLevel = s[n - 1];
        this.lastTrend = b[n - 1];
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
        for (int h = 1; h <= steps; h++) {
            result.add(lastLevel + h * lastTrend);
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
        if (data == null || data.size() < 2) {
            throw new IllegalArgumentException("Data must contain at least 2 points.");
        }
        int n = data.size();
        double[] y = new double[n + steps];
        double[] s = new double[n];
        double[] b = new double[n];
        s[0] = data.get(0);
        switch (initializationMethod) {
            case 0 -> b[0] = data.get(1) - data.get(0);
            case 1 -> b[0] = (n > 4) ? (data.get(3) - data.get(0)) / 3 : data.get(1) - data.get(0);
            case 2 -> b[0] = (data.get(n - 1) - data.get(0)) / (n - 1);
        }
        y[0] = s[0] + b[0];
        for (int i = 1; i < n; i++) {
            s[i] = alpha * data.get(i) + (1 - alpha) * (s[i - 1] + b[i - 1]);
            b[i] = gamma * (s[i] - s[i - 1]) + (1 - gamma) * b[i - 1];
            y[i] = s[i] + b[i];
        }
        for (int j = 0; j < steps; j++) {
            y[n + j] = s[n - 1] + (j + 1) * b[n - 1];
        }
        List<Double> result = new ArrayList<>(y.length);
        for (double v : y) {
            result.add(v);
        }
        return result;
    }

    private void requireFitted() {
        if (!fitted) {
            throw new IllegalStateException("Model must be fitted before forecasting");
        }
    }
}
