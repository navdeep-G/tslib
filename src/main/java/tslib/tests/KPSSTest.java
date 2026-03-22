package tslib.tests;

import java.util.List;

/**
 * KPSS stationarity test with level and trend options.
 */
public class KPSSTest {

    public enum RegressionType {
        LEVEL,
        TREND
    }

    private final List<Double> data;
    private final RegressionType regressionType;
    private final int lags;
    private final double statistic;

    public KPSSTest(List<Double> data) {
        this(data, RegressionType.LEVEL, automaticLag(data));
    }

    public KPSSTest(List<Double> data, RegressionType regressionType) {
        this(data, regressionType, automaticLag(data));
    }

    public KPSSTest(List<Double> data, RegressionType regressionType, int lags) {
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("Input data must not be null or empty.");
        }
        if (lags < 0) {
            throw new IllegalArgumentException("Lags must be >= 0");
        }
        this.data = data;
        this.regressionType = regressionType;
        this.lags = lags;
        this.statistic = computeStatistic();
    }

    public double getStatistic() {
        return statistic;
    }

    public int getLags() {
        return lags;
    }

    public RegressionType getRegressionType() {
        return regressionType;
    }

    public boolean isStationaryAtFivePercent() {
        return statistic < getCriticalValue(0.05);
    }

    public boolean isStationaryAtOnePercent() {
        return statistic < getCriticalValue(0.01);
    }

    public double getCriticalValue(double alpha) {
        if (regressionType == RegressionType.LEVEL) {
            if (alpha <= 0.01) {
                return 0.739;
            }
            if (alpha <= 0.025) {
                return 0.574;
            }
            if (alpha <= 0.05) {
                return 0.463;
            }
            return 0.347;
        }

        if (alpha <= 0.01) {
            return 0.216;
        }
        if (alpha <= 0.025) {
            return 0.176;
        }
        if (alpha <= 0.05) {
            return 0.146;
        }
        return 0.119;
    }

    private double computeStatistic() {
        int n = data.size();
        double[] residuals = regressionType == RegressionType.LEVEL
                ? levelResiduals(data)
                : trendResiduals(data);

        double cumulative = 0.0;
        double partialSumSquares = 0.0;
        for (double residual : residuals) {
            cumulative += residual;
            partialSumSquares += cumulative * cumulative;
        }

        double longRunVariance = longRunVariance(residuals, lags);
        if (longRunVariance <= 0.0) {
            return 0.0;
        }
        return partialSumSquares / (n * n * longRunVariance);
    }

    private double[] levelResiduals(List<Double> data) {
        double mean = 0.0;
        for (double value : data) {
            mean += value;
        }
        mean /= data.size();

        double[] residuals = new double[data.size()];
        for (int i = 0; i < data.size(); i++) {
            residuals[i] = data.get(i) - mean;
        }
        return residuals;
    }

    private double[] trendResiduals(List<Double> data) {
        int n = data.size();
        double sumX = 0.0;
        double sumY = 0.0;
        double sumXX = 0.0;
        double sumXY = 0.0;
        for (int i = 0; i < n; i++) {
            double x = i + 1;
            double y = data.get(i);
            sumX += x;
            sumY += y;
            sumXX += x * x;
            sumXY += x * y;
        }

        double slope = (n * sumXY - sumX * sumY) / (n * sumXX - sumX * sumX);
        double intercept = (sumY - slope * sumX) / n;

        double[] residuals = new double[n];
        for (int i = 0; i < n; i++) {
            residuals[i] = data.get(i) - (intercept + slope * (i + 1));
        }
        return residuals;
    }

    private double longRunVariance(double[] residuals, int lags) {
        int n = residuals.length;
        double gamma0 = 0.0;
        for (double residual : residuals) {
            gamma0 += residual * residual;
        }
        gamma0 /= n;

        double total = gamma0;
        for (int lag = 1; lag <= lags; lag++) {
            double covariance = 0.0;
            for (int t = lag; t < n; t++) {
                covariance += residuals[t] * residuals[t - lag];
            }
            covariance /= n;
            double weight = 1.0 - (lag / (lags + 1.0));
            total += 2.0 * weight * covariance;
        }
        return Math.max(total, 1e-12);
    }

    private static int automaticLag(List<Double> data) {
        int n = Math.max(1, data.size());
        return Math.max(1, (int) Math.floor(12.0 * Math.pow(n / 100.0, 0.25)));
    }
}
