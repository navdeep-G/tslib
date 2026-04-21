import tslib.collect.Collect;
import tslib.util.Stats;

import java.util.Arrays;
import java.util.List;

/**
 * Example 01: Data ingestion and descriptive statistics via Collect.
 *
 * Demonstrates loading a series from a double[], inspecting summary statistics,
 * computing autocorrelation/PACF, applying built-in transformations, and
 * running the Augmented Dickey-Fuller stationarity check.
 */
public class Example01_DataIngestion {

    public static void run() {
        System.out.println("=== Example 01: Data Ingestion & Descriptive Statistics ===\n");

        // --- 1. Build a synthetic series (random walk with drift) ---
        double[] raw = generateRandomWalk(120, 0.5, 1.0, 42);
        // k=1 (autocovariance lag), n=10 (PACF lags)
        Collect ts = new Collect(raw, 1, 10);

        // --- 2. Summary statistics ---
        System.out.printf("N          : %d%n", raw.length);
        System.out.printf("Mean       : %.4f%n", ts.getAverage());
        System.out.printf("Variance   : %.4f%n", ts.getVariance());
        System.out.printf("Std Dev    : %.4f%n", ts.getStandardDeviation());
        System.out.printf("Min        : %.4f  (index %d)%n", ts.getMin(), ts.getMinIndex());
        System.out.printf("Max        : %.4f  (index %d)%n", ts.getMax(), ts.getMaxIndex());

        // --- 3. Autocorrelation ---
        System.out.printf("%nAutocovariance (lag 1) : %.4f%n", ts.getAutocovariance());
        System.out.printf("Autocorrelation (lag 1): %.4f%n", ts.getAutocorrelation());

        // --- 4. ACF and PACF ---
        double[] acf = ts.acf(8);
        System.out.print("\nACF (lags 0-8)  : ");
        for (double v : acf) System.out.printf("%.3f  ", v);

        double[] pacf = ts.pacf();
        System.out.print("\nPACF (up to lag 10): ");
        for (double v : pacf) System.out.printf("%.3f  ", v);

        // --- 5. Transformations ---
        List<Double> logT = ts.getLogTransformed();
        List<Double> bcT  = ts.getBoxCoxTransformed();
        List<Double> diff = ts.getFirstDifference();
        List<Double> roll = ts.getRollingAverage(5);
        System.out.printf("%n%nFirst 5 log-transformed values  : %s%n",
                logT.subList(0, 5));
        System.out.printf("First 5 Box-Cox values          : %s%n",
                bcT.subList(0, 5));
        System.out.printf("First 5 first-difference values : %s%n",
                diff.subList(0, 5));
        System.out.printf("First 5 rolling-avg (window=5)  : %s%n",
                roll.subList(0, 5));

        // --- 6. ADF stationarity test ---
        System.out.printf("%nADF statistic : %.4f%n", ts.getADFStat());
        System.out.printf("Is stationary : %b%n", ts.isStationary());

        // --- 7. Stats utility (direct) ---
        List<Double> data = Arrays.asList(1.0, 2.0, 3.0, 4.0, 5.0);
        System.out.printf("%n[Stats util] mean=%.2f  var=%.2f  stdDev=%.2f%n",
                Stats.average(data), Stats.variance(data), Stats.standardDeviation(data));

        System.out.println();
    }

    // --- helper: deterministic random walk ---
    static double[] generateRandomWalk(int n, double drift, double sigma, long seed) {
        double[] out = new double[n];
        java.util.Random rng = new java.util.Random(seed);
        out[0] = 100.0;
        for (int i = 1; i < n; i++) {
            out[i] = out[i - 1] + drift + sigma * rng.nextGaussian();
        }
        return out;
    }
}
