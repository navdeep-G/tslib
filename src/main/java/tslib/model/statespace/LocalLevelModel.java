package tslib.model.statespace;

import java.util.ArrayList;
import java.util.List;
import tslib.evaluation.ForecastIntervals;
import tslib.evaluation.IntervalForecast;
import tslib.evaluation.PredictionInterval;

/**
 * Local-level state-space model with simple variance search.
 */
public class LocalLevelModel {

    private static final double[] PROCESS_RATIO_GRID = {0.01, 0.03, 0.1, 0.3, 1.0, 3.0, 10.0};

    private KalmanFilter filter;
    private KalmanFilter.Result result;
    private double processVariance;
    private double observationVariance;
    private List<Double> data;

    public LocalLevelModel fit(List<Double> observations) {
        if (observations == null || observations.isEmpty()) {
            throw new IllegalArgumentException("Observations must not be null or empty.");
        }
        this.data = new ArrayList<>(observations);

        double baseScale = estimateDifferenceVariance(observations);
        double bestScore = Double.NEGATIVE_INFINITY;
        KalmanFilter bestFilter = null;
        KalmanFilter.Result bestResult = null;
        double bestQ = 0.0;
        double bestR = 0.0;

        for (double ratio : PROCESS_RATIO_GRID) {
            double q = Math.max(1e-6, baseScale * ratio);
            double r = Math.max(1e-6, baseScale);
            KalmanFilter candidate = new KalmanFilter(q, r);
            KalmanFilter.Result candidateResult = candidate.filter(observations);
            if (candidateResult.getLogLikelihood() > bestScore) {
                bestScore = candidateResult.getLogLikelihood();
                bestFilter = candidate;
                bestResult = candidateResult;
                bestQ = q;
                bestR = r;
            }
        }

        this.filter = bestFilter;
        this.result = bestResult;
        this.processVariance = bestQ;
        this.observationVariance = bestR;
        return this;
    }

    public List<Double> forecast(int steps) {
        requireFit();
        return filter.forecast(steps);
    }

    public List<Double> getFilteredStates() {
        requireFit();
        return result.getFilteredStates();
    }

    public List<Double> getSmoothedSignal() {
        return getFilteredStates();
    }

    public List<Double> getForecastVariances(int steps) {
        requireFit();
        return filter.forecastVariances(steps);
    }

    public List<PredictionInterval> forecastIntervals(int steps, double confidenceLevel) {
        requireFit();
        return ForecastIntervals.normalIntervals(forecast(steps), getForecastVariances(steps), confidenceLevel);
    }

    public IntervalForecast forecastWithIntervals(int steps, double confidenceLevel) {
        List<Double> forecast = forecast(steps);
        return ForecastIntervals.wrap(forecast, forecastIntervals(steps, confidenceLevel));
    }

    public double getProcessVariance() {
        requireFit();
        return processVariance;
    }

    public double getObservationVariance() {
        requireFit();
        return observationVariance;
    }

    public double getLogLikelihood() {
        requireFit();
        return result.getLogLikelihood();
    }

    private void requireFit() {
        if (result == null) {
            throw new IllegalStateException("Model must be fitted before calling this method");
        }
    }

    private double estimateDifferenceVariance(List<Double> observations) {
        if (observations.size() < 2) {
            return 1.0;
        }
        double mean = 0.0;
        List<Double> differences = new ArrayList<>(observations.size() - 1);
        for (int i = 1; i < observations.size(); i++) {
            double diff = observations.get(i) - observations.get(i - 1);
            differences.add(diff);
            mean += diff;
        }
        mean /= differences.size();

        double variance = 0.0;
        for (double diff : differences) {
            variance += (diff - mean) * (diff - mean);
        }
        variance /= Math.max(1, differences.size() - 1);
        return Math.max(variance, 1e-3);
    }
}
