package tslib.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class StlRequest {
    @NotEmpty(message = "data must not be empty")
    private List<Double> data;

    @Min(2) private int period;
    private Integer trendWindow;
    private Integer seasonalWindow;
    private Integer iterations;
    private Integer outerIterations;

    public List<Double> getData() { return data; }
    public void setData(List<Double> data) { this.data = data; }
    public int getPeriod() { return period; }
    public void setPeriod(int period) { this.period = period; }
    public Integer getTrendWindow() { return trendWindow; }
    public void setTrendWindow(Integer trendWindow) { this.trendWindow = trendWindow; }
    public Integer getSeasonalWindow() { return seasonalWindow; }
    public void setSeasonalWindow(Integer seasonalWindow) { this.seasonalWindow = seasonalWindow; }
    public Integer getIterations() { return iterations; }
    public void setIterations(Integer iterations) { this.iterations = iterations; }
    public Integer getOuterIterations() { return outerIterations; }
    public void setOuterIterations(Integer outerIterations) { this.outerIterations = outerIterations; }
}
