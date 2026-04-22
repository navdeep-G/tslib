package tslib.model.expsmoothing;

import java.util.List;
import tslib.evaluation.IntervalForecast;
import tslib.model.TimeSeriesModel;

public interface ExponentialSmoothing extends TimeSeriesModel {

    /** Fit the model and return {@code this} for chaining. */
    ExponentialSmoothing fit(List<Double> data);

    /** Forecast {@code steps} future values from the fitted model. */
    List<Double> forecast(int steps);

    /** Forecast with prediction intervals from the fitted model. */
    IntervalForecast forecastWithIntervals(int steps, double confidenceLevel);

    /**
     * Stateless convenience: fit on {@code data} then return in-sample fitted values
     * followed by {@code steps} future forecasts.
     */
    List<Double> forecast(List<Double> data, int steps);
}
