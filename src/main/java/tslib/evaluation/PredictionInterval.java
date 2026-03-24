package tslib.evaluation;

/**
 * Prediction interval for a single forecast horizon.
 */
public class PredictionInterval {

    private final int step;
    private final double pointForecast;
    private final double lower;
    private final double upper;
    private final double confidenceLevel;

    public PredictionInterval(int step, double pointForecast, double lower, double upper, double confidenceLevel) {
        if (step < 1) {
            throw new IllegalArgumentException("Step must be >= 1");
        }
        if (!(confidenceLevel > 0.0 && confidenceLevel < 1.0)) {
            throw new IllegalArgumentException("Confidence level must be in (0, 1)");
        }
        this.step = step;
        this.pointForecast = pointForecast;
        this.lower = lower;
        this.upper = upper;
        this.confidenceLevel = confidenceLevel;
    }

    public int getStep() {
        return step;
    }

    public double getPointForecast() {
        return pointForecast;
    }

    public double getLower() {
        return lower;
    }

    public double getUpper() {
        return upper;
    }

    public double getConfidenceLevel() {
        return confidenceLevel;
    }
}
