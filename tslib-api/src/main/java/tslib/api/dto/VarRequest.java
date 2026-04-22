package tslib.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class VarRequest {
    @NotEmpty(message = "series must not be empty")
    private List<List<Double>> series;

    @Min(1) private int steps = 1;
    private Integer lagOrder;
    @Min(1) private int maxLag = 5;

    public List<List<Double>> getSeries() { return series; }
    public void setSeries(List<List<Double>> series) { this.series = series; }
    public int getSteps() { return steps; }
    public void setSteps(int steps) { this.steps = steps; }
    public Integer getLagOrder() { return lagOrder; }
    public void setLagOrder(Integer lagOrder) { this.lagOrder = lagOrder; }
    public int getMaxLag() { return maxLag; }
    public void setMaxLag(int maxLag) { this.maxLag = maxLag; }
}
