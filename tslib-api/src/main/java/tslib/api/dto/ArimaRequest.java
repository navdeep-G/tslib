package tslib.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class ArimaRequest {
    @NotEmpty(message = "data must not be empty")
    private List<Double> data;

    @Min(0) private int p;
    @Min(0) private int d;
    @Min(0) private int q;
    @Min(1) private int steps = 1;
    private double confidenceLevel = 0.95;
    @Min(1) private int maxIterations = 200;
    private double tolerance = 1e-8;

    public List<Double> getData() { return data; }
    public void setData(List<Double> data) { this.data = data; }
    public int getP() { return p; }
    public void setP(int p) { this.p = p; }
    public int getD() { return d; }
    public void setD(int d) { this.d = d; }
    public int getQ() { return q; }
    public void setQ(int q) { this.q = q; }
    public int getSteps() { return steps; }
    public void setSteps(int steps) { this.steps = steps; }
    public double getConfidenceLevel() { return confidenceLevel; }
    public void setConfidenceLevel(double confidenceLevel) { this.confidenceLevel = confidenceLevel; }
    public int getMaxIterations() { return maxIterations; }
    public void setMaxIterations(int maxIterations) { this.maxIterations = maxIterations; }
    public double getTolerance() { return tolerance; }
    public void setTolerance(double tolerance) { this.tolerance = tolerance; }
}
