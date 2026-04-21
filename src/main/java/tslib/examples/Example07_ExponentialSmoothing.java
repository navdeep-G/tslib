package tslib.examples;

import tslib.model.DoubleExpSmoothing;
import tslib.model.SingleExpSmoothing;
import tslib.model.TripleExpSmoothing;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Example 07: Exponential smoothing — Simple (SES), Double (Holt), and Triple (Holt-Winters).
 *
 * SES    — level only; good for data with no trend or seasonality.
 * DES    — level + trend; Holt's linear method.
 * TES    — level + trend + multiplicative seasonality; Holt-Winters.
 */
public class Example07_ExponentialSmoothing {

    public static void run() {
        System.out.println("=== Example 07: Exponential Smoothing ===\n");

        // --- 1. Single Exponential Smoothing (SES) ---
        System.out.println("-- SES (alpha=0.3) --");
        List<Double> flatWithNoise = generateNoisyFlat(60, 100.0, 5.0, 1);
        // forecast() returns [in-sample smoothed values... , h future forecasts]
        // Slice off only the future h values with subList(n, n+h)
        SingleExpSmoothing ses = new SingleExpSmoothing(0.3);
        int n60 = flatWithNoise.size();
        List<Double> sesFull = ses.forecast(flatWithNoise, 6);
        List<Double> sesForecast = sesFull.subList(n60, n60 + 6);
        System.out.println("Training tail (last 5):  " + fmt(flatWithNoise.subList(55, 60)));
        System.out.println("SES forecast (h=6):       " + fmt(sesForecast));

        // Different alphas: higher alpha = more reactive
        System.out.println("\nEffect of alpha on SES (h=3):");
        for (double alpha : new double[]{0.1, 0.3, 0.5, 0.7, 0.9}) {
            List<Double> full = new SingleExpSmoothing(alpha).forecast(flatWithNoise, 3);
            List<Double> f = full.subList(n60, n60 + 3);
            System.out.printf("  alpha=%.1f → %s%n", alpha, fmt(f));
        }

        // --- 2. Double Exponential Smoothing (Holt) ---
        System.out.println("\n-- DES / Holt's method (alpha=0.4, gamma=0.2) --");
        List<Double> trend = generateTrend(80, 50.0, 2.0, 3.0, 2);
        // initializationMethod: 0=first obs, 1=average, 2=regression-based
        DoubleExpSmoothing des0 = new DoubleExpSmoothing(0.4, 0.2, 0);
        DoubleExpSmoothing des1 = new DoubleExpSmoothing(0.4, 0.2, 1);
        DoubleExpSmoothing des2 = new DoubleExpSmoothing(0.4, 0.2, 2);

        int n80 = trend.size();
        System.out.println("Training tail (last 5): " + fmt(trend.subList(75, 80)));
        List<Double> des0f = des0.forecast(trend, 5);
        List<Double> des1f = des1.forecast(trend, 5);
        List<Double> des2f = des2.forecast(trend, 5);
        System.out.println("DES(init=0) forecast (h=5): " + fmt(des0f.subList(n80, n80 + 5)));
        System.out.println("DES(init=1) forecast (h=5): " + fmt(des1f.subList(n80, n80 + 5)));
        System.out.println("DES(init=2) forecast (h=5): " + fmt(des2f.subList(n80, n80 + 5)));

        // --- 3. Triple Exponential Smoothing (Holt-Winters multiplicative) ---
        System.out.println("\n-- TES / Holt-Winters (alpha=0.3, beta=0.1, gamma=0.2, period=12) --");
        List<Double> seasonal = generateSeasonalTrend(120, 12, 200.0, 3.0, 20.0, 4.0, 3);
        TripleExpSmoothing tes = new TripleExpSmoothing(0.3, 0.1, 0.2, 12, false);
        int n120 = seasonal.size();
        List<Double> tesFull = tes.forecast(seasonal, 12);
        List<Double> tesForecast = tesFull.subList(n120, n120 + 12);
        System.out.println("Training tail (last 12): " + fmt(seasonal.subList(108, 120)));
        System.out.println("TES forecast (h=12):      " + fmt(tesForecast));

        // With debug output enabled (prints state at each step to stdout)
        System.out.println("\nTES with debug=true — fitting on 36 pts, forecast h=6:");
        List<Double> shortSeasonal = generateSeasonalTrend(36, 12, 50.0, 1.0, 10.0, 2.0, 5);
        TripleExpSmoothing tesDebug = new TripleExpSmoothing(0.3, 0.1, 0.2, 12, true);
        int n36 = shortSeasonal.size();
        List<Double> tesDebugFull = tesDebug.forecast(shortSeasonal, 6);
        System.out.println("TES (debug) forecast (h=6): " + fmt(tesDebugFull.subList(n36, n36 + 6)));

        System.out.println();
    }

    static List<Double> generateNoisyFlat(int n, double level, double sigma, long seed) {
        List<Double> out = new ArrayList<>(n);
        Random rng = new Random(seed);
        for (int i = 0; i < n; i++) out.add(level + sigma * rng.nextGaussian());
        return out;
    }

    static List<Double> generateTrend(int n, double start, double slope, double sigma, long seed) {
        List<Double> out = new ArrayList<>(n);
        Random rng = new Random(seed);
        for (int i = 0; i < n; i++) out.add(start + slope * i + sigma * rng.nextGaussian());
        return out;
    }

    static List<Double> generateSeasonalTrend(int n, int period, double base, double slope,
                                               double amplitude, double sigma, long seed) {
        List<Double> out = new ArrayList<>(n);
        Random rng = new Random(seed);
        for (int i = 0; i < n; i++) {
            double t = base + slope * i;
            double s = amplitude * Math.sin(2.0 * Math.PI * i / period);
            out.add(t + s + sigma * rng.nextGaussian());
        }
        return out;
    }

    static List<Double> fmt(List<Double> list) {
        List<Double> out = new ArrayList<>(list.size());
        for (Double v : list) out.add(v == null ? null : Math.round(v * 100.0) / 100.0);
        return out;
    }
}
