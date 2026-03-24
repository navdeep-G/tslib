package tslib.evaluation;

import java.util.List;

/**
 * Standard forecast accuracy metrics.
 */
public final class ForecastMetrics {

    private ForecastMetrics() {}

    public static double mae(List<Double> actual, List<Double> forecast) {
        validateComparable(actual, forecast);
        double total = 0.0;
        for (int i = 0; i < actual.size(); i++) {
            total += Math.abs(actual.get(i) - forecast.get(i));
        }
        return total / actual.size();
    }

    public static double rmse(List<Double> actual, List<Double> forecast) {
        validateComparable(actual, forecast);
        double total = 0.0;
        for (int i = 0; i < actual.size(); i++) {
            double error = actual.get(i) - forecast.get(i);
            total += error * error;
        }
        return Math.sqrt(total / actual.size());
    }

    public static double mape(List<Double> actual, List<Double> forecast) {
        validateComparable(actual, forecast);
        double total = 0.0;
        int count = 0;
        for (int i = 0; i < actual.size(); i++) {
            double denominator = Math.abs(actual.get(i));
            if (denominator > 1e-12) {
                total += Math.abs(actual.get(i) - forecast.get(i)) / denominator;
                count++;
            }
        }
        return count == 0 ? 0.0 : 100.0 * total / count;
    }

    public static double smape(List<Double> actual, List<Double> forecast) {
        validateComparable(actual, forecast);
        double total = 0.0;
        int count = 0;
        for (int i = 0; i < actual.size(); i++) {
            double denominator = Math.abs(actual.get(i)) + Math.abs(forecast.get(i));
            if (denominator > 1e-12) {
                total += 2.0 * Math.abs(actual.get(i) - forecast.get(i)) / denominator;
                count++;
            }
        }
        return count == 0 ? 0.0 : 100.0 * total / count;
    }

    public static double mase(List<Double> actual, List<Double> forecast, List<Double> trainingSeries, int seasonalPeriod) {
        validateComparable(actual, forecast);
        if (trainingSeries == null || trainingSeries.size() < seasonalPeriod + 1) {
            throw new IllegalArgumentException("Training series is too short for MASE scaling");
        }
        if (seasonalPeriod < 1) {
            throw new IllegalArgumentException("Seasonal period must be >= 1");
        }
        double scale = 0.0;
        int count = 0;
        for (int i = seasonalPeriod; i < trainingSeries.size(); i++) {
            scale += Math.abs(trainingSeries.get(i) - trainingSeries.get(i - seasonalPeriod));
            count++;
        }
        scale /= Math.max(1, count);
        if (scale <= 1e-12) {
            return 0.0;
        }
        return mae(actual, forecast) / scale;
    }

    public static double meanError(List<Double> actual, List<Double> forecast) {
        validateComparable(actual, forecast);
        double total = 0.0;
        for (int i = 0; i < actual.size(); i++) {
            total += actual.get(i) - forecast.get(i);
        }
        return total / actual.size();
    }

    private static void validateComparable(List<Double> actual, List<Double> forecast) {
        if (actual == null || forecast == null || actual.isEmpty() || actual.size() != forecast.size()) {
            throw new IllegalArgumentException("Actual and forecast series must be non-empty and have the same length");
        }
    }
}
