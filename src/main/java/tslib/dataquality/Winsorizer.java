package tslib.dataquality;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Quantile winsorization helpers.
 */
public final class Winsorizer {

    private Winsorizer() {}

    public static List<Double> winsorize(List<Double> data, double lowerProbability, double upperProbability) {
        if (!(lowerProbability >= 0.0 && upperProbability <= 1.0 && lowerProbability < upperProbability)) {
            throw new IllegalArgumentException("Probabilities must satisfy 0 <= lower < upper <= 1");
        }
        List<Double> sorted = new ArrayList<>(data);
        Collections.sort(sorted);
        double lower = OutlierDetector.quantile(sorted, lowerProbability);
        double upper = OutlierDetector.quantile(sorted, upperProbability);
        List<Double> result = new ArrayList<>(data.size());
        for (double value : data) {
            result.add(Math.max(lower, Math.min(upper, value)));
        }
        return result;
    }
}
