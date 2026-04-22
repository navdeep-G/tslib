package tslib.api.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class DoubleEtsRequest {
    @NotEmpty(message = "data must not be empty")
    private List<Double> data;

    @DecimalMin("0.01") @DecimalMax("1.0") private double alpha = 0.3;
    @DecimalMin("0.01") @DecimalMax("1.0") private double gamma = 0.1;
    private int initializationMethod = 0;
    @Min(1) private int steps = 1;
    private double confidenceLevel = 0.95;

    public List<Double> getData() { return data; }
    public void setData(List<Double> data) { this.data = data; }
    public double getAlpha() { return alpha; }
    public void setAlpha(double alpha) { this.alpha = alpha; }
    public double getGamma() { return gamma; }
    public void setGamma(double gamma) { this.gamma = gamma; }
    public int getInitializationMethod() { return initializationMethod; }
    public void setInitializationMethod(int initializationMethod) { this.initializationMethod = initializationMethod; }
    public int getSteps() { return steps; }
    public void setSteps(int steps) { this.steps = steps; }
    public double getConfidenceLevel() { return confidenceLevel; }
    public void setConfidenceLevel(double confidenceLevel) { this.confidenceLevel = confidenceLevel; }
}
