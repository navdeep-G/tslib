package tslib.evaluation;

import java.util.ArrayList;
import java.util.List;

/**
 * Point forecasts paired with prediction intervals.
 */
public class IntervalForecast {

    private final List<Double> forecasts;
    private final List<PredictionInterval> intervals;

    public IntervalForecast(List<Double> forecasts, List<PredictionInterval> intervals) {
        this.forecasts = new ArrayList<>(forecasts);
        this.intervals = new ArrayList<>(intervals);
    }

    public List<Double> getForecasts() {
        return new ArrayList<>(forecasts);
    }

    public List<PredictionInterval> getIntervals() {
        return new ArrayList<>(intervals);
    }
}
