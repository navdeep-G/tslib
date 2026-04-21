import tslib.transform.Differencing;
import tslib.transform.Transform;

import java.util.Arrays;
import java.util.List;

/**
 * Example 04: Time series transformations — log, sqrt, Box-Cox, differencing, and
 * their inverses.
 */
public class Example04_Transformations {

    public static void run() {
        System.out.println("=== Example 04: Transformations ===\n");

        List<Double> data = Arrays.asList(
                1.0, 4.0, 9.0, 16.0, 25.0, 36.0, 49.0, 64.0, 81.0, 100.0);
        System.out.println("Original: " + data);

        // --- 1. Basic transformations ---
        System.out.println("\n-- Algebraic transforms --");
        System.out.println("log(x)        : " + fmt(Transform.log(data)));
        System.out.println("sqrt(x)       : " + fmt(Transform.sqrt(data)));
        System.out.println("cbrt(x)       : " + fmt(Transform.cbrt(data)));
        System.out.println("root(x, 4)    : " + fmt(Transform.root(data, 4.0)));

        // --- 2. Box-Cox with explicit lambda ---
        System.out.println("\n-- Box-Cox transforms --");
        System.out.println("boxCox(lambda=0)   : " + fmt(Transform.boxCox(data, 0.0)));
        System.out.println("boxCox(lambda=0.5) : " + fmt(Transform.boxCox(data, 0.5)));
        System.out.println("boxCox(lambda=1)   : " + fmt(Transform.boxCox(data, 1.0)));

        // Automatic lambda search
        double lambda = Transform.boxCoxLambdaSearch(data);
        System.out.printf("Auto lambda        : %.4f%n", lambda);
        List<Double> bcAuto = Transform.boxCox(data);
        System.out.println("boxCox(auto lambda): " + fmt(bcAuto));

        // Custom search range
        double lambdaNarrow = Transform.boxCoxLambdaSearch(data, 0.0, 1.0);
        System.out.printf("Auto lambda [0,1]  : %.4f%n", lambdaNarrow);

        // Inverse Box-Cox
        List<Double> invBc = Transform.inverseBoxCox(bcAuto, lambda);
        System.out.println("Inverse Box-Cox    : " + fmt(invBc));

        // --- 3. Differencing ---
        List<Double> series = Arrays.asList(
                10.0, 13.0, 17.0, 22.0, 28.0, 35.0, 43.0, 52.0, 62.0, 73.0);
        System.out.println("\n-- Differencing --");
        System.out.println("Original        : " + series);

        List<Double> d1 = Differencing.difference(series);
        System.out.println("1st difference  : " + d1);

        List<Double> d2 = Differencing.difference(series, 2);
        System.out.println("2nd difference  : " + d2);

        // Inverse differencing recovers the original scale
        List<Double> inv1 = Differencing.inverseDifference(d1, series);
        System.out.println("Inverse diff (1): " + inv1);

        // --- 4. Seasonal differencing ---
        List<Double> seasonal = Arrays.asList(
                10.0, 12.0, 9.0, 11.0,  // period 4, cycle 1
                14.0, 16.0, 13.0, 15.0, // cycle 2
                18.0, 20.0, 17.0, 19.0, // cycle 3
                22.0, 24.0, 21.0, 23.0);// cycle 4
        System.out.println("\n-- Seasonal Differencing (period=4) --");
        System.out.println("Original          : " + seasonal);

        List<Double> sd = Differencing.seasonalDifference(seasonal, 4);
        System.out.println("Seasonal diff (1) : " + sd);

        List<Double> sd2 = Differencing.seasonalDifference(seasonal, 4, 2);
        System.out.println("Seasonal diff (2) : " + sd2);

        System.out.println();
    }

    private static List<Double> fmt(List<Double> list) {
        // Return rounded list for readable output
        List<Double> rounded = new java.util.ArrayList<>(list.size());
        for (Double v : list) rounded.add(v == null ? null : Math.round(v * 1000.0) / 1000.0);
        return rounded;
    }
}
