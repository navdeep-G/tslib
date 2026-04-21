package tslib.decomposition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * STL-style decomposition with loess smoothing and optional robustness re-weighting.
 *
 * <p>When {@code outerIterations > 0} the algorithm applies Cleveland et al. (1990)
 * robustness re-weighting: after each outer pass the remainder is used to compute
 * bisquare robustness weights that down-weight outliers in the next pass, making the
 * decomposition less sensitive to anomalous observations.
 */
public class STLDecomposition {

    /** Multiplier for the robust bandwidth: h = BISQUARE_H_FACTOR * median(|remainder|). */
    private static final double BISQUARE_H_FACTOR = 6.0;
    private static final double BISQUARE_GUARD = 1e-10;

    private final int period;
    private final int trendWindow;
    private final int seasonalWindow;
    private final int iterations;
    private final int outerIterations;

    public STLDecomposition(int period) {
        this(period, Math.max(7, toOdd(period + 1)), 7, 2, 0);
    }

    public STLDecomposition(int period, int trendWindow, int seasonalWindow, int iterations) {
        this(period, trendWindow, seasonalWindow, iterations, 0);
    }

    public STLDecomposition(int period, int trendWindow, int seasonalWindow, int iterations, int outerIterations) {
        if (period < 2) {
            throw new IllegalArgumentException("Period must be >= 2");
        }
        if (trendWindow < 3 || trendWindow % 2 == 0) {
            throw new IllegalArgumentException("Trend window must be an odd integer >= 3");
        }
        if (seasonalWindow < 3 || seasonalWindow % 2 == 0) {
            throw new IllegalArgumentException("Seasonal window must be an odd integer >= 3");
        }
        if (iterations < 1) {
            throw new IllegalArgumentException("Iterations must be >= 1");
        }
        if (outerIterations < 0) {
            throw new IllegalArgumentException("Outer iterations must be >= 0");
        }
        this.period = period;
        this.trendWindow = trendWindow;
        this.seasonalWindow = seasonalWindow;
        this.iterations = iterations;
        this.outerIterations = outerIterations;
    }

    public Result decompose(List<Double> data) {
        validateData(data);
        int n = data.size();
        double[] values = toArray(data);
        double[] trend = centeredMovingAverage(values, Math.max(period, trendWindow));
        double[] seasonal = new double[n];

        double[] robustnessWeights = null; // null = unit weights (no robustness)

        for (int outer = 0; outer <= outerIterations; outer++) {
            for (int iteration = 0; iteration < iterations; iteration++) {
                double[] detrended = new double[n];
                for (int i = 0; i < n; i++) {
                    detrended[i] = values[i] - trend[i];
                }
                seasonal = estimateSeasonal(detrended, period, seasonalWindow, robustnessWeights);
                double[] deseasonalized = new double[n];
                for (int i = 0; i < n; i++) {
                    deseasonalized[i] = values[i] - seasonal[i];
                }
                trend = loessSmooth(deseasonalized, trendWindow, robustnessWeights);
            }

            if (outer < outerIterations) {
                double[] remainder = new double[n];
                for (int i = 0; i < n; i++) {
                    remainder[i] = values[i] - trend[i] - seasonal[i];
                }
                robustnessWeights = computeRobustnessWeights(remainder);
            }
        }

        double[] remainder = new double[n];
        for (int i = 0; i < n; i++) {
            remainder[i] = values[i] - trend[i] - seasonal[i];
        }

        return new Result(toList(trend), toList(seasonal), toList(remainder));
    }

    private double[] computeRobustnessWeights(double[] remainder) {
        double[] absRemainder = new double[remainder.length];
        for (int i = 0; i < remainder.length; i++) {
            absRemainder[i] = Math.abs(remainder[i]);
        }
        double median = computeMedian(absRemainder);
        double h = BISQUARE_H_FACTOR * median + BISQUARE_GUARD;

        double[] weights = new double[remainder.length];
        for (int i = 0; i < remainder.length; i++) {
            double u = absRemainder[i] / h;
            if (u < 1.0) {
                double base = 1.0 - u * u;
                weights[i] = base * base;
            } else {
                weights[i] = 0.0;
            }
        }
        return weights;
    }

    private double computeMedian(double[] values) {
        double[] sorted = Arrays.copyOf(values, values.length);
        Arrays.sort(sorted);
        int mid = sorted.length / 2;
        if (sorted.length % 2 == 0) {
            return (sorted[mid - 1] + sorted[mid]) / 2.0;
        }
        return sorted[mid];
    }

    private double[] estimateSeasonal(double[] detrended, int period, int window, double[] robustnessWeights) {
        int n = detrended.length;
        double[] seasonal = new double[n];
        double[] cycleMeans = new double[period];

        for (int offset = 0; offset < period; offset++) {
            List<Double> subseries = new ArrayList<>();
            List<Integer> indices = new ArrayList<>();
            for (int i = offset; i < n; i += period) {
                subseries.add(detrended[i]);
                indices.add(i);
            }

            double[] subWeights = null;
            if (robustnessWeights != null) {
                subWeights = new double[subseries.size()];
                for (int i = 0; i < indices.size(); i++) {
                    subWeights[i] = robustnessWeights[indices.get(i)];
                }
            }

            double[] smoothed = loessSmooth(toArray(subseries), Math.min(window, toOdd(subseries.size())), subWeights);
            for (int i = 0; i < smoothed.length; i++) {
                seasonal[indices.get(i)] = smoothed[i];
                cycleMeans[offset] += smoothed[i];
            }
            if (!subseries.isEmpty()) {
                cycleMeans[offset] /= subseries.size();
            }
        }

        double grandMean = 0.0;
        for (double value : cycleMeans) {
            grandMean += value;
        }
        grandMean /= period;

        for (int i = 0; i < n; i++) {
            seasonal[i] -= grandMean;
        }
        return seasonal;
    }

    private double[] centeredMovingAverage(double[] values, int window) {
        return loessSmooth(values, toOdd(window), null);
    }

    private double[] loessSmooth(double[] values, int window) {
        return loessSmooth(values, window, null);
    }

    private double[] loessSmooth(double[] values, int window, double[] robustnessWeights) {
        if (values.length == 0) {
            return new double[0];
        }
        if (values.length == 1) {
            return new double[]{values[0]};
        }
        window = Math.max(3, Math.min(toOdd(window), toOdd(values.length)));
        double[] smoothed = new double[values.length];
        int half = window / 2;

        for (int i = 0; i < values.length; i++) {
            int left = Math.max(0, i - half);
            int right = Math.min(values.length - 1, i + half);
            if ((right - left + 1) % 2 == 0) {
                if (right < values.length - 1) {
                    right++;
                } else if (left > 0) {
                    left--;
                }
            }

            double maxDistance = Math.max(1.0, Math.max(i - left, right - i));
            double sw = 0.0;
            double sx = 0.0;
            double sy = 0.0;
            double sxx = 0.0;
            double sxy = 0.0;

            for (int j = left; j <= right; j++) {
                double distance = Math.abs(j - i) / maxDistance;
                double weight = tricube(distance);
                if (robustnessWeights != null) {
                    weight *= robustnessWeights[j];
                }
                double x = j;
                double y = values[j];
                sw += weight;
                sx += weight * x;
                sy += weight * y;
                sxx += weight * x * x;
                sxy += weight * x * y;
            }

            if (sw == 0.0) {
                smoothed[i] = values[i];
                continue;
            }

            double denominator = sw * sxx - sx * sx;
            if (Math.abs(denominator) < 1e-12) {
                smoothed[i] = sy / sw;
            } else {
                double beta = (sw * sxy - sx * sy) / denominator;
                double alpha = (sy - beta * sx) / sw;
                smoothed[i] = alpha + beta * i;
            }
        }
        return smoothed;
    }

    private double tricube(double x) {
        if (x >= 1.0) {
            return 0.0;
        }
        double base = 1.0 - x * x * x;
        return base * base * base;
    }

    private static int toOdd(int value) {
        if (value < 3) {
            return 3;
        }
        return value % 2 == 0 ? value + 1 : value;
    }

    private double[] toArray(List<Double> values) {
        double[] result = new double[values.size()];
        for (int i = 0; i < values.size(); i++) {
            result[i] = values.get(i);
        }
        return result;
    }

    private List<Double> toList(double[] values) {
        List<Double> result = new ArrayList<>(values.length);
        for (double value : values) {
            result.add(value);
        }
        return result;
    }

    private void validateData(List<Double> data) {
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("Input data must not be null or empty.");
        }
        if (data.size() < period * 2) {
            throw new IllegalArgumentException("STL decomposition needs at least two seasonal cycles");
        }
    }

    public static final class Result {
        private final List<Double> trend;
        private final List<Double> seasonal;
        private final List<Double> remainder;

        private Result(List<Double> trend, List<Double> seasonal, List<Double> remainder) {
            this.trend = Collections.unmodifiableList(trend);
            this.seasonal = Collections.unmodifiableList(seasonal);
            this.remainder = Collections.unmodifiableList(remainder);
        }

        public List<Double> getTrend() {
            return trend;
        }

        public List<Double> getSeasonal() {
            return seasonal;
        }

        public List<Double> getRemainder() {
            return remainder;
        }

        public List<Double> reconstruct() {
            List<Double> reconstructed = new ArrayList<>(trend.size());
            for (int i = 0; i < trend.size(); i++) {
                reconstructed.add(trend.get(i) + seasonal.get(i) + remainder.get(i));
            }
            return reconstructed;
        }
    }
}
