package tslib.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared numerical utilities for ARIMA-family models.
 * Centralises ridge-regression, Gaussian elimination, and array conversions
 * that were previously duplicated across ARIMA, SARIMA, and ARIMAX.
 */
public final class LinearAlgebra {

    private LinearAlgebra() {}

    public static double[] ordinaryLeastSquares(double[][] x, double[] y, double ridge) {
        int rows = x.length;
        int cols = x[0].length;
        double[][] xtx = new double[cols][cols];
        double[] xty = new double[cols];

        for (int r = 0; r < rows; r++) {
            for (int i = 0; i < cols; i++) {
                xty[i] += x[r][i] * y[r];
                for (int j = 0; j < cols; j++) {
                    xtx[i][j] += x[r][i] * x[r][j];
                }
            }
        }

        for (int i = 0; i < cols; i++) {
            xtx[i][i] += ridge;
        }

        return solveLinearSystem(xtx, xty);
    }

    public static double[] solveLinearSystem(double[][] matrix, double[] vector) {
        int n = vector.length;
        double[][] augmented = new double[n][n + 1];
        for (int i = 0; i < n; i++) {
            System.arraycopy(matrix[i], 0, augmented[i], 0, n);
            augmented[i][n] = vector[i];
        }

        for (int pivot = 0; pivot < n; pivot++) {
            int bestRow = pivot;
            for (int row = pivot + 1; row < n; row++) {
                if (Math.abs(augmented[row][pivot]) > Math.abs(augmented[bestRow][pivot])) {
                    bestRow = row;
                }
            }
            swapRows(augmented, pivot, bestRow);

            double pivotValue = augmented[pivot][pivot];
            if (Math.abs(pivotValue) < 1e-12) {
                pivotValue = pivotValue >= 0 ? 1e-12 : -1e-12;
                augmented[pivot][pivot] = pivotValue;
            }

            for (int row = pivot + 1; row < n; row++) {
                double factor = augmented[row][pivot] / pivotValue;
                for (int col = pivot; col <= n; col++) {
                    augmented[row][col] -= factor * augmented[pivot][col];
                }
            }
        }

        double[] solution = new double[n];
        for (int row = n - 1; row >= 0; row--) {
            double sum = augmented[row][n];
            for (int col = row + 1; col < n; col++) {
                sum -= augmented[row][col] * solution[col];
            }
            solution[row] = sum / augmented[row][row];
        }
        return solution;
    }

    public static double mean(double[] values) {
        double total = 0.0;
        for (double value : values) {
            total += value;
        }
        return total / values.length;
    }

    public static double meanSquared(double[] values, int skip) {
        if (values.length <= skip) {
            return 0.0;
        }
        double total = 0.0;
        for (int i = skip; i < values.length; i++) {
            total += values[i] * values[i];
        }
        return total / (values.length - skip);
    }

    public static double maxAbsDiff(double[] a, double[] b) {
        double max = 0.0;
        for (int i = 0; i < a.length; i++) {
            max = Math.max(max, Math.abs(a[i] - b[i]));
        }
        return max;
    }

    public static double[] toArray(List<Double> values) {
        double[] result = new double[values.size()];
        for (int i = 0; i < values.size(); i++) {
            result[i] = values.get(i);
        }
        return result;
    }

    public static List<Double> toList(double[] values) {
        List<Double> result = new ArrayList<>(values.length);
        for (double value : values) {
            result.add(value);
        }
        return result;
    }

    private static void swapRows(double[][] matrix, int i, int j) {
        if (i == j) {
            return;
        }
        double[] tmp = matrix[i];
        matrix[i] = matrix[j];
        matrix[j] = tmp;
    }
}
