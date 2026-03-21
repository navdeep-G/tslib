package tslib.transform;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Differencing helpers for ARIMA-style workflows.
 */
public final class Differencing {

    private Differencing() {}

    public static List<Double> difference(List<Double> data) {
        return difference(data, 1);
    }

    public static List<Double> difference(List<Double> data, int order) {
        validateData(data);
        if (order < 0) {
            throw new IllegalArgumentException("Order must be >= 0");
        }
        if (order == 0) {
            return new ArrayList<>(data);
        }

        List<Double> result = new ArrayList<>(data);
        for (int k = 0; k < order; k++) {
            if (result.size() < 2) {
                return Collections.emptyList();
            }
            List<Double> next = new ArrayList<>(result.size() - 1);
            for (int i = 1; i < result.size(); i++) {
                next.add(result.get(i) - result.get(i - 1));
            }
            result = next;
        }
        return result;
    }

    public static List<Double> seasonalDifference(List<Double> data, int lag) {
        return seasonalDifference(data, lag, 1);
    }

    public static List<Double> seasonalDifference(List<Double> data, int lag, int order) {
        validateData(data);
        if (lag < 1) {
            throw new IllegalArgumentException("Lag must be >= 1");
        }
        if (order < 0) {
            throw new IllegalArgumentException("Order must be >= 0");
        }
        if (order == 0) {
            return new ArrayList<>(data);
        }

        List<Double> result = new ArrayList<>(data);
        for (int k = 0; k < order; k++) {
            if (result.size() <= lag) {
                return Collections.emptyList();
            }
            List<Double> next = new ArrayList<>(result.size() - lag);
            for (int i = lag; i < result.size(); i++) {
                next.add(result.get(i) - result.get(i - lag));
            }
            result = next;
        }
        return result;
    }

    public static List<Double> inverseDifference(List<Double> differenced, List<Double> history) {
        return inverseDifference(differenced, history, 1);
    }

    public static List<Double> inverseDifference(List<Double> differenced, List<Double> history, int order) {
        validateData(history);
        if (differenced == null) {
            throw new IllegalArgumentException("Differenced data must not be null");
        }
        if (order < 0) {
            throw new IllegalArgumentException("Order must be >= 0");
        }
        if (order == 0) {
            return new ArrayList<>(differenced);
        }
        if (history.size() < order) {
            throw new IllegalArgumentException("History must contain at least 'order' original observations");
        }

        List<Double> result = new ArrayList<>(differenced);
        double[] anchors = buildAnchors(history, order);
        for (int level = order - 1; level >= 0; level--) {
            List<Double> restored = new ArrayList<>(result.size());
            double previous = anchors[level];
            for (double delta : result) {
                previous += delta;
                restored.add(previous);
            }
            result = restored;
        }
        return result;
    }

    private static double[] buildAnchors(List<Double> history, int order) {
        double[] anchors = new double[order];
        anchors[0] = history.get(history.size() - 1);

        List<Double> current = new ArrayList<>(history);
        for (int level = 1; level < order; level++) {
            current = difference(current, 1);
            if (current.isEmpty()) {
                throw new IllegalArgumentException("History is too short to invert the requested differencing order");
            }
            anchors[level] = current.get(current.size() - 1);
        }
        return anchors;
    }

    private static void validateData(List<Double> data) {
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("Input data must not be null or empty.");
        }
    }
}
