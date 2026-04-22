package tslib.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class AutoEtsRequest {
    @NotEmpty(message = "data must not be empty")
    private List<Double> data;

    @Min(1) private int steps = 1;
    private double confidenceLevel = 0.95;
    private Integer seasonalPeriod;

    public List<Double> getData() { return data; }
    public void setData(List<Double> data) { this.data = data; }
    public int getSteps() { return steps; }
    public void setSteps(int steps) { this.steps = steps; }
    public double getConfidenceLevel() { return confidenceLevel; }
    public void setConfidenceLevel(double confidenceLevel) { this.confidenceLevel = confidenceLevel; }
    public Integer getSeasonalPeriod() { return seasonalPeriod; }
    public void setSeasonalPeriod(Integer seasonalPeriod) { this.seasonalPeriod = seasonalPeriod; }
}
