package tslib.examples;

import tslib.evaluation.IntervalForecast;
import tslib.evaluation.PredictionInterval;
import tslib.model.LocalLevelModel;
import tslib.model.statespace.KalmanFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Example 08: State-space models — manual Kalman filter and the LocalLevelModel
 * with Brent MLE hyperparameter estimation.
 *
 * KalmanFilter — raw filter exposing predicted states, filtered states, innovations,
 *                log-likelihood, and h-step-ahead forecasts.
 * LocalLevelModel — fits process/observation variance pair by maximising the
 *                   Kalman log-likelihood, then exposes smoothed signal and forecasts.
 */
public class Example08_StateSpaceModels {

    public static void run() {
        System.out.println("=== Example 08: State-Space Models ===\n");

        List<Double> noisySignal = generateNoisyRamp(80, 0.5, 2.0, 1.0, 42);

        // --- 1. Manual KalmanFilter ---
        System.out.println("-- KalmanFilter (manual variances) --");
        // processVariance = how much the hidden state evolves per step
        // observationVariance = measurement noise
        KalmanFilter kf = new KalmanFilter(1.0, 4.0);
        KalmanFilter.Result result = kf.filter(noisySignal);

        List<Double> filtered = result.getFilteredStates();
        List<Double> predicted = result.getPredictedStates();
        List<Double> innovations = result.getInnovations();
        System.out.printf("Log-likelihood        : %.4f%n", result.getLogLikelihood());
        System.out.printf("Filtered states [0-4] : %s%n", fmt(filtered.subList(0, 5)));
        System.out.printf("Predicted states[0-4] : %s%n", fmt(predicted.subList(0, 5)));
        System.out.printf("Innovations    [0-4]  : %s%n", fmt(innovations.subList(0, 5)));

        // h-step forecasts extend the filtered state forward using process noise
        List<Double> kfForecast = kf.forecast(6);
        System.out.printf("KF h=6 forecast       : %s%n", fmt(kfForecast));

        List<Double> kfForecastVar = kf.forecastVariances(6);
        System.out.printf("Forecast variances    : %s%n", fmt(kfForecastVar));

        System.out.printf("Process variance      : %.4f%n", kf.getProcessVariance());
        System.out.printf("Observation variance  : %.4f%n", kf.getObservationVariance());

        // --- 2. LocalLevelModel (MLE hyperparameter estimation) ---
        System.out.println("\n-- LocalLevelModel (Brent MLE) --");
        LocalLevelModel llm = new LocalLevelModel().fit(noisySignal);

        List<Double> smoothed = llm.getFilteredStates();   // = getSmoothedSignal()
        System.out.printf("Smoothed signal [0-4]    : %s%n", fmt(smoothed.subList(0, 5)));
        System.out.printf("Smoothed signal [-5:]    : %s%n", fmt(smoothed.subList(75, 80)));

        List<Double> llmForecast = llm.forecast(6);
        System.out.printf("LLM h=6 forecast         : %s%n", fmt(llmForecast));

        List<Double> llmVar = llm.getForecastVariances(6);
        System.out.printf("LLM forecast variances   : %s%n", fmt(llmVar));

        // Prediction intervals
        IntervalForecast ivf = llm.forecastWithIntervals(6, 0.95);
        System.out.println("LLM 95% prediction intervals:");
        for (PredictionInterval pi : ivf.getIntervals()) {
            System.out.printf("  h=%d  point=%.3f  [%.3f, %.3f]%n",
                    pi.getStep(), pi.getPointForecast(), pi.getLower(), pi.getUpper());
        }

        // forecastIntervals returns a List<PredictionInterval> directly
        List<PredictionInterval> pis = llm.forecastIntervals(3, 0.80);
        System.out.println("LLM 80% intervals (List<PredictionInterval>):");
        for (PredictionInterval pi : pis) {
            System.out.printf("  h=%d  [%.3f, %.3f]%n", pi.getStep(), pi.getLower(), pi.getUpper());
        }

        System.out.println();
    }

    static List<Double> generateNoisyRamp(int n, double slope, double start,
                                           double sigma, long seed) {
        List<Double> out = new ArrayList<>(n);
        Random rng = new Random(seed);
        for (int i = 0; i < n; i++) out.add(start + slope * i + sigma * rng.nextGaussian());
        return out;
    }

    static List<Double> fmt(List<Double> list) {
        List<Double> out = new ArrayList<>(list.size());
        for (Double v : list) out.add(v == null ? null : Math.round(v * 1000.0) / 1000.0);
        return out;
    }
}
