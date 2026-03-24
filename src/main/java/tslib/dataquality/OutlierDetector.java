package tslib.dataquality;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Basic univariate outlier detection helpers.
 */
public final class OutlierDetector {

    private OutlierDetector() {}

    public static List<Integer> zScore(List<Double> data, double threshold) {
        if (threshold <= 0.0) {
            throw new IllegalArgumentException("Threshold must be > 0");
        }
        double mean = 0.0;
        for (double value : data) {
            mean += value;
        }
        mean /= data.size();
        double variance = 0.0;
        for (double value : data) {
            double diff = value - mean;
            variance += diff * diff;
        }
        variance /= Math.max(1, data.size() - 1);
        double std = Math.sqrt(Math.max(variance, 1e-12));
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            double z = Math.abs((data.get(i) - mean) / std);
            if (z > threshold) {
                result.add(i);
            }
        }
        return result;
    }

    public static List<Integer> iqr(List<Double> data, double multiplier) {
        if (multiplier <= 0.0) {
            throw new IllegalArgumentException("Multiplier must be > 0");
        }
        List<Double> sorted = new ArrayList<>(data);
        Collections.sort(sorted);
        double q1 = quantile(sorted, 0.25);
        double q3 = quantile(sorted, 0.75);
        double iqr = q3 - q1;
        double lower = q1 - multiplier * iqr;
        double upper = q3 + multiplier * iqr;
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            double value = data.get(i);
            if (value < lower || value > upper) {
                result.add(i);
            }
        }
        return result;
    }

    static double quantile(List<Double> sorted, double probability) {
        double position = probability * (sorted.size() - 1);
        int lowerIndex = (int) Math.floor(position);
        int upperIndex = (int) Math.ceil(position);
        if (lowerIndex == upperIndex) {
            return sorted.get(lowerIndex);
        }
        double weight = position - lowerIndex;
        return sorted.get(lowerIndex) * (1.0 - weight) + sorted.get(upperIndex) * weight;
    }
}
