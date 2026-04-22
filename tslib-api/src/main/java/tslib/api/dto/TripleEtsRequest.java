package tslib.api.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class TripleEtsRequest {
    @NotEmpty(message = "data must not be empty")
    private List<Double> data;

    @DecimalMin("0.0") @DecimalMax("1.0") private double alpha = 0.3;
    @DecimalMin("0.0") @DecimalMax("1.0") private double beta = 0.1;
    @DecimalMin("0.0") @DecimalMax("1.0") private double gamma = 0.1;
    @Min(2) private int period = 12;
    @Min(1) private int steps = 1;
    private double confidenceLevel = 0.95;

    public List<Double> getData() { return data; }
    public void setData(List<Double> data) { this.data = data; }
    public double getAlpha() { return alpha; }
    public void setAlpha(double alpha) { this.alpha = alpha; }
    public double getBeta() { return beta; }
    public void setBeta(double beta) { this.beta = beta; }
    public double getGamma() { return gamma; }
    public void setGamma(double gamma) { this.gamma = gamma; }
    public int getPeriod() { return period; }
    public void setPeriod(int period) { this.period = period; }
    public int getSteps() { return steps; }
    public void setSteps(int steps) { this.steps = steps; }
    public double getConfidenceLevel() { return confidenceLevel; }
    public void setConfidenceLevel(double confidenceLevel) { this.confidenceLevel = confidenceLevel; }
}
