package tslib.examples;

import tslib.dataquality.MissingValueImputer;
import tslib.dataquality.OutlierDetector;
import tslib.dataquality.Winsorizer;

import java.util.Arrays;
import java.util.List;

/**
 * Example 03: Data quality — missing value imputation, outlier detection, and Winsorization.
 */
public class Example03_DataQuality {

    public static void run() {
        System.out.println("=== Example 03: Data Quality ===\n");

        // --- 1. Missing Value Imputation ---
        List<Double> withNulls = Arrays.asList(
                1.0, 2.0, null, null, 5.0, 6.0, null, 8.0, 9.0, 10.0);
        System.out.println("Original (with nulls): " + withNulls);

        List<Double> ff = MissingValueImputer.impute(withNulls, MissingValueImputer.Strategy.FORWARD_FILL);
        System.out.println("Forward fill         : " + ff);

        List<Double> bf = MissingValueImputer.impute(withNulls, MissingValueImputer.Strategy.BACKWARD_FILL);
        System.out.println("Backward fill        : " + bf);

        List<Double> li = MissingValueImputer.impute(withNulls, MissingValueImputer.Strategy.LINEAR_INTERPOLATION);
        System.out.printf("Linear interpolation : %s%n", li);

        List<Double> mf = MissingValueImputer.impute(withNulls, MissingValueImputer.Strategy.MEAN);
        System.out.printf("Mean fill            : %s%n", mf);

        // Also available as direct static methods:
        System.out.println("\n[Direct method] forwardFill : " + MissingValueImputer.forwardFill(withNulls));
        System.out.println("[Direct method] backwardFill: " + MissingValueImputer.backwardFill(withNulls));
        System.out.println("[Direct method] linearInterp: " + MissingValueImputer.linearInterpolation(withNulls));
        System.out.println("[Direct method] meanFill    : " + MissingValueImputer.meanFill(withNulls));

        // --- 2. Outlier Detection ---
        List<Double> withOutliers = Arrays.asList(
                10.0, 11.0, 10.5, 9.8, 10.2, 100.0,   // 100 is a spike
                10.1, 11.2, 10.8, 9.9, 10.3, -80.0,   // -80 is a dip
                10.6, 10.4, 11.0, 10.7, 10.3, 10.9, 11.1, 10.0);
        System.out.println("\nData (with outliers at indices 5 and 11):");
        System.out.println(withOutliers);

        List<Integer> zOutliers = OutlierDetector.zScore(withOutliers, 2.5);
        System.out.println("Z-score outliers (threshold=2.5) at indices: " + zOutliers);

        List<Integer> iqrOutliers = OutlierDetector.iqr(withOutliers, 1.5);
        System.out.println("IQR outliers (multiplier=1.5) at indices    : " + iqrOutliers);

        // --- 3. Winsorization ---
        List<Double> skewed = Arrays.asList(
                1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 200.0,
                1.5, 2.5, 3.5, 4.5, 5.5, 6.5, 7.5, 8.5, 9.5, -100.0);
        System.out.println("\nBefore Winsorization (note 200 and -100): " + skewed);

        List<Double> clipped = Winsorizer.winsorize(skewed, 0.05, 0.95);
        System.out.println("After  Winsorize(5%, 95%)               : " + clipped);

        System.out.println();
    }
}
