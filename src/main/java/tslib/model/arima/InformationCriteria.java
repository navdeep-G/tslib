package tslib.model.arima;

/**
 * Information-criterion helpers for manual model comparison.
 */
public final class InformationCriteria {

    private InformationCriteria() {}

    public static double aic(double rss, int n, int parameterCount) {
        validateInputs(rss, n, parameterCount);
        return n * Math.log(rss / n) + 2.0 * parameterCount;
    }

    public static double bic(double rss, int n, int parameterCount) {
        validateInputs(rss, n, parameterCount);
        return n * Math.log(rss / n) + parameterCount * Math.log(n);
    }

    public static double aicc(double rss, int n, int parameterCount) {
        validateInputs(rss, n, parameterCount);
        double aic = aic(rss, n, parameterCount);
        if (n <= parameterCount + 1) {
            return Double.POSITIVE_INFINITY;
        }
        return aic + (2.0 * parameterCount * (parameterCount + 1)) / (n - parameterCount - 1.0);
    }

    private static void validateInputs(double rss, int n, int parameterCount) {
        if (!(rss > 0.0)) {
            throw new IllegalArgumentException("RSS must be > 0");
        }
        if (n < 1) {
            throw new IllegalArgumentException("Sample size must be >= 1");
        }
        if (parameterCount < 1) {
            throw new IllegalArgumentException("Parameter count must be >= 1");
        }
    }
}
