package tslib.model;

import java.io.Serializable;
import java.util.List;
import tslib.evaluation.IntervalForecast;

/**
 * Common contract for all univariate time-series forecasting models.
 *
 * <p>Implementors follow a two-step workflow:
 * <pre>
 *   model.fit(data).forecast(steps)
 *   model.fit(data).forecastWithIntervals(steps, 0.95)
 * </pre>
 *
 * <p>Calling {@link #forecast} or {@link #forecastWithIntervals} before {@link #fit}
 * must throw {@link IllegalStateException}.
 */
public interface TimeSeriesModel extends Serializable {

    TimeSeriesModel fit(List<Double> data);

    List<Double> forecast(int steps);

    IntervalForecast forecastWithIntervals(int steps, double confidenceLevel);
}
