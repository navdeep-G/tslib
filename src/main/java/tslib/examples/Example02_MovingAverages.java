package tslib.examples;

import tslib.movingaverage.CumulativeMovingAverage;
import tslib.movingaverage.ExponentialMovingAverage;
import tslib.movingaverage.SimpleMovingAverage;
import tslib.movingaverage.WeightedMovingAverage;

import java.util.Arrays;
import java.util.List;

/**
 * Example 02: All four moving-average implementations.
 *
 * Covers batch compute() on a full series and the incremental add()/compute(double)
 * methods that let you process new observations one at a time.
 */
public class Example02_MovingAverages {

    public static void run() {
        System.out.println("=== Example 02: Moving Averages ===\n");

        List<Double> data = Arrays.asList(
                10.0, 12.0, 11.0, 13.0, 15.0, 14.0, 16.0, 18.0, 17.0, 19.0,
                21.0, 20.0, 22.0, 24.0, 23.0, 25.0, 27.0, 26.0, 28.0, 30.0);

        // --- 1. Simple Moving Average (period = 5) ---
        SimpleMovingAverage sma = new SimpleMovingAverage(5);
        List<Double> smaValues = sma.compute(data);
        System.out.println("SMA(5) [null until window fills]:");
        printSeries("SMA ", smaValues);

        // Incremental usage
        sma.reset();
        System.out.print("SMA incremental (add one-by-one): ");
        for (double v : data) sma.add(v);
        // After calling add() the last computed SMA is not directly retrieved via add(),
        // so we demonstrate re-computing on a growing list approach via compute():
        System.out.println(sma.compute(data).get(data.size() - 1));

        // --- 2. Exponential Moving Average (alpha = 0.3) ---
        ExponentialMovingAverage ema = new ExponentialMovingAverage(0.3);
        List<Double> emaValues = ema.compute(data);
        System.out.println("\nEMA(alpha=0.3):");
        printSeries("EMA ", emaValues);

        // EMA also supports single-value incremental mode
        ema.reset();
        System.out.print("EMA incremental (last 5 updates): ");
        double last = 0;
        for (double v : data) last = ema.compute(v);
        System.out.printf("final EMA=%.4f%n", last);

        // --- 3. Weighted Moving Average (period = 5, linear weights) ---
        WeightedMovingAverage wma = new WeightedMovingAverage(5);
        List<Double> wmaValues = wma.compute(data);
        System.out.println("\nWMA(5) [null until window fills]:");
        printSeries("WMA ", wmaValues);

        // --- 4. Cumulative Moving Average (running average of all seen values) ---
        CumulativeMovingAverage cma = new CumulativeMovingAverage();
        List<Double> cmaValues = cma.compute(data);
        System.out.println("\nCMA (running average over all values):");
        printSeries("CMA ", cmaValues);

        // Incremental CMA: add() returns the updated running average
        cma.reset();
        System.out.print("CMA incremental (every 5th update): ");
        for (int i = 0; i < data.size(); i++) {
            double running = cma.add(data.get(i));
            if ((i + 1) % 5 == 0) System.out.printf("[t=%d]=%.2f  ", i + 1, running);
        }
        System.out.println();

        System.out.println();
    }

    private static void printSeries(String label, List<Double> values) {
        System.out.print(label + ": ");
        for (Double v : values) {
            if (v == null) System.out.print(" null ");
            else System.out.printf("%.2f  ", v);
        }
        System.out.println();
    }
}
