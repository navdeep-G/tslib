package tslib.model.arima;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import tslib.evaluation.ForecastIntervals;
import tslib.evaluation.IntervalForecast;
import tslib.evaluation.PredictionInterval;
import tslib.transform.Differencing;
import tslib.util.LinearAlgebra;

/**
 * A compact seasonal ARIMA implementation.
 *
 * <p>This implementation keeps the same design goals as the base ARIMA class: small,
 * dependency-light, and easy to reason about. Seasonal AR and MA terms are fitted together with
 * the non-seasonal terms after applying the requested regular and seasonal differencing.
 */
public class SARIMA implements tslib.model.TimeSeriesModel {

    private static final long serialVersionUID = 1L;
    private static final double DEFAULT_RIDGE = 1e-6;
    private static final int DEFAULT_MAX_ITERATIONS = 200;
    private static final double DEFAULT_TOLERANCE = 1e-8;

    private final int p;
    private final int d;
    private final int q;
    private final int seasonalP;
    private final int seasonalD;
    private final int seasonalQ;
    private final int seasonalPeriod;
    private final int maxIterations;
    private final double tolerance;

    private final int[] arLags;
    private final int[] maLags;

    private boolean fitted;
    private double intercept;
    private double[] arCoefficients;
    private double[] maCoefficients;
    private double[] seasonalArCoefficients;
    private double[] seasonalMaCoefficients;
    private double[] residuals;
    private double[] fittedStationary;
    private double innovationVariance;
    private List<Double> originalData;
    private List<Double> seasonalDifferencedData;
    private List<Double> stationaryData;
    private List<Double> fittedSeries;

    public SARIMA(int p, int d, int q, int seasonalP, int seasonalD, int seasonalQ, int seasonalPeriod) {
        this(p, d, q, seasonalP, seasonalD, seasonalQ, seasonalPeriod, DEFAULT_MAX_ITERATIONS, DEFAULT_TOLERANCE);
    }

    public SARIMA(
            int p,
            int d,
            int q,
            int seasonalP,
            int seasonalD,
            int seasonalQ,
            int seasonalPeriod,
            int maxIterations,
            double tolerance) {
        validateOrders(p, d, q, seasonalP, seasonalD, seasonalQ, seasonalPeriod, maxIterations, tolerance);
        this.p = p;
        this.d = d;
        this.q = q;
        this.seasonalP = seasonalP;
        this.seasonalD = seasonalD;
        this.seasonalQ = seasonalQ;
        this.seasonalPeriod = seasonalPeriod;
        this.maxIterations = maxIterations;
        this.tolerance = tolerance;
        this.arLags = buildLags(p, seasonalP, seasonalPeriod);
        this.maLags = buildLags(q, seasonalQ, seasonalPeriod);
        this.arCoefficients = new double[p];
        this.maCoefficients = new double[q];
        this.seasonalArCoefficients = new double[seasonalP];
        this.seasonalMaCoefficients = new double[seasonalQ];
        this.residuals = new double[0];
        this.fittedStationary = new double[0];
    }

    public SARIMA fit(List<Double> data) {
        validateInputData(data);

        this.originalData = new ArrayList<>(data);
        this.seasonalDifferencedData = seasonalD == 0
                ? new ArrayList<>(data)
                : Differencing.seasonalDifference(data, seasonalPeriod, seasonalD);
        this.stationaryData = d == 0
                ? new ArrayList<>(seasonalDifferencedData)
                : Differencing.difference(seasonalDifferencedData, d);

        if (stationaryData.isEmpty()) {
            throw new IllegalArgumentException("Differencing orders are too high for the provided series");
        }

        double[] y = LinearAlgebra.toArray(stationaryData);
        int maxLag = Math.max(maxLag(arLags), maxLag(maLags));
        if (y.length <= maxLag) {
            throw new IllegalArgumentException("Time series is too short for the requested SARIMA order");
        }

        int parameterCount = 1 + arLags.length + maLags.length;
        double[] params = new double[parameterCount];
        params[0] = LinearAlgebra.mean(y);
        double[] currentResiduals = new double[y.length];

        for (int iteration = 0; iteration < maxIterations; iteration++) {
            RegressionData regressionData = buildRegressionData(y, currentResiduals, maxLag);
            double[] nextParams = LinearAlgebra.ordinaryLeastSquares(regressionData.features, regressionData.targets, DEFAULT_RIDGE);
            double[] nextPredictions = computePredictions(y, nextParams);
            double[] nextResiduals = subtractArrays(y, nextPredictions);

            if (LinearAlgebra.maxAbsDiff(params, nextParams) < tolerance) {
                params = nextParams;
                currentResiduals = nextResiduals;
                break;
            }

            params = nextParams;
            currentResiduals = nextResiduals;
        }

        this.intercept = params[0];
        assignCoefficients(params);
        this.fittedStationary = computePredictions(y, params);
        this.residuals = subtractArrays(y, this.fittedStationary);
        this.innovationVariance = LinearAlgebra.meanSquared(this.residuals, maxLag);
        this.fittedSeries = buildFittedSeries(maxLag);
        this.fitted = true;
        return this;
    }

    public List<Double> forecast(List<Double> data, int steps) {
        fit(data);
        List<Double> result = new ArrayList<>(fittedSeries);
        result.addAll(forecast(steps));
        return result;
    }

    public List<Double> forecast(int steps) {
        requireFitted();
        if (steps < 0) {
            throw new IllegalArgumentException("Steps must be >= 0");
        }
        if (steps == 0) {
            return new ArrayList<>();
        }

        double[] params = flattenParameters();
        List<Double> yHistory = new ArrayList<>(stationaryData);
        List<Double> residualHistory = LinearAlgebra.toList(residuals);
        List<Double> seasonalHistory = new ArrayList<>(seasonalDifferencedData);
        List<Double> originalHistory = new ArrayList<>(originalData);
        List<Double> forecasts = new ArrayList<>(steps);

        for (int h = 0; h < steps; h++) {
            double predictedStationary = predictNextStationary(yHistory, residualHistory, params);
            yHistory.add(predictedStationary);
            residualHistory.add(0.0);

            double restoredSeasonal = invertRegularDifferenceNext(predictedStationary, seasonalHistory, d);
            seasonalHistory.add(restoredSeasonal);

            double restoredOriginal = invertSeasonalDifferenceNext(restoredSeasonal, originalHistory, seasonalPeriod, seasonalD);
            originalHistory.add(restoredOriginal);
            forecasts.add(restoredOriginal);
        }
        return forecasts;
    }

    public List<Double> getFittedSeries() {
        requireFitted();
        return new ArrayList<>(fittedSeries);
    }

    public List<Double> getResiduals() {
        requireFitted();
        return LinearAlgebra.toList(residuals);
    }

    public double[] getArCoefficients() {
        requireFitted();
        return Arrays.copyOf(arCoefficients, arCoefficients.length);
    }

    public double[] getMaCoefficients() {
        requireFitted();
        return Arrays.copyOf(maCoefficients, maCoefficients.length);
    }

    public double[] getSeasonalArCoefficients() {
        requireFitted();
        return Arrays.copyOf(seasonalArCoefficients, seasonalArCoefficients.length);
    }

    public double[] getSeasonalMaCoefficients() {
        requireFitted();
        return Arrays.copyOf(seasonalMaCoefficients, seasonalMaCoefficients.length);
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

    public int getSeasonalP() {
        return seasonalP;
    }

    public int getSeasonalD() {
        return seasonalD;
    }

    public int getSeasonalQ() {
        return seasonalQ;
    }

    public int getSeasonalPeriod() {
        return seasonalPeriod;
    }

    public List<PredictionInterval> forecastIntervals(int steps, double confidenceLevel) {
        requireFitted();
        List<Double> forecast = forecast(steps);
        return ForecastIntervals.normalIntervals(forecast, computeStepVariances(steps), confidenceLevel);
    }

    /**
     * Computes h-step forecast variances using the psi-weight (MA-infinity) representation.
     * Handles arbitrary AR/MA lag sets from the combined non-seasonal and seasonal terms.
     * Regular and seasonal differencing are integrated via cumulative summation.
     */
    private List<Double> computeStepVariances(int steps) {
        // Build combined AR and MA coefficient arrays aligned with arLags/maLags
        double[] allArCoeffs = new double[arLags.length];
        System.arraycopy(arCoefficients, 0, allArCoeffs, 0, p);
        System.arraycopy(seasonalArCoefficients, 0, allArCoeffs, p, seasonalP);

        double[] allMaCoeffs = new double[maLags.length];
        System.arraycopy(maCoefficients, 0, allMaCoeffs, 0, q);
        System.arraycopy(seasonalMaCoefficients, 0, allMaCoeffs, q, seasonalQ);

        int maxMaLag = 0;
        for (int lag : maLags) {
            maxMaLag = Math.max(maxMaLag, lag);
        }
        double[] maByLag = new double[maxMaLag + 1];
        for (int i = 0; i < maLags.length; i++) {
            maByLag[maLags[i]] = allMaCoeffs[i];
        }

        double[] psi = new double[steps];
        if (steps > 0) {
            psi[0] = 1.0;
        }
        for (int h = 1; h < steps; h++) {
            double sum = 0.0;
            for (int i = 0; i < arLags.length; i++) {
                int lag = arLags[i];
                if (h - lag >= 0) {
                    sum += allArCoeffs[i] * psi[h - lag];
                }
            }
            if (h <= maxMaLag) {
                sum += maByLag[h];
            }
            psi[h] = sum;
        }

        // Regular differencing integration
        for (int k = 0; k < d; k++) {
            for (int h = 1; h < steps; h++) {
                psi[h] += psi[h - 1];
            }
        }
        // Seasonal differencing integration
        for (int k = 0; k < seasonalD; k++) {
            for (int h = seasonalPeriod; h < steps; h++) {
                psi[h] += psi[h - seasonalPeriod];
            }
        }

        List<Double> variances = new ArrayList<>(steps);
        double cumPsiSq = 0.0;
        double baseVar = Math.max(1e-12, innovationVariance);
        for (int h = 0; h < steps; h++) {
            cumPsiSq += psi[h] * psi[h];
            variances.add(baseVar * cumPsiSq);
        }
        return variances;
    }

    public IntervalForecast forecastWithIntervals(int steps, double confidenceLevel) {
        List<Double> forecast = forecast(steps);
        return ForecastIntervals.wrap(forecast, forecastIntervals(steps, confidenceLevel));
    }

    private List<Double> buildFittedSeries(int maxLag) {
        List<Double> fittedOriginal = new ArrayList<>(originalData);
        int offset = seasonalD * seasonalPeriod + d;
        for (int stationaryIndex = maxLag; stationaryIndex < fittedStationary.length; stationaryIndex++) {
            int originalIndex = offset + stationaryIndex;
            if (originalIndex >= originalData.size()) {
                break;
            }
            List<Double> actualSeasonalHistory = originalData.subList(0, originalIndex);
            List<Double> seasonalHistory = seasonalD == 0
                    ? new ArrayList<>(actualSeasonalHistory)
                    : Differencing.seasonalDifference(actualSeasonalHistory, seasonalPeriod, seasonalD);
            double restoredSeasonal = invertRegularDifferenceNext(fittedStationary[stationaryIndex], seasonalHistory, d);
            double restoredOriginal = invertSeasonalDifferenceNext(
                    restoredSeasonal,
                    actualSeasonalHistory,
                    seasonalPeriod,
                    seasonalD);
            fittedOriginal.set(originalIndex, restoredOriginal);
        }
        return fittedOriginal;
    }

    private RegressionData buildRegressionData(double[] y, double[] currentResiduals, int maxLag) {
        int rows = y.length - maxLag;
        int columns = 1 + arLags.length + maLags.length;
        double[][] features = new double[rows][columns];
        double[] targets = new double[rows];

        for (int row = 0; row < rows; row++) {
            int t = row + maxLag;
            int column = 0;
            features[row][column++] = 1.0;
            for (int lag : arLags) {
                features[row][column++] = y[t - lag];
            }
            for (int lag : maLags) {
                features[row][column++] = currentResiduals[t - lag];
            }
            targets[row] = y[t];
        }

        return new RegressionData(features, targets);
    }

    private double[] computePredictions(double[] y, double[] params) {
        double[] predictions = new double[y.length];
        double[] stateResiduals = new double[y.length];
        for (int t = 0; t < y.length; t++) {
            double prediction = predictAt(y, stateResiduals, t, params);
            predictions[t] = prediction;
            stateResiduals[t] = y[t] - prediction;
        }
        return predictions;
    }

    private double[] subtractArrays(double[] a, double[] b) {
        double[] result = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            result[i] = a[i] - b[i];
        }
        return result;
    }

    private double predictNextStationary(List<Double> yHistory, List<Double> residualHistory, double[] params) {
        double prediction = params[0];
        int parameterIndex = 1;
        for (int lag : arLags) {
            int index = yHistory.size() - lag;
            if (index >= 0) {
                prediction += params[parameterIndex] * yHistory.get(index);
            }
            parameterIndex++;
        }
        for (int lag : maLags) {
            int index = residualHistory.size() - lag;
            if (index >= 0) {
                prediction += params[parameterIndex] * residualHistory.get(index);
            }
            parameterIndex++;
        }
        return prediction;
    }

    private double predictAt(double[] y, double[] residualHistory, int t, double[] params) {
        double prediction = params[0];
        int parameterIndex = 1;
        for (int lag : arLags) {
            int index = t - lag;
            if (index >= 0) {
                prediction += params[parameterIndex] * y[index];
            }
            parameterIndex++;
        }
        for (int lag : maLags) {
            int index = t - lag;
            if (index >= 0) {
                prediction += params[parameterIndex] * residualHistory[index];
            }
            parameterIndex++;
        }
        return prediction;
    }

    private void assignCoefficients(double[] params) {
        int parameterIndex = 1;
        for (int i = 0; i < p; i++) {
            arCoefficients[i] = params[parameterIndex++];
        }
        for (int i = 0; i < seasonalP; i++) {
            seasonalArCoefficients[i] = params[parameterIndex++];
        }
        for (int i = 0; i < q; i++) {
            maCoefficients[i] = params[parameterIndex++];
        }
        for (int i = 0; i < seasonalQ; i++) {
            seasonalMaCoefficients[i] = params[parameterIndex++];
        }
    }

    private double[] flattenParameters() {
        double[] params = new double[1 + arLags.length + maLags.length];
        int parameterIndex = 0;
        params[parameterIndex++] = intercept;
        for (double coefficient : arCoefficients) {
            params[parameterIndex++] = coefficient;
        }
        for (double coefficient : seasonalArCoefficients) {
            params[parameterIndex++] = coefficient;
        }
        for (double coefficient : maCoefficients) {
            params[parameterIndex++] = coefficient;
        }
        for (double coefficient : seasonalMaCoefficients) {
            params[parameterIndex++] = coefficient;
        }
        return params;
    }

    private int[] buildLags(int nonSeasonal, int seasonal, int seasonalPeriod) {
        int[] result = new int[nonSeasonal + seasonal];
        int index = 0;
        for (int i = 1; i <= nonSeasonal; i++) {
            result[index++] = i;
        }
        for (int i = 1; i <= seasonal; i++) {
            result[index++] = i * seasonalPeriod;
        }
        return result;
    }

    private static void validateOrders(
            int p,
            int d,
            int q,
            int seasonalP,
            int seasonalD,
            int seasonalQ,
            int seasonalPeriod,
            int maxIterations,
            double tolerance) {
        if (p < 0 || d < 0 || q < 0 || seasonalP < 0 || seasonalD < 0 || seasonalQ < 0) {
            throw new IllegalArgumentException("SARIMA orders must be >= 0");
        }
        if ((seasonalP > 0 || seasonalD > 0 || seasonalQ > 0) && seasonalPeriod < 1) {
            throw new IllegalArgumentException("Seasonal period must be >= 1");
        }
        if (maxIterations < 1) {
            throw new IllegalArgumentException("maxIterations must be >= 1");
        }
        if (!(tolerance > 0.0)) {
            throw new IllegalArgumentException("tolerance must be > 0");
        }
    }

    private static int maxLag(int[] lags) {
        int max = 0;
        for (int lag : lags) {
            max = Math.max(max, lag);
        }
        return max;
    }

    private double invertRegularDifferenceNext(double predictedDifference, List<Double> history, int order) {
        if (order == 0) {
            return predictedDifference;
        }
        if (history.size() < order) {
            throw new IllegalArgumentException("History is too short to invert regular differencing");
        }
        double restored = predictedDifference;
        for (int i = 1; i <= order; i++) {
            double sign = (i % 2 == 1) ? 1.0 : -1.0;
            restored += sign * combination(order, i) * history.get(history.size() - i);
        }
        return restored;
    }

    private double invertSeasonalDifferenceNext(double predictedSeasonalDifference, List<Double> history, int lag, int order) {
        if (order == 0) {
            return predictedSeasonalDifference;
        }
        if (history.size() < lag * order) {
            throw new IllegalArgumentException("History is too short to invert seasonal differencing");
        }
        double restored = predictedSeasonalDifference;
        for (int i = 1; i <= order; i++) {
            double sign = (i % 2 == 1) ? 1.0 : -1.0;
            restored += sign * combination(order, i) * history.get(history.size() - i * lag);
        }
        return restored;
    }

    private double combination(int n, int k) {
        if (k < 0 || k > n) {
            return 0.0;
        }
        if (k == 0 || k == n) {
            return 1.0;
        }
        double result = 1.0;
        for (int i = 1; i <= k; i++) {
            result *= (n - (k - i));
            result /= i;
        }
        return result;
    }

    private void validateInputData(List<Double> data) {
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("Input data must not be null or empty.");
        }
        int minimumSize = Math.max(4, seasonalD * seasonalPeriod + d + Math.max(maxLag(arLags), maxLag(maLags)) + 1);
        if (data.size() < minimumSize) {
            throw new IllegalArgumentException("Time series is too short for the requested SARIMA order");
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
