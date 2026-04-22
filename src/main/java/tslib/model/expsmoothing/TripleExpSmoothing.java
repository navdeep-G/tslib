package tslib.model.expsmoothing;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import tslib.evaluation.ForecastIntervals;
import tslib.evaluation.IntervalForecast;

public class TripleExpSmoothing implements ExponentialSmoothing, Serializable {

    private static final long serialVersionUID = 1L;

    private final double alpha;
    private final double beta;
    private final double gamma;
    private final int period;

    private boolean fitted;
    private double lastLevel;
    private double lastTrend;
    private List<Double> lastSeasonalIndices;
    private List<Double> residuals;

    public TripleExpSmoothing(double alpha, double beta, double gamma, int period) {
        this(alpha, beta, gamma, period, false);
    }

    public TripleExpSmoothing(double alpha, double beta, double gamma, int period, boolean debug) {
        if (alpha < 0.0 || alpha > 1.0 || beta < 0.0 || beta > 1.0 || gamma < 0.0 || gamma > 1.0) {
            throw new IllegalArgumentException("Smoothing factors must be between 0.0 and 1.0.");
        }
        if (period <= 1) {
            throw new IllegalArgumentException("Period must be > 1.");
        }
        this.alpha = alpha;
        this.beta = beta;
        this.gamma = gamma;
        this.period = period;
    }

    @Override
    public TripleExpSmoothing fit(List<Double> data) {
        if (data == null || data.size() < 2 * period) {
            throw new IllegalArgumentException(
                    "Data must contain at least 2 complete seasonal cycles (2 * period = " + (2 * period) + ").");
        }
        int n = data.size();
        int seasons = n / period;
        double b0 = calculateInitialTrend(data, period);
        List<Double> seasonal = calculateSeasonalIndices(data, period, seasons);

        double[] St = new double[n];
        double[] Bt = new double[n];
        double[] It = new double[n];

        for (int i = 0; i < period && i < n; i++) {
            It[i] = seasonal.get(i);
        }
        for (int i = period; i < n; i++) {
            It[i] = 1.0;
        }
        St[1] = data.get(0);
        Bt[1] = b0;

        List<Double> res = new ArrayList<>();
        for (int i = 2; i < n; i++) {
            if (i - period >= 0) {
                St[i] = alpha * data.get(i) / It[i - period] + (1 - alpha) * (St[i - 1] + Bt[i - 1]);
            } else {
                St[i] = alpha * data.get(i) + (1 - alpha) * (St[i - 1] + Bt[i - 1]);
            }
            Bt[i] = gamma * (St[i] - St[i - 1]) + (1 - gamma) * Bt[i - 1];
            if (i - period >= 0) {
                It[i] = beta * data.get(i) / St[i] + (1 - beta) * It[i - period];
            }
            if (i >= period + 1 && It[i - period] != 0) {
                double onestep = (St[i - 1] + Bt[i - 1]) * It[i - period];
                res.add(data.get(i) - onestep);
            }
        }

        this.lastLevel = St[n - 1];
        this.lastTrend = Bt[n - 1];
        this.lastSeasonalIndices = new ArrayList<>(period);
        for (int i = n - period; i < n; i++) {
            lastSeasonalIndices.add(It[i]);
        }
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
            double seasonal = lastSeasonalIndices.get((h - 1) % period);
            result.add((lastLevel + h * lastTrend) * seasonal);
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
    public List<Double> forecast(List<Double> y, int m) {
        if (y == null || y.isEmpty()) {
            throw new IllegalArgumentException("Input time series must not be null or empty.");
        }
        int n = y.size();
        int seasons = n / period;
        double a0 = y.get(0);
        double b0 = calculateInitialTrend(y, period);
        List<Double> seasonal = calculateSeasonalIndices(y, period, seasons);

        List<Double> St = new ArrayList<>(n);
        List<Double> Bt = new ArrayList<>(n);
        List<Double> It = new ArrayList<>(n);
        List<Double> Ft = new ArrayList<>(n + m);

        for (int i = 0; i < n + m; i++) Ft.add(0.0);
        for (int i = 0; i < n; i++) {
            St.add(0.0);
            Bt.add(0.0);
            It.add(i < period ? seasonal.get(i) : 1.0);
        }

        St.set(1, a0);
        Bt.set(1, b0);

        for (int i = 2; i < n; i++) {
            if (i - period >= 0) {
                St.set(i, alpha * y.get(i) / It.get(i - period) + (1 - alpha) * (St.get(i - 1) + Bt.get(i - 1)));
            } else {
                St.set(i, alpha * y.get(i) + (1 - alpha) * (St.get(i - 1) + Bt.get(i - 1)));
            }
            Bt.set(i, gamma * (St.get(i) - St.get(i - 1)) + (1 - gamma) * Bt.get(i - 1));
            if (i - period >= 0) {
                It.set(i, beta * y.get(i) / St.get(i) + (1 - beta) * It.get(i - period));
            }
            if (i + m < Ft.size() && (i - period + m) >= 0 && (i - period + m) < It.size()) {
                Ft.set(i + m, (St.get(i) + m * Bt.get(i)) * It.get(i - period + m));
            }
        }
        return Ft;
    }

    private static double calculateInitialTrend(List<Double> y, int period) {
        double sum = 0.0;
        for (int i = 0; i < period; i++) {
            sum += (y.get(period + i) - y.get(i));
        }
        return sum / (period * period);
    }

    private static List<Double> calculateSeasonalIndices(List<Double> y, int period, int seasons) {
        double[] seasonalAverages = new double[seasons];
        double[] seasonalIndices = new double[period];
        double[] normalized = new double[y.size()];

        for (int i = 0; i < seasons; i++) {
            for (int j = 0; j < period; j++) {
                seasonalAverages[i] += y.get(i * period + j);
            }
            seasonalAverages[i] /= period;
        }
        for (int i = 0; i < seasons; i++) {
            for (int j = 0; j < period; j++) {
                normalized[i * period + j] = y.get(i * period + j) / seasonalAverages[i];
            }
        }
        for (int i = 0; i < period; i++) {
            for (int j = 0; j < seasons; j++) {
                seasonalIndices[i] += normalized[j * period + i];
            }
            seasonalIndices[i] /= seasons;
        }
        List<Double> result = new ArrayList<>();
        for (double val : seasonalIndices) result.add(val);
        return result;
    }

    private void requireFitted() {
        if (!fitted) {
            throw new IllegalStateException("Model must be fitted before forecasting");
        }
    }
}
