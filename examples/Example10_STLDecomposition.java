import tslib.decomposition.STLDecomposition;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Example 10: STL decomposition — Seasonal-Trend Decomposition using Loess.
 *
 * Breaks a time series into:
 *   trend    — long-run direction
 *   seasonal — repeating pattern of fixed period
 *   remainder — residual noise
 *
 * Robust mode (outerIterations > 0) uses bisquare re-weighting to reduce
 * the influence of outliers on the decomposition.
 */
public class Example10_STLDecomposition {

    public static void run() {
        System.out.println("=== Example 10: STL Decomposition ===\n");

        // Monthly data: 3 years of retail sales with seasonal pattern + upward trend
        int period = 12;
        List<Double> series = generateSeasonalSeries(period * 3, period, 100.0, 2.0, 30.0, 5.0, 10);

        System.out.println("Input series (first 24 values):");
        System.out.println(fmt(series.subList(0, 24)));

        // --- 1. Basic STL with default window sizes ---
        System.out.println("\n-- STL(period=12) default windows --");
        STLDecomposition stl = new STLDecomposition(period);
        STLDecomposition.Result r = stl.decompose(series);

        System.out.println("Trend    [0:12]: " + fmt(r.getTrend().subList(0, 12)));
        System.out.println("Seasonal [0:12]: " + fmt(r.getSeasonal().subList(0, 12)));
        System.out.println("Remainder[0:12]: " + fmt(r.getRemainder().subList(0, 12)));

        // Sanity check: reconstruct ≈ original
        List<Double> reconstructed = r.reconstruct();
        double maxErr = 0.0;
        for (int i = 0; i < series.size(); i++) {
            maxErr = Math.max(maxErr, Math.abs(series.get(i) - reconstructed.get(i)));
        }
        System.out.printf("Max reconstruction error: %.2e (should be ~0)%n", maxErr);

        // --- 2. Custom window sizes ---
        System.out.println("\n-- STL(period=12, trendWindow=13, seasonalWindow=7, iterations=3) --");
        STLDecomposition stlCustom = new STLDecomposition(period, 13, 7, 3);
        STLDecomposition.Result rc = stlCustom.decompose(series);
        System.out.println("Trend    [0:6] : " + fmt(rc.getTrend().subList(0, 6)));
        System.out.println("Seasonal [0:6] : " + fmt(rc.getSeasonal().subList(0, 6)));

        // Seasonal pattern repeats — compare cycle 1 vs cycle 2
        System.out.println("\nSeasonal (cycle 1, months 0-11): "
                + fmt(rc.getSeasonal().subList(0, 12)));
        System.out.println("Seasonal (cycle 2, months 12-23): "
                + fmt(rc.getSeasonal().subList(12, 24)));

        // --- 3. Robust STL with outer iterations ---
        System.out.println("\n-- STL robust (outerIterations=2) — handles outliers better --");
        List<Double> withSpike = new ArrayList<>(series);
        withSpike.set(15, withSpike.get(15) + 150.0);  // inject a spike at month 16
        withSpike.set(30, withSpike.get(30) - 120.0);  // inject a dip at month 31

        STLDecomposition stlRobust = new STLDecomposition(period, 13, 7, 2, 2);
        STLDecomposition.Result rr = stlRobust.decompose(withSpike);

        // Non-robust for comparison
        STLDecomposition stlPlain = new STLDecomposition(period, 13, 7, 2, 0);
        STLDecomposition.Result rp = stlPlain.decompose(withSpike);

        // Robust STL should absorb outliers into remainder, keeping trend clean
        System.out.printf("Trend at spike (robust) : %.2f%n", rr.getTrend().get(15));
        System.out.printf("Trend at spike (plain)  : %.2f%n", rp.getTrend().get(15));
        System.out.printf("Remainder at spike (robust): %.2f%n", rr.getRemainder().get(15));
        System.out.printf("Remainder at spike (plain) : %.2f%n", rp.getRemainder().get(15));

        System.out.println();
    }

    static List<Double> generateSeasonalSeries(int n, int period, double base, double slope,
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
