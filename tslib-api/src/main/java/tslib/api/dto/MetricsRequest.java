package tslib.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class MetricsRequest {
    @NotEmpty(message = "actual must not be empty")
    private List<Double> actual;

    @NotEmpty(message = "forecast must not be empty")
    private List<Double> forecast;

    private List<Double> trainingSeries;
    @Min(1) private int seasonalPeriod = 1;

    public List<Double> getActual() { return actual; }
    public void setActual(List<Double> actual) { this.actual = actual; }
    public List<Double> getForecast() { return forecast; }
    public void setForecast(List<Double> forecast) { this.forecast = forecast; }
    public List<Double> getTrainingSeries() { return trainingSeries; }
    public void setTrainingSeries(List<Double> trainingSeries) { this.trainingSeries = trainingSeries; }
    public int getSeasonalPeriod() { return seasonalPeriod; }
    public void setSeasonalPeriod(int seasonalPeriod) { this.seasonalPeriod = seasonalPeriod; }
}
