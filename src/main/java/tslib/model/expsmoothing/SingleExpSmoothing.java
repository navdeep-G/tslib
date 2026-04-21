package tslib.model.expsmoothing;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Single Exponential Smoothing implementation.
 */
public class SingleExpSmoothing implements ExponentialSmoothing, Serializable {

    private static final long serialVersionUID = 1L;

    private final double alpha;

    public SingleExpSmoothing(double alpha) {
        if (alpha <= 0 || alpha > 1) {
            throw new IllegalArgumentException("Alpha must be in (0, 1]");
        }
        this.alpha = alpha;
    }

    @Override
    public List<Double> forecast(List<Double> data, int steps) {
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("Input data must not be null or empty.");
        }

        int n = data.size();
        List<Double> result = new ArrayList<>(n + steps);

        // Initialize first smoothed value
        double smoothed = data.get(0);
        result.add(smoothed);

        // Apply smoothing to historical data
        for (int i = 1; i < n; i++) {
            smoothed = alpha * data.get(i) + (1 - alpha) * smoothed;
            result.add(smoothed);
        }

        // Forecast future values using last smoothed value
        for (int i = 0; i < steps; i++) {
            result.add(smoothed);
        }

        return result;
    }
}
