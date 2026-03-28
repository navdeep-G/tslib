package tslib.model.arima;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import tslib.transform.Differencing;

/**
 * Basic ARIMAX(p, d, q) model with contemporaneous exogenous regressors.
 */
public class ARIMAX {

    private static final double DEFAULT_RIDGE = 1e-6;
    private static final int DEFAULT_MAX_ITERATIONS = 200;
    private static final double DEFAULT_TOLERANCE = 1e-8;

    private final int p;
    private final int d;
    private final int q;
    private final int maxIterations;
    private final double tolerance;

    private boolean fitted;
    private int exogenousDimension;
    private double intercept;
    private double[] exogenousCoefficients = new double[0];
    private double[] arCoefficients;
    private double[] maCoefficients;
    private double[] residuals;
    private double innovationVariance;
    private List<Double> originalData;
    private List<Double> differencedData;
    private double[][] trainingExogenous;

    public ARIMAX(int p, int d, int q) {
        this(p, d, q, DEFAULT_MAX_ITERATIONS, DEFAULT_TOLERANCE);
    }

    public ARIMAX(int p, int d, int q, int maxIterations, double tolerance) {
        if (p < 0 || d < 0 || q < 0) {
            throw new IllegalArgumentException("Orders must be >= 0");
        }
        this.p = p;
        this.d = d;
        this.q = q;
        this.maxIterations = maxIterations;
        this.tolerance = tolerance;
        this.arCoefficients = new double[p];
        this.maCoefficients = new double[q];
        this.residuals = new double[0];
    }

    public ARIMAX fit(List<Double> data, double[][] exogenous) {
        validateInputs(data, exogenous);
        this.originalData = new ArrayList<>(data);
        this.differencedData = Differencing.difference(data, d);
        if (differencedData.isEmpty()) {
            throw new IllegalArgumentException("Differencing order is too high for the provided data");
        }

        double[] y = toArray(differencedData);
        this.trainingExogenous = alignExogenous(exogenous, d);
        this.exogenousDimension = trainingExogenous[0].length;

        int maxLag = Math.max(p, q);
        if (y.length <= maxLag) {
            throw new IllegalArgumentException("Time series is too short for the requested ARIMAX order");
        }

        int parameterCount = 1 + exogenousDimension + p + q;
        double[] params = new double[parameterCount];
        params[0] = mean(y);
        double[] currentResiduals = new double[y.length];

        for (int iteration = 0; iteration < maxIterations; iteration++) {
            RegressionData regressionData = buildRegressionData(y, trainingExogenous, currentResiduals, maxLag);
            double[] nextParams = ordinaryLeastSquares(regressionData.features, regressionData.targets, DEFAULT_RIDGE);
            double[] nextResiduals = computeResiduals(y, trainingExogenous, nextParams);
            if (maxAbsDiff(params, nextParams) < tolerance) {
                params = nextParams;
                currentResiduals = nextResiduals;
                break;
            }
            params = nextParams;
            currentResiduals = nextResiduals;
        }

        this.intercept = params[0];
        this.exogenousCoefficients = Arrays.copyOfRange(params, 1, 1 + exogenousDimension);
        this.arCoefficients = Arrays.copyOfRange(params, 1 + exogenousDimension, 1 + exogenousDimension + p);
        this.maCoefficients = Arrays.copyOfRange(params, 1 + exogenousDimension + p, parameterCount);
        this.residuals = computeResiduals(y, trainingExogenous, params);
        this.innovationVariance = meanSquared(this.residuals, maxLag);
        this.fitted = true;
        return this;
    }

    public List<Double> forecast(double[][] futureExogenous) {
        requireFit();
        if (futureExogenous == null) {
            throw new IllegalArgumentException("Future exogenous matrix must not be null");
        }
        for (double[] row : futureExogenous) {
            if (row == null || row.length != exogenousDimension) {
                throw new IllegalArgumentException("Each future exogenous row must match the training exogenous dimension");
            }
        }
        List<Double> yHistory = new ArrayList<>(differencedData);
        List<Double> residualHistory = toList(residuals);
        List<Double> forecastedDifferenced = new ArrayList<>(futureExogenous.length);

        for (double[] row : futureExogenous) {
            double prediction = intercept;
            for (int j = 0; j < exogenousDimension; j++) {
                prediction += exogenousCoefficients[j] * row[j];
            }
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

    public double[] getExogenousCoefficients() {
        requireFitted();
        return Arrays.copyOf(exogenousCoefficients, exogenousCoefficients.length);
    }

    public double[] getArCoefficients() {
        requireFitted();
        return Arrays.copyOf(arCoefficients, arCoefficients.length);
    }

    public double[] getMaCoefficients() {
        requireFitted();
        return Arrays.copyOf(maCoefficients, maCoefficients.length);
    }

    public List<Double> getResiduals() {
        requireFitted();
        return toList(residuals);
    }

    public double getInnovationVariance() {
        requireFitted();
        return innovationVariance;
    }

    private RegressionData buildRegressionData(double[] y, double[][] x, double[] currentResiduals, int maxLag) {
        int rows = y.length - maxLag;
        int columns = 1 + exogenousDimension + p + q;
        double[][] features = new double[rows][columns];
        double[] targets = new double[rows];
        for (int row = 0; row < rows; row++) {
            int t = row + maxLag;
            int column = 0;
            features[row][column++] = 1.0;
            for (int j = 0; j < exogenousDimension; j++) {
                features[row][column++] = x[t][j];
            }
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

    private double[] computeResiduals(double[] y, double[][] x, double[] params) {
        double[] predictions = new double[y.length];
        double[] stateResiduals = new double[y.length];
        for (int t = 0; t < y.length; t++) {
            double prediction = params[0];
            int paramIndex = 1;
            for (int j = 0; j < exogenousDimension; j++) {
                prediction += params[paramIndex++] * x[t][j];
            }
            for (int i = 0; i < p; i++) {
                int index = t - 1 - i;
                if (index >= 0) {
                    prediction += params[paramIndex] * y[index];
                }
                paramIndex++;
            }
            for (int j = 0; j < q; j++) {
                int index = t - 1 - j;
                if (index >= 0) {
                    prediction += params[paramIndex] * stateResiduals[index];
                }
                paramIndex++;
            }
            predictions[t] = prediction;
            stateResiduals[t] = y[t] - prediction;
        }
        double[] result = new double[y.length];
        for (int i = 0; i < y.length; i++) {
            result[i] = y[i] - predictions[i];
        }
        return result;
    }

    private double[][] alignExogenous(double[][] exogenous, int offset) {
        if (exogenous.length != originalData.size()) {
            throw new IllegalArgumentException("Exogenous matrix must have the same number of rows as the target series");
        }
        int dimension = exogenous[0].length;
        double[][] aligned = new double[exogenous.length - offset][dimension];
        for (int i = offset; i < exogenous.length; i++) {
            if (exogenous[i] == null || exogenous[i].length != dimension) {
                throw new IllegalArgumentException("All exogenous rows must share the same dimension");
            }
            System.arraycopy(exogenous[i], 0, aligned[i - offset], 0, dimension);
        }
        return aligned;
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
            double[] tmp = augmented[pivot];
            augmented[pivot] = augmented[bestRow];
            augmented[bestRow] = tmp;
            double pivotValue = Math.abs(augmented[pivot][pivot]) < 1e-12 ? 1e-12 : augmented[pivot][pivot];
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

    private void validateInputs(List<Double> data, double[][] exogenous) {
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("Input data must not be null or empty");
        }
        if (exogenous == null || exogenous.length != data.size() || exogenous.length == 0 || exogenous[0] == null || exogenous[0].length == 0) {
            throw new IllegalArgumentException("Exogenous matrix must have one non-empty row per observation");
        }
        int minimumSize = Math.max(4, d + Math.max(p, q) + 2);
        if (data.size() < minimumSize) {
            throw new IllegalArgumentException("Time series is too short for the requested ARIMAX order");
        }
    }

    private void requireFit() {
        requireFitted();
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
