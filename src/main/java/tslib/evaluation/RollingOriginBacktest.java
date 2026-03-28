package tslib.evaluation;

import java.util.ArrayList;
import java.util.List;

/**
 * Rolling-origin evaluation over a single univariate series.
 */
public class RollingOriginBacktest {

    private final int minTrainSize;
    private final int horizon;
    private final int stepSize;
    private final int seasonalPeriod;

    public RollingOriginBacktest(int minTrainSize, int horizon) {
        this(minTrainSize, horizon, 1, 1);
    }

    public RollingOriginBacktest(int minTrainSize, int horizon, int stepSize, int seasonalPeriod) {
        if (minTrainSize < 2 || horizon < 1 || stepSize < 1 || seasonalPeriod < 1) {
            throw new IllegalArgumentException("Backtest arguments must be positive and minTrainSize >= 2");
        }
        this.minTrainSize = minTrainSize;
        this.horizon = horizon;
        this.stepSize = stepSize;
        this.seasonalPeriod = seasonalPeriod;
    }

    public BacktestResult run(List<Double> data, ForecastFunction forecaster) {
        if (data == null || data.size() <= minTrainSize) {
            throw new IllegalArgumentException("Series is too short for the requested backtest");
        }
        List<Double> actual = new ArrayList<>();
        List<Double> forecast = new ArrayList<>();
        List<Integer> origins = new ArrayList<>();

        for (int origin = minTrainSize; origin < data.size(); origin += stepSize) {
            int remaining = data.size() - origin;
            int currentHorizon = Math.min(horizon, remaining);
            List<Double> train = new ArrayList<>(data.subList(0, origin));
            List<Double> prediction = forecaster.forecast(train, currentHorizon);
            List<Double> futureOnly = extractFuture(prediction, currentHorizon);
            for (int h = 0; h < currentHorizon; h++) {
                actual.add(data.get(origin + h));
                forecast.add(futureOnly.get(h));
                origins.add(origin);
            }
        }

        return new BacktestResult(actual, forecast, origins, data, seasonalPeriod);
    }

    private List<Double> extractFuture(List<Double> prediction, int currentHorizon) {
        if (prediction.size() < currentHorizon) {
            throw new IllegalArgumentException("Forecaster returned fewer values than the requested horizon");
        }
        return new ArrayList<>(prediction.subList(prediction.size() - currentHorizon, prediction.size()));
    }
}
