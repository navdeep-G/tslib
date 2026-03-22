package tslib.model.arima;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import tslib.transform.Differencing;

/**
 * A lightweight ARIMA(p, d, q) implementation using iterative conditional least squares.
 *
 * This implementation is intentionally small and dependency-light. It supports manual order
 * selection and is designed for straightforward forecasting workflows inside this library.
 */
public class ARIMA {

    private static final double DEFAULT_RIDGE = 1e-6;
    private static final int DEFAULT_MAX_ITERATIONS = 200;
    private static final double DEFAULT_TOLERANCE = 1e-8;

    private final int p;
    private final int d;
    private final int q;
    private final int maxIterations;
    private final double tolerance;

    private boolean fitted;
    private double intercept;
    private double[] arCoefficients;
    private double[] maCoefficients;
    private double[] residuals;
    private double[] fittedDifferenced;
    private double innovationVariance;
    private List<Double> originalData;
    private List<Double> differencedData;
    private List<Double> fittedSeries;

    public ARIMA(int p, int d, int q) {
        this(p, d, q, DEFAULT_MAX_ITERATIONS, DEFAULT_TOLERANCE);
    }

    public ARIMA(int p, int d, int q, int maxIterations, double tolerance) {
        if (p < 0 || d < 0 || q < 0) {
            throw new IllegalArgumentException("ARIMA orders must be >= 0");
        }
        if (maxIterations < 1) {
            throw new IllegalArgumentException("maxIterations must be >= 1");
        }
        if (tolerance <= 0) {
            throw new IllegalArgumentException("tolerance must be > 0");
        }
        this.p = p;
        this.d = d;
        this.q = q;
        this.maxIterations = maxIterations;
        this.tolerance = tolerance;
        this.arCoefficients = new double[p];
        this.maCoefficients = new double[q];
        this.residuals = new double[0];
        this.fittedDifferenced = new double[0];
    }

    public ARIMA fit(List<Double> data) {
        validateInputData(data);

        this.originalData = new ArrayList<>(data);
        this.differencedData = Differencing.difference(data, d);
        if (differencedData.isEmpty()) {
            throw new IllegalArgumentException("Differencing order is too high for the provided data");
        }

        double[] y = toArray(differencedData);
        int maxLag = Math.max(p, q);
        if (y.length <= maxLag) {
            throw new IllegalArgumentException("Time series is too short for the requested ARIMA order");
        }

        int parameterCount = 1 + p + q;
        double[] params = new double[parameterCount];
        params[0] = mean(y);
        double[] currentResiduals = new double[y.length];

        for (int iteration = 0; iteration < maxIterations; iteration++) {
            RegressionData regressionData = buildRegressionData(y, currentResiduals, maxLag);
            double[] nextParams = ordinaryLeastSquares(regressionData.features, regressionData.targets, DEFAULT_RIDGE);
            double[] nextResiduals = computeResiduals(y, nextParams);

            if (maxAbsDiff(params, nextParams) < tolerance) {
                params = nextParams;
                currentResiduals = nextResiduals;
                break;
            }

            params = nextParams;
            currentResiduals = nextResiduals;
        }

        this.intercept = params[0];
        this.arCoefficients = Arrays.copyOfRange(params, 1, 1 + p);
        this.maCoefficients = Arrays.copyOfRange(params, 1 + p, 1 + p + q);
        this.residuals = computeResiduals(y, params);
        this.fittedDifferenced = computePredictions(y, params);
        this.innovationVariance = meanSquared(this.residuals, maxLag);
        this.fittedSeries = buildFittedSeries();
        this.fitted = true;
        return this;
    }

    /**
     * Fits the model and returns an in-sample fitted series followed by future forecasts.
     */
    public List<Double> forecast(List<Double> data, int steps) {
        fit(data);
        List<Double> result = new ArrayList<>(fittedSeries);
        result.addAll(forecast(steps));
        return result;
    }

    /**
     * Forecasts future values from the fitted model.
     */
    public List<Double> forecast(int steps) {
        requireFitted();
        if (steps < 0) {
            throw new IllegalArgumentException("Steps must be >= 0");
        }
        if (steps == 0) {
            return new ArrayList<>();
        }

        List<Double> yHistory = new ArrayList<>(differencedData);
        List<Double> residualHistory = toList(residuals);
        List<Double> forecastedDifferenced = new ArrayList<>(steps);

        for (int h = 0; h < steps; h++) {
            double prediction = intercept;
            for (int i = 0; i < p; i++) {
                int index = yHistory.size() - 1 - i;
                if (index >= 0) {
                    prediction += arCoefficients[i] * yHistory.get(index);
                }
            }
            for (int j = 0; j < q; j++) {
                int index = residualHistory.size() - 1 - j;
                if (index >= 0) {
                    prediction += maCoefficients[j] * residualHistory.get(index);
                }
            }

            forecastedDifferenced.add(prediction);
            yHistory.add(prediction);
            residualHistory.add(0.0);
        }

        return Differencing.inverseDifference(forecastedDifferenced, originalData, d);
    }

    public List<Double> getFittedSeries() {
        requireFitted();
        return new ArrayList<>(fittedSeries);
    }

    public List<Double> getResiduals() {
        requireFitted();
        return toList(residuals);
    }

    public double[] getArCoefficients() {
        requireFitted();
        return Arrays.copyOf(arCoefficients, arCoefficients.length);
    }

    public double[] getMaCoefficients() {
        requireFitted();
        return Arrays.copyOf(maCoefficients, maCoefficients.length);
    }

    public double getIntercept() {
        requireFitted();
        return intercept;
    }

    public double getInnovationVariance() {
        requireFitted();
        return innovationVariance;
    }

    public int getP() {
        return p;
    }

    public int getD() {
        return d;
    }

    public int getQ() {
        return q;
    }

    private List<Double> buildFittedSeries() {
        List<Double> fittedOriginal = new ArrayList<>(originalData);
        int maxLag = Math.max(p, q);

        for (int diffIndex = maxLag; diffIndex < fittedDifferenced.length; diffIndex++) {
            int originalIndex = diffIndex + d;
            if (originalIndex >= originalData.size()) {
                break;
            }
            if (d == 0) {
                fittedOriginal.set(originalIndex, fittedDifferenced[diffIndex]);
                continue;
            }
            List<Double> history = originalData.subList(0, originalIndex);
            double restored = Differencing.inverseDifference(List.of(fittedDifferenced[diffIndex]), history, d).get(0);
            fittedOriginal.set(originalIndex, restored);
        }
        return fittedOriginal;
    }

    private RegressionData buildRegressionData(double[] y, double[] currentResiduals, int maxLag) {
        int rows = y.length - maxLag;
        int columns = 1 + p + q;
        double[][] features = new double[rows][columns];
        double[] targets = new double[rows];

        for (int row = 0; row < rows; row++) {
            int t = row + maxLag;
            int column = 0;
            features[row][column++] = 1.0;
            for (int i = 1; i <= p; i++) {
                features[row][column++] = y[t - i];
            }
            for (int j = 1; j <= q; j++) {
                features[row][column++] = currentResiduals[t - j];
            }
            targets[row] = y[t];
        }

        return new RegressionData(features, targets);
    }

    private double[] computePredictions(double[] y, double[] params) {
        double[] predictions = new double[y.length];
        double[] stateResiduals = new double[y.length];
        for (int t = 0; t < y.length; t++) {
            double prediction = params[0];
            for (int i = 0; i < p; i++) {
                int index = t - 1 - i;
                if (index >= 0) {
                    prediction += params[1 + i] * y[index];
                }
            }
            for (int j = 0; j < q; j++) {
                int index = t - 1 - j;
                if (index >= 0) {
                    prediction += params[1 + p + j] * stateResiduals[index];
                }
            }
            predictions[t] = prediction;
            stateResiduals[t] = y[t] - prediction;
        }
        return predictions;
    }

    private double[] computeResiduals(double[] y, double[] params) {
        double[] predictions = computePredictions(y, params);
        double[] result = new double[y.length];
        for (int i = 0; i < y.length; i++) {
            result[i] = y[i] - predictions[i];
        }
        return result;
    }

    private double[] ordinaryLeastSquares(double[][] x, double[] y, double ridge) {
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

    private double[] solveLinearSystem(double[][] matrix, double[] vector) {
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

    private void swapRows(double[][] matrix, int i, int j) {
        if (i == j) {
            return;
        }
        double[] tmp = matrix[i];
        matrix[i] = matrix[j];
        matrix[j] = tmp;
    }

    private double mean(double[] values) {
        double total = 0.0;
        for (double value : values) {
            total += value;
        }
        return total / values.length;
    }

    private double meanSquared(double[] values, int skip) {
        if (values.length <= skip) {
            return 0.0;
        }
        double total = 0.0;
        for (int i = skip; i < values.length; i++) {
            total += values[i] * values[i];
        }
        return total / (values.length - skip);
    }

    private double maxAbsDiff(double[] a, double[] b) {
        double max = 0.0;
        for (int i = 0; i < a.length; i++) {
            max = Math.max(max, Math.abs(a[i] - b[i]));
        }
        return max;
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

    private void validateInputData(List<Double> data) {
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("Input data must not be null or empty.");
        }
        int minimumSize = Math.max(3, d + Math.max(p, q) + 1);
        if (data.size() < minimumSize) {
            throw new IllegalArgumentException("Time series is too short for the requested ARIMA order");
        }
    }

    private void requireFitted() {
        if (!fitted) {
            throw new IllegalStateException("Model must be fitted before calling this method");
        }
    }

    private static final class RegressionData {
        private final double[][] features;
        private final double[] targets;

        private RegressionData(double[][] features, double[] targets) {
            this.features = features;
            this.targets = targets;
        }
    }
}
