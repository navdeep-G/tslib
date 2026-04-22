package tslib.api.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class SingleEtsRequest {
    @NotEmpty(message = "data must not be empty")
    private List<Double> data;

    @DecimalMin("0.01") @DecimalMax("1.0")
    private double alpha = 0.3;

    @Min(1) private int steps = 1;
    private double confidenceLevel = 0.95;

    public List<Double> getData() { return data; }
    public void setData(List<Double> data) { this.data = data; }
    public double getAlpha() { return alpha; }
    public void setAlpha(double alpha) { this.alpha = alpha; }
    public int getSteps() { return steps; }
    public void setSteps(int steps) { this.steps = steps; }
    public double getConfidenceLevel() { return confidenceLevel; }
    public void setConfidenceLevel(double confidenceLevel) { this.confidenceLevel = confidenceLevel; }
}
