package tslib.examples;

import tslib.evaluation.IntervalForecast;
import tslib.evaluation.PredictionInterval;
import tslib.model.ARIMA;
import tslib.model.ARIMAX;
import tslib.model.SARIMA;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Example 06: ARIMA, SARIMA, and ARIMAX forecasting with prediction intervals.
 */
public class Example06_ARIMAForecasting {

    public static void run() {
        System.out.println("=== Example 06: ARIMA / SARIMA / ARIMAX Forecasting ===\n");

        // --- 1. ARIMA(1,1,1) ---
        System.out.println("-- ARIMA(1,1,1) --");
        List<Double> series = generateAR1WithDrift(150, 0.7, 0.5, 1.0, 1);
        ARIMA arima = new ARIMA(1, 1, 1).fit(series);

        List<Double> forecast = arima.forecast(5);
        System.out.println("Point forecast (h=5): " + fmt(forecast));

        // Coefficients and model diagnostics
        System.out.printf("Innovation variance : %.6f%n", arima.getInnovationVariance());
        double[] arCoefs = arima.getArCoefficients();
        double[] maCoefs = arima.getMaCoefficients();
        System.out.printf("AR coefficient      : %.4f%n", arCoefs[0]);
        System.out.printf("MA coefficient      : %.4f%n", maCoefs[0]);

        // Prediction intervals at 95% confidence
        IntervalForecast ivf = arima.forecastWithIntervals(5, 0.95);
        System.out.println("95% Prediction intervals:");
        for (PredictionInterval pi : ivf.getIntervals()) {
            System.out.printf("  h=%d  point=%.3f  [%.3f, %.3f]%n",
                    pi.getStep(), pi.getPointForecast(), pi.getLower(), pi.getUpper());
        }

        // Fitted values and residuals
        List<Double> fitted = arima.getFittedSeries();
        List<Double> residuals = arima.getResiduals();
        System.out.printf("Fitted series length: %d, first fitted value=%.4f%n",
                fitted.size(), fitted.get(0));
        System.out.printf("Residuals length    : %d, last residual=%.6f%n",
                residuals.size(), residuals.get(residuals.size() - 1));

        // 80% prediction intervals via forecastIntervals
        List<PredictionInterval> pis80 = arima.forecastIntervals(3, 0.80);
        System.out.println("\n80% prediction intervals (h=3):");
        for (PredictionInterval pi : pis80) {
            System.out.printf("  h=%d  point=%.3f  [%.3f, %.3f]%n",
                    pi.getStep(), pi.getPointForecast(), pi.getLower(), pi.getUpper());
        }

        // --- 2. SARIMA(1,1,1)(1,0,1)_12 ---
        System.out.println("\n-- SARIMA(1,1,1)(1,0,1)_12 --");
        List<Double> seasonal = generateSeasonalSeries(120, 12, 5.0, 1.0, 2);
        SARIMA sarima = new SARIMA(1, 1, 1, 1, 0, 1, 12).fit(seasonal);

        List<Double> sarimaForecast = sarima.forecast(12);
        System.out.println("SARIMA forecast (next 12 steps): " + fmt(sarimaForecast));
        System.out.printf("Seasonal period: %d%n", sarima.getSeasonalPeriod());

        IntervalForecast sarimaIvf = sarima.forecastWithIntervals(6, 0.90);
        System.out.println("90% Prediction intervals (h=6):");
        for (PredictionInterval pi : sarimaIvf.getIntervals()) {
            System.out.printf("  h=%d  point=%.3f  [%.3f, %.3f]%n",
                    pi.getStep(), pi.getPointForecast(), pi.getLower(), pi.getUpper());
        }

        // --- 3. ARIMAX(1,0,1) with one exogenous variable ---
        System.out.println("\n-- ARIMAX(1,0,1) with exogenous variable --");
        int n = 80;
        double[] temperature = generateTemperature(n, 3);  // exogenous: temperature readings
        List<Double> demand = generateDemandFromTemp(temperature, 1.0, 0.6, 0.8, 4);

        double[][] exoTrain = new double[n][1];
        for (int i = 0; i < n; i++) exoTrain[i][0] = temperature[i];

        ARIMAX arimax = new ARIMAX(1, 0, 1).fit(demand, exoTrain);

        double[] futureTempArray = {22.0, 23.5, 21.0, 24.0};
        double[][] futureExo = new double[4][1];
        for (int i = 0; i < 4; i++) futureExo[i][0] = futureTempArray[i];

        List<Double> arimaxForecast = arimax.forecast(futureExo);
        System.out.println("ARIMAX forecast (h=4): " + fmt(arimaxForecast));

        double[] exoCoefs = arimax.getExogenousCoefficients();
        System.out.printf("Exogenous coefficient (temperature): %.4f%n", exoCoefs[0]);

        System.out.println();
    }

    // --- data generators ---

    static List<Double> generateAR1WithDrift(int n, double phi, double drift, double sigma, long seed) {
        List<Double> out = new ArrayList<>(n);
        Random rng = new Random(seed);
        double val = 10.0;
        for (int i = 0; i < n; i++) {
            val = drift + phi * val + sigma * rng.nextGaussian();
            out.add(val);
        }
        return out;
    }

    static List<Double> generateSeasonalSeries(int n, int period, double amplitude, double sigma, long seed) {
        List<Double> out = new ArrayList<>(n);
        Random rng = new Random(seed);
        double trend = 0.0;
        for (int i = 0; i < n; i++) {
            double s = amplitude * Math.sin(2.0 * Math.PI * i / period);
            trend += 0.1;
            out.add(trend + s + sigma * rng.nextGaussian());
        }
        return out;
    }

    static double[] generateTemperature(int n, long seed) {
        double[] out = new double[n];
        Random rng = new Random(seed);
        double t = 20.0;
        for (int i = 0; i < n; i++) {
            t += 0.5 * rng.nextGaussian();
            t = Math.max(10.0, Math.min(35.0, t));
            out[i] = t;
        }
        return out;
    }

    static List<Double> generateDemandFromTemp(double[] temp, double intercept, double slope,
                                                double sigma, long seed) {
        List<Double> out = new ArrayList<>(temp.length);
        Random rng = new Random(seed);
        double prev = intercept + slope * temp[0];
        for (int i = 0; i < temp.length; i++) {
            double val = intercept + slope * temp[i] + 0.5 * prev + sigma * rng.nextGaussian();
            out.add(val);
            prev = val;
        }
        return out;
    }

    static List<Double> fmt(List<Double> list) {
        List<Double> out = new ArrayList<>(list.size());
        for (Double v : list) out.add(v == null ? null : Math.round(v * 1000.0) / 1000.0);
        return out;
    }
}
