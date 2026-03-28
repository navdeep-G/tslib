package tslib.diagnostics;

import java.util.List;
import tslib.math.Probability;

/**
 * Ljung-Box portmanteau test with approximate chi-square p-values.
 */
public class LjungBoxTest {

    private final List<Double> residuals;
    private final int lags;
    private final int degreesOfFreedomAdjustment;
    private final double statistic;
    private final double pValue;

    public LjungBoxTest(List<Double> residuals, int lags) {
        this(residuals, lags, 0);
    }

    public LjungBoxTest(List<Double> residuals, int lags, int degreesOfFreedomAdjustment) {
        if (residuals == null || residuals.size() < 3) {
            throw new IllegalArgumentException("Residuals must contain at least 3 points");
        }
        if (lags < 1) {
            throw new IllegalArgumentException("Lags must be >= 1");
        }
        this.residuals = residuals;
        this.lags = Math.min(lags, residuals.size() - 1);
        this.degreesOfFreedomAdjustment = Math.max(0, degreesOfFreedomAdjustment);
        this.statistic = computeStatistic();
        this.pValue = approximatePValue(statistic, Math.max(1, this.lags - this.degreesOfFreedomAdjustment));
    }

    public double getStatistic() {
        return statistic;
    }

    public double getPValue() {
        return pValue;
    }

    public boolean rejectsAtFivePercent() {
        return pValue < 0.05;
    }

    private double computeStatistic() {
        int n = residuals.size();
        double total = 0.0;
        for (int lag = 1; lag <= lags; lag++) {
            double rho = autocorrelation(residuals, lag);
            total += rho * rho / (n - lag);
        }
        return n * (n + 2.0) * total;
    }

    private double autocorrelation(List<Double> values, int lag) {
        double mean = 0.0;
        for (double value : values) {
            mean += value;
        }
        mean /= values.size();

        double numerator = 0.0;
        double denominator = 0.0;
        for (int i = 0; i < values.size(); i++) {
            double centered = values.get(i) - mean;
            denominator += centered * centered;
            if (i >= lag) {
                numerator += centered * (values.get(i - lag) - mean);
            }
        }
        if (denominator <= 1e-12) {
            return 0.0;
        }
        return numerator / denominator;
    }

    private double approximatePValue(double x, int k) {
        if (x <= 0.0) {
            return 1.0;
        }
        double z = (Math.pow(x / k, 1.0 / 3.0) - (1.0 - 2.0 / (9.0 * k))) / Math.sqrt(2.0 / (9.0 * k));
        return 1.0 - Probability.normalCdf(z);
    }
}
