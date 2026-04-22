package tslib.api.dto;

import java.util.List;

public class ForecastResponse {
    private List<Double> forecasts;

    public ForecastResponse() {}
    public ForecastResponse(List<Double> forecasts) { this.forecasts = forecasts; }

    public List<Double> getForecasts() { return forecasts; }
    public void setForecasts(List<Double> forecasts) { this.forecasts = forecasts; }
}
