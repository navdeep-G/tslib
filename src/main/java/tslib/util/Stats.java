package tslib.util;

import java.util.List;
import org.apache.commons.math3.linear.*;

/**
 * Collect relevant statistics about a time series.
 * Optimized for performance with single-pass algorithms where possible.
 */
public class Stats {

    public static double average(List<Double> data) {
        validateNonEmpty(data);
        double total = 0;
        for (double item : data) {
            total += item;
        }
        return total / data.size();
    }

    public static double variance(List<Double> data) {
        validateNonEmpty(data);
        double avg = average(data);
        double total = 0;
        int n = data.size();
        if (n == 1) return 0.0;

        for (double item : data) {
            total += (item - avg) * (item - avg);
        }
        return total / (n - 1);
    }

    public static double standardDeviation(List<Double> data) {
        return Math.sqrt(variance(data));
    }

    public static int getMinimumIndex(List<Double> data) {
        validateNonEmpty(data);
        int minIndex = 0;
        double min = data.get(0);
        for (int i = 1; i < data.size(); i++) {
            double value = data.get(i);
            if (value < min) {
                min = value;
                minIndex = i;
            }
        }
        return minIndex;
    }

    public static int getMaximumIndex(List<Double> data) {
        validateNonEmpty(data);
        int maxIndex = 0;
        double max = data.get(0);
        for (int i = 1; i < data.size(); i++) {
            double value = data.get(i);
            if (value > max) {
                max = value;
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    public static double getMinimum(List<Double> data) {
        validateNonEmpty(data);
        double min = data.get(0);
        for (int i = 1; i < data.size(); i++) {
            double value = data.get(i);
            if (value < min) min = value;
        }
        return min;
    }

    public static double getMaximum(List<Double> data) {
        validateNonEmpty(data);
        double max = data.get(0);
        for (int i = 1; i < data.size(); i++) {
            double value = data.get(i);
            if (value > max) max = value;
        }
        return max;
    }

    public static double[] getMinMax(List<Double> data) {
        validateNonEmpty(data);
        double min = data.get(0);
        double max = data.get(0);
        for (int i = 1; i < data.size(); i++) {
            double value = data.get(i);
            if (value < min) min = value;
            if (value > max) max = value;
        }
        return new double[]{min, max};
    }

    public static int[] getMinMaxIndices(List<Double> data) {
        validateNonEmpty(data);
        int minIndex = 0;
        int maxIndex = 0;
        double min = data.get(0);
        double max = data.get(0);
        for (int i = 1; i < data.size(); i++) {
            double value = data.get(i);
            if (value < min) {
                min = value;
                minIndex = i;
            }
            if (value > max) {
                max = value;
                maxIndex = i;
            }
        }
        return new int[]{minIndex, maxIndex};
    }

    public static double getAutoCovariance(List<Double> data, int k) {
        validateNonEmpty(data);
        if (k < 0) throw new IllegalArgumentException("Lag k must be >= 0");

        int n = data.size();
        if (k >= n) return 0;

        double mean = average(data);
        double total = 0;
        for (int i = k; i < n; i++) {
            total += (data.get(i - k) - mean) * (data.get(i) - mean);
        }
        return total / (n - k); // unbiased estimator
    }

    public static double getAutoCorrelation(List<Double> data, int k) {
        double acov = getAutoCovariance(data, k);
        double var = variance(data);
        return (var == 0) ? 0 : acov / var;
    }

    public static double[] getAcf(List<Double> data, int n) {
        validateNonEmpty(data);
        if (n < 0) throw new IllegalArgumentException("Number of lags must be >= 0");
        
        double[] acfValues = new double[n + 1];
        for (int i = 0; i <= n; i++) {
            acfValues[i] = getAutoCorrelation(data, i);
        }
        return acfValues;
    }

    /**
     * Computes PACF using Yule-Walker equations.
     *
     * @param data Time series data
     * @param maxLag Maximum lag to compute PACF
     * @return Array of PACF values from lag 0 to maxLag
     */
    public static double[] getPacf(List<Double> data, int maxLag) {
        validateNonEmpty(data);
        if (maxLag < 0) throw new IllegalArgumentException("Max lag must be >= 0");
        
        double[] pacf = new double[maxLag + 1];
        pacf[0] = 1.0;

        for (int k = 1; k <= maxLag; k++) {
            double[][] matrix = new double[k][k];
            double[] rhs = new double[k];

            for (int i = 0; i < k; i++) {
                rhs[i] = getAutoCorrelation(data, i + 1);
                for (int j = 0; j < k; j++) {
                    matrix[i][j] = getAutoCorrelation(data, Math.abs(i - j));
                }
            }

            try {
                RealMatrix A = new Array2DRowRealMatrix(matrix);
                RealVector b = new ArrayRealVector(rhs);
                RealVector phi = new LUDecomposition(A).getSolver().solve(b);
                pacf[k] = phi.getEntry(k - 1);
            } catch (Exception e) {
                pacf[k] = Double.NaN;
            }
        }

        return pacf;
    }

    private static void validateNonEmpty(List<Double> data) {
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("Input data must not be null or empty.");
        }
    }
}
