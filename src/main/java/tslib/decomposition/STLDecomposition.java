package tslib.decomposition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A compact STL-style decomposition with loess smoothing for trend extraction.
 */
public class STLDecomposition {

    private final int period;
    private final int trendWindow;
    private final int seasonalWindow;
    private final int iterations;

    public STLDecomposition(int period) {
        this(period, Math.max(7, toOdd(period + 1)), 7, 2);
    }

    public STLDecomposition(int period, int trendWindow, int seasonalWindow, int iterations) {
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
        this.period = period;
        this.trendWindow = trendWindow;
        this.seasonalWindow = seasonalWindow;
        this.iterations = iterations;
    }

    public Result decompose(List<Double> data) {
        validateData(data);
        int n = data.size();
        double[] values = toArray(data);
        double[] trend = centeredMovingAverage(values, Math.max(period, trendWindow));
        double[] seasonal = new double[n];
        double[] remainder = new double[n];

        for (int iteration = 0; iteration < iterations; iteration++) {
            double[] detrended = new double[n];
            for (int i = 0; i < n; i++) {
                detrended[i] = values[i] - trend[i];
            }

            seasonal = estimateSeasonal(detrended, period, seasonalWindow);
            double[] deseasonalized = new double[n];
            for (int i = 0; i < n; i++) {
                deseasonalized[i] = values[i] - seasonal[i];
            }
            trend = loessSmooth(deseasonalized, trendWindow);
        }

        for (int i = 0; i < n; i++) {
            remainder[i] = values[i] - trend[i] - seasonal[i];
        }

        return new Result(toList(trend), toList(seasonal), toList(remainder));
    }

    private double[] estimateSeasonal(double[] detrended, int period, int window) {
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
            double[] smoothed = loessSmooth(toArray(subseries), Math.min(window, toOdd(subseries.size())));
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
        return loessSmooth(values, toOdd(window));
    }

    private double[] loessSmooth(double[] values, int window) {
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
                double x = j;
                double y = values[j];
                sw += weight;
                sx += weight * x;
                sy += weight * y;
                sxx += weight * x * x;
                sxy += weight * x * y;
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
