package tslib.stats;

import java.util.List;

/**
 * Compatibility facade that mirrors the package name used in the documentation.
 */
public final class Stats {
    private Stats() {}

    public static double average(List<Double> data) { return tslib.util.Stats.average(data); }
    public static double variance(List<Double> data) { return tslib.util.Stats.variance(data); }
    public static double standardDeviation(List<Double> data) { return tslib.util.Stats.standardDeviation(data); }
    public static int getMinimumIndex(List<Double> data) { return tslib.util.Stats.getMinimumIndex(data); }
    public static int getMaximumIndex(List<Double> data) { return tslib.util.Stats.getMaximumIndex(data); }
    public static double getMinimum(List<Double> data) { return tslib.util.Stats.getMinimum(data); }
    public static double getMaximum(List<Double> data) { return tslib.util.Stats.getMaximum(data); }
    public static double[] getMinMax(List<Double> data) { return tslib.util.Stats.getMinMax(data); }
    public static int[] getMinMaxIndices(List<Double> data) { return tslib.util.Stats.getMinMaxIndices(data); }
    public static double getAutoCovariance(List<Double> data, int k) { return tslib.util.Stats.getAutoCovariance(data, k); }
    public static double getAutoCorrelation(List<Double> data, int k) { return tslib.util.Stats.getAutoCorrelation(data, k); }
    public static double[] getAcf(List<Double> data, int n) { return tslib.util.Stats.getAcf(data, n); }
    public static double[] getPacf(List<Double> data, int maxLag) { return tslib.util.Stats.getPacf(data, maxLag); }
}
