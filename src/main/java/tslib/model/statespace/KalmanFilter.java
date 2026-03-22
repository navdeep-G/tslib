package tslib.model.statespace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * One-dimensional Kalman filter for the local-level state-space model.
 */
public class KalmanFilter {

    private final double processVariance;
    private final double observationVariance;
    private double lastFilteredState;
    private double lastFilteredCovariance;
    private boolean filtered;

    public KalmanFilter(double processVariance, double observationVariance) {
        if (!(processVariance > 0.0) || !(observationVariance > 0.0)) {
            throw new IllegalArgumentException("Variances must be > 0");
        }
        this.processVariance = processVariance;
        this.observationVariance = observationVariance;
    }

    public Result filter(List<Double> observations) {
        if (observations == null || observations.isEmpty()) {
            throw new IllegalArgumentException("Observations must not be null or empty.");
        }

        double state = observations.get(0);
        double covariance = Math.max(observationVariance, processVariance);

        List<Double> predictedStates = new ArrayList<>(observations.size());
        List<Double> filteredStates = new ArrayList<>(observations.size());
        List<Double> filteredCovariances = new ArrayList<>(observations.size());
        List<Double> innovations = new ArrayList<>(observations.size());
        double logLikelihood = 0.0;

        for (double observation : observations) {
            double predictedState = state;
            double predictedCovariance = covariance + processVariance;
            double innovation = observation - predictedState;
            double innovationVariance = predictedCovariance + observationVariance;
            double gain = predictedCovariance / innovationVariance;

            state = predictedState + gain * innovation;
            covariance = (1.0 - gain) * predictedCovariance;

            predictedStates.add(predictedState);
            filteredStates.add(state);
            filteredCovariances.add(covariance);
            innovations.add(innovation);
            logLikelihood += -0.5 * (Math.log(2.0 * Math.PI * innovationVariance)
                    + (innovation * innovation) / innovationVariance);
        }

        this.lastFilteredState = state;
        this.lastFilteredCovariance = covariance;
        this.filtered = true;

        return new Result(predictedStates, filteredStates, filteredCovariances, innovations, logLikelihood);
    }

    public List<Double> forecast(int steps) {
        ensureFiltered();
        if (steps < 0) {
            throw new IllegalArgumentException("Steps must be >= 0");
        }
        List<Double> forecasts = new ArrayList<>(steps);
        for (int i = 0; i < steps; i++) {
            forecasts.add(lastFilteredState);
        }
        return forecasts;
    }

    public List<Double> forecastVariances(int steps) {
        ensureFiltered();
        if (steps < 0) {
            throw new IllegalArgumentException("Steps must be >= 0");
        }
        List<Double> variances = new ArrayList<>(steps);
        for (int i = 1; i <= steps; i++) {
            variances.add(lastFilteredCovariance + i * processVariance + observationVariance);
        }
        return variances;
    }

    public double getProcessVariance() {
        return processVariance;
    }

    public double getObservationVariance() {
        return observationVariance;
    }

    private void ensureFiltered() {
        if (!filtered) {
            throw new IllegalStateException("Filter must be run before forecasting");
        }
    }

    public static final class Result {
        private final List<Double> predictedStates;
        private final List<Double> filteredStates;
        private final List<Double> filteredCovariances;
        private final List<Double> innovations;
        private final double logLikelihood;

        private Result(
                List<Double> predictedStates,
                List<Double> filteredStates,
                List<Double> filteredCovariances,
                List<Double> innovations,
                double logLikelihood) {
            this.predictedStates = Collections.unmodifiableList(predictedStates);
            this.filteredStates = Collections.unmodifiableList(filteredStates);
            this.filteredCovariances = Collections.unmodifiableList(filteredCovariances);
            this.innovations = Collections.unmodifiableList(innovations);
            this.logLikelihood = logLikelihood;
        }

        public List<Double> getPredictedStates() {
            return predictedStates;
        }

        public List<Double> getFilteredStates() {
            return filteredStates;
        }

        public List<Double> getFilteredCovariances() {
            return filteredCovariances;
        }

        public List<Double> getInnovations() {
            return innovations;
        }

        public double getLogLikelihood() {
            return logLikelihood;
        }
    }
}
