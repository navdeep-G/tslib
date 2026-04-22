package tslib.api.dto;

import tslib.evaluation.IntervalForecast;

import java.util.List;
import java.util.stream.Collectors;

public class IntervalForecastResponse {
    private List<Double> forecasts;
    private List<PredictionIntervalDto> intervals;

    public IntervalForecastResponse() {}

    public static IntervalForecastResponse from(IntervalForecast ivf) {
        var resp = new IntervalForecastResponse();
        resp.forecasts = ivf.getForecasts();
        resp.intervals = ivf.getIntervals().stream()
                .map(PredictionIntervalDto::from)
                .collect(Collectors.toList());
        return resp;
    }

    public List<Double> getForecasts() { return forecasts; }
    public List<PredictionIntervalDto> getIntervals() { return intervals; }
}
