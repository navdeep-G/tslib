package tslib.model.statespace;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.univariate.BrentOptimizer;
import org.apache.commons.math3.optim.univariate.SearchInterval;
import org.apache.commons.math3.optim.univariate.UnivariateObjectiveFunction;
import tslib.evaluation.ForecastIntervals;
import tslib.evaluation.IntervalForecast;
import tslib.evaluation.PredictionInterval;

/**
 * Local-level state-space model with MLE optimization via Brent search.
 *
 * <p>The observation variance is estimated from the first-difference variance of the
 * series. The process variance is found by maximising the Kalman-filter log-likelihood
 * over a log-transformed ratio search interval, using the Brent algorithm (exact MLE
 * rather than a 7-point grid).
 */
public class LocalLevelModel implements tslib.model.TimeSeriesModel {

    private static final long serialVersionUID = 1L;

    /** Log-scale lower bound for the q/r ratio search: log(1e-4). */
    private static final double LOG_MIN_RATIO = Math.log(1e-4);
    /** Log-scale upper bound for the q/r ratio search: log(1e4). */
    private static final double LOG_MAX_RATIO = Math.log(1e4);
    private static final int MAX_BRENT_EVALS = 200;

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

        final double baseScale = estimateDifferenceVariance(observations);
        final double r = Math.max(1e-6, baseScale);
        final List<Double> obs = this.data;

        BrentOptimizer optimizer = new BrentOptimizer(1e-8, 1e-10);
        double bestLogRatio = optimizer.optimize(
                new MaxEval(MAX_BRENT_EVALS),
                new UnivariateObjectiveFunction(new UnivariateFunction() {
                    @Override
                    public double value(double logRatio) {
                        double q = Math.max(1e-6, baseScale * Math.exp(logRatio));
                        return -new KalmanFilter(q, r).filter(obs).getLogLikelihood();
                    }
                }),
                GoalType.MINIMIZE,
                new SearchInterval(LOG_MIN_RATIO, LOG_MAX_RATIO)
        ).getPoint();

        double bestQ = Math.max(1e-6, baseScale * Math.exp(bestLogRatio));
        this.filter = new KalmanFilter(bestQ, r);
        this.result = this.filter.filter(observations);
        this.processVariance = bestQ;
        this.observationVariance = r;
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
