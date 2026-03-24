package tslib.evaluation;

import java.util.ArrayList;
import java.util.List;

/**
 * Flattened rolling-origin backtest results.
 */
public class BacktestResult {

    private final List<Double> actual;
    private final List<Double> forecast;
    private final List<Integer> origins;
    private final List<Double> fullSeries;
    private final int seasonalPeriod;

    public BacktestResult(
            List<Double> actual,
            List<Double> forecast,
            List<Integer> origins,
            List<Double> fullSeries,
            int seasonalPeriod) {
        this.actual = new ArrayList<>(actual);
        this.forecast = new ArrayList<>(forecast);
        this.origins = new ArrayList<>(origins);
        this.fullSeries = new ArrayList<>(fullSeries);
        this.seasonalPeriod = seasonalPeriod;
    }

    public List<Double> getActual() {
        return new ArrayList<>(actual);
    }

    public List<Double> getForecast() {
        return new ArrayList<>(forecast);
    }

    public List<Integer> getOrigins() {
        return new ArrayList<>(origins);
    }

    public double getMae() {
        return ForecastMetrics.mae(actual, forecast);
    }

    public double getRmse() {
        return ForecastMetrics.rmse(actual, forecast);
    }

    public double getMape() {
        return ForecastMetrics.mape(actual, forecast);
    }

    public double getSmape() {
        return ForecastMetrics.smape(actual, forecast);
    }

    public double getMase() {
        return ForecastMetrics.mase(actual, forecast, fullSeries, seasonalPeriod);
    }
}
