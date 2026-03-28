package tslib.dataquality;

import java.util.ArrayList;
import java.util.List;

/**
 * Missing-value imputation helpers.
 */
public final class MissingValueImputer {

    public enum Strategy {
        FORWARD_FILL,
        BACKWARD_FILL,
        LINEAR_INTERPOLATION,
        MEAN
    }

    private MissingValueImputer() {}

    public static List<Double> impute(List<Double> data, Strategy strategy) {
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("Data must not be null or empty");
        }
        return switch (strategy) {
            case FORWARD_FILL -> forwardFill(data);
            case BACKWARD_FILL -> backwardFill(data);
            case LINEAR_INTERPOLATION -> linearInterpolation(data);
            case MEAN -> meanFill(data);
        };
    }

    public static List<Double> forwardFill(List<Double> data) {
        List<Double> result = new ArrayList<>(data.size());
        Double last = null;
        for (Double value : data) {
            if (value != null && !Double.isNaN(value)) {
                last = value;
            }
            result.add(last == null ? 0.0 : last);
        }
        return result;
    }

    public static List<Double> backwardFill(List<Double> data) {
        List<Double> result = new ArrayList<>(data);
        Double next = null;
        for (int i = data.size() - 1; i >= 0; i--) {
            Double value = data.get(i);
            if (value != null && !Double.isNaN(value)) {
                next = value;
                result.set(i, value);
            } else {
                result.set(i, next == null ? 0.0 : next);
            }
        }
        return result;
    }

    public static List<Double> meanFill(List<Double> data) {
        double total = 0.0;
        int count = 0;
        for (Double value : data) {
            if (value != null && !Double.isNaN(value)) {
                total += value;
                count++;
            }
        }
        double mean = count == 0 ? 0.0 : total / count;
        List<Double> result = new ArrayList<>(data.size());
        for (Double value : data) {
            result.add(value == null || Double.isNaN(value) ? mean : value);
        }
        return result;
    }

    public static List<Double> linearInterpolation(List<Double> data) {
        List<Double> result = new ArrayList<>(data.size());
        for (Double value : data) {
            result.add(value == null || Double.isNaN(value) ? null : value);
        }
        int i = 0;
        while (i < result.size()) {
            if (result.get(i) != null) {
                i++;
                continue;
            }
            int start = i - 1;
            int end = i;
            while (end < result.size() && result.get(end) == null) {
                end++;
            }
            double left = start >= 0 ? result.get(start) : (end < result.size() ? result.get(end) : 0.0);
            double right = end < result.size() ? result.get(end) : left;
            int gap = end - start;
            for (int j = i; j < end; j++) {
                double weight = (j - start) / (double) gap;
                result.set(j, left + weight * (right - left));
            }
            i = end;
        }
        return result;
    }
}
