package tslib.model.arima;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import tslib.util.LinearAlgebra;

/**
 * Vector Autoregression (VAR) model for multivariate time series.
 *
 * <p>A VAR(p) model jointly models K endogenous series via:
 * <pre>  Y_t = c + A_1 Y_{t-1} + ... + A_p Y_{t-p} + e_t</pre>
 * where each A_i is a K×K coefficient matrix. Each equation is estimated
 * independently by ordinary least squares with a small ridge penalty.
 *
 * <p>Use {@link #fitOptimal(List, int)} to select the lag order by AIC.
 */
public class VARModel implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final double DEFAULT_RIDGE = 1e-6;

    private final int lagOrder;
    private boolean fitted;
    private int numSeries;
    private int effectiveObs;

    /** coefficients[k] has length 1 + lagOrder * numSeries. */
    private double[][] coefficients;
    /** residuals[k] has length effectiveObs. */
    private double[][] residuals;
    private List<List<Double>> trainingData;

    public VARModel(int lagOrder) {
        if (lagOrder < 1) {
            throw new IllegalArgumentException("Lag order must be >= 1");
        }
        this.lagOrder = lagOrder;
    }

    /**
     * Fits the VAR(p) model to the given multivariate series.
     *
     * @param series list of K series, each of the same length T; requires T > lagOrder
     * @return this (fluent)
     */
    public VARModel fit(List<List<Double>> series) {
        validateInput(series);
        this.numSeries = series.size();
        int seriesLength = series.get(0).size();
        this.effectiveObs = seriesLength - lagOrder;

        List<List<Double>> dataCopy = new ArrayList<>(numSeries);
        for (List<Double> s : series) {
            dataCopy.add(Collections.unmodifiableList(new ArrayList<>(s)));
        }
        this.trainingData = Collections.unmodifiableList(dataCopy);

        double[][] designMatrix = buildDesignMatrix(series, seriesLength);

        this.coefficients = new double[numSeries][];
        this.residuals = new double[numSeries][effectiveObs];
        for (int k = 0; k < numSeries; k++) {
            double[] targets = new double[effectiveObs];
            for (int row = 0; row < effectiveObs; row++) {
                targets[row] = series.get(k).get(row + lagOrder);
            }
            coefficients[k] = LinearAlgebra.ordinaryLeastSquares(designMatrix, targets, DEFAULT_RIDGE);
            for (int row = 0; row < effectiveObs; row++) {
                residuals[k][row] = targets[row] - dot(coefficients[k], designMatrix[row]);
            }
        }

        this.fitted = true;
        return this;
    }

    /**
     * Forecasts {@code steps} periods ahead for each series.
     *
     * @param steps number of future steps
     * @return list of K forecast lists, each of length {@code steps}
     */
    public List<List<Double>> forecast(int steps) {
        requireFitted();
        if (steps < 1) {
            throw new IllegalArgumentException("Steps must be >= 1");
        }

        List<List<Double>> history = new ArrayList<>(numSeries);
        for (int k = 0; k < numSeries; k++) {
            history.add(new ArrayList<>(trainingData.get(k)));
        }

        List<List<Double>> forecasts = new ArrayList<>(numSeries);
        for (int k = 0; k < numSeries; k++) {
            forecasts.add(new ArrayList<>(steps));
        }

        for (int h = 0; h < steps; h++) {
            int currentSize = history.get(0).size();
            double[] featureVector = buildFeatureVector(history, currentSize);
            for (int k = 0; k < numSeries; k++) {
                double prediction = dot(coefficients[k], featureVector);
                forecasts.get(k).add(prediction);
                history.get(k).add(prediction);
            }
        }

        return forecasts;
    }

    /**
     * Akaike Information Criterion: sum_k[T_eff * log(RSS_k / T_eff)] + 2*K*(1 + p*K).
     */
    public double getAic() {
        requireFitted();
        int k = numSeries;
        double aic = 0.0;
        for (int eq = 0; eq < k; eq++) {
            double rss = 0.0;
            for (double r : residuals[eq]) {
                rss += r * r;
            }
            aic += effectiveObs * Math.log(Math.max(rss / effectiveObs, Double.MIN_VALUE));
        }
        return aic + 2.0 * k * (1 + lagOrder * k);
    }

    /**
     * Fits VAR models for each lag from 1 to {@code maxLag} and returns the one with
     * the lowest AIC.
     */
    public static VARModel fitOptimal(List<List<Double>> series, int maxLag) {
        if (series == null || series.isEmpty()) {
            throw new IllegalArgumentException("Series must not be null or empty");
        }
        if (maxLag < 1) {
            throw new IllegalArgumentException("maxLag must be >= 1");
        }
        VARModel best = null;
        double bestAic = Double.POSITIVE_INFINITY;
        for (int p = 1; p <= maxLag; p++) {
            VARModel candidate = new VARModel(p);
            try {
                candidate.fit(series);
                double aic = candidate.getAic();
                if (aic < bestAic) {
                    bestAic = aic;
                    best = candidate;
                }
            } catch (IllegalArgumentException ignored) {
                // not enough data for this lag order
            }
        }
        if (best == null) {
            throw new IllegalArgumentException("No valid VAR model could be fitted with the given data and maxLag");
        }
        return best;
    }

    /** Returns the coefficient matrix: {@code coefficients[k][j]} for equation k, feature j. */
    public double[][] getCoefficients() {
        requireFitted();
        double[][] copy = new double[numSeries][];
        for (int k = 0; k < numSeries; k++) {
            copy[k] = java.util.Arrays.copyOf(coefficients[k], coefficients[k].length);
        }
        return copy;
    }

    public int getLagOrder() {
        return lagOrder;
    }

    public int getNumSeries() {
        requireFitted();
        return numSeries;
    }

    private double[][] buildDesignMatrix(List<List<Double>> series, int seriesLength) {
        double[][] matrix = new double[effectiveObs][1 + lagOrder * numSeries];
        for (int row = 0; row < effectiveObs; row++) {
            int t = row + lagOrder;
            int col = 0;
            matrix[row][col++] = 1.0;
            for (int lag = 1; lag <= lagOrder; lag++) {
                for (int k = 0; k < numSeries; k++) {
                    matrix[row][col++] = series.get(k).get(t - lag);
                }
            }
        }
        return matrix;
    }

    private double[] buildFeatureVector(List<List<Double>> history, int currentIndex) {
        int numCols = 1 + lagOrder * numSeries;
        double[] vector = new double[numCols];
        int col = 0;
        vector[col++] = 1.0;
        for (int lag = 1; lag <= lagOrder; lag++) {
            int index = currentIndex - lag;
            for (int k = 0; k < numSeries; k++) {
                vector[col++] = history.get(k).get(index);
            }
        }
        return vector;
    }

    private double dot(double[] a, double[] b) {
        double sum = 0.0;
        for (int i = 0; i < a.length; i++) {
            sum += a[i] * b[i];
        }
        return sum;
    }

    private void validateInput(List<List<Double>> series) {
        if (series == null || series.size() < 2) {
            throw new IllegalArgumentException("VAR requires at least 2 series");
        }
        int length = series.get(0).size();
        if (length <= lagOrder) {
            throw new IllegalArgumentException("Series length must exceed lag order");
        }
        for (List<Double> s : series) {
            if (s == null || s.size() != length) {
                throw new IllegalArgumentException("All series must be non-null and have the same length");
            }
        }
        int minObs = lagOrder * series.size() + 2;
        if (length - lagOrder < minObs - lagOrder) {
            throw new IllegalArgumentException("Not enough observations to estimate VAR(" + lagOrder + ")");
        }
    }

    private void requireFitted() {
        if (!fitted) {
            throw new IllegalStateException("Model must be fitted before calling this method");
        }
    }
}
