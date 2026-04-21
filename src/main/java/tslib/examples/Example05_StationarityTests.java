package tslib.examples;

import tslib.tests.AugmentedDickeyFuller;
import tslib.tests.KPSSTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Example 05: Stationarity testing with the Augmented Dickey-Fuller (ADF) test
 * and the KPSS test.
 *
 * ADF  — H0: unit root (non-stationary). Reject H0 ⟹ stationary.
 * KPSS — H0: stationary. Reject H0 ⟹ non-stationary (trend/unit-root).
 */
public class Example05_StationarityTests {

    public static void run() {
        System.out.println("=== Example 05: Stationarity Tests ===\n");

        List<Double> randomWalk = generateRandomWalk(100, 0.0, 1.0, 7);
        List<Double> stationary = generateAR1(100, 0.4, 1.0, 7);  // |phi| < 1 → stationary

        // --- 1. ADF on random walk ---
        System.out.println("-- ADF Test --");
        AugmentedDickeyFuller adfRW = new AugmentedDickeyFuller(randomWalk);
        System.out.printf("Random walk  |  stat=%.4f  p≈%.4f  isStationary=%b  lag=%d%n",
                adfRW.getAdfStat(), adfRW.getPValue(), adfRW.isStationary(), adfRW.getLag());

        AugmentedDickeyFuller adfStat = new AugmentedDickeyFuller(stationary);
        System.out.printf("AR(1) φ=0.4  |  stat=%.4f  p≈%.4f  isStationary=%b  lag=%d%n",
                adfStat.getAdfStat(), adfStat.getPValue(), adfStat.isStationary(), adfStat.getLag());

        // Manual lag selection
        AugmentedDickeyFuller adfManual = new AugmentedDickeyFuller(stationary, 3);
        System.out.printf("AR(1), lag=3 |  stat=%.4f  isStationary=%b%n",
                adfManual.getAdfStat(), adfManual.isStationary());

        System.out.println("\nisNeedsDiff (random walk) : " + adfRW.isNeedsDiff());
        System.out.println("isNeedsDiff (AR1 stat)    : " + adfStat.isNeedsDiff());

        // --- 2. KPSS on random walk ---
        System.out.println("\n-- KPSS Test --");

        KPSSTest kpssRW = new KPSSTest(randomWalk);
        System.out.printf("Random walk (LEVEL) |  stat=%.4f  stationaryAt5%%=%b  stationaryAt1%%=%b  lags=%d%n",
                kpssRW.getStatistic(),
                kpssRW.isStationaryAtFivePercent(),
                kpssRW.isStationaryAtOnePercent(),
                kpssRW.getLags());

        KPSSTest kpssStatLevel = new KPSSTest(stationary, KPSSTest.RegressionType.LEVEL);
        System.out.printf("AR(1) (LEVEL)       |  stat=%.4f  stationaryAt5%%=%b%n",
                kpssStatLevel.getStatistic(), kpssStatLevel.isStationaryAtFivePercent());

        KPSSTest kpssStatTrend = new KPSSTest(stationary, KPSSTest.RegressionType.TREND);
        System.out.printf("AR(1) (TREND)       |  stat=%.4f  stationaryAt5%%=%b%n",
                kpssStatTrend.getStatistic(), kpssStatTrend.isStationaryAtFivePercent());

        // Critical values
        System.out.printf("%nKPSS critical values (LEVEL):  1%%=%.4f  5%%=%.4f  10%%=%.4f%n",
                kpssStatLevel.getCriticalValue(0.01),
                kpssStatLevel.getCriticalValue(0.05),
                kpssStatLevel.getCriticalValue(0.10));

        // Manual lag selection
        KPSSTest kpssManual = new KPSSTest(stationary, KPSSTest.RegressionType.LEVEL, 5);
        System.out.printf("KPSS manual lags=5  |  stat=%.4f%n", kpssManual.getStatistic());

        System.out.println();
    }

    static List<Double> generateRandomWalk(int n, double drift, double sigma, long seed) {
        List<Double> out = new ArrayList<>(n);
        Random rng = new Random(seed);
        double val = 0.0;
        for (int i = 0; i < n; i++) {
            val += drift + sigma * rng.nextGaussian();
            out.add(val);
        }
        return out;
    }

    static List<Double> generateAR1(int n, double phi, double sigma, long seed) {
        List<Double> out = new ArrayList<>(n);
        Random rng = new Random(seed);
        double val = 0.0;
        for (int i = 0; i < n; i++) {
            val = phi * val + sigma * rng.nextGaussian();
            out.add(val);
        }
        return out;
    }
}
