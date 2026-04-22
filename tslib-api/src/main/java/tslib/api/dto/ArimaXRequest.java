package tslib.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class ArimaXRequest {
    @NotEmpty(message = "data must not be empty")
    private List<Double> data;

    @NotNull(message = "exogenous must not be null")
    private List<List<Double>> exogenous;

    @NotNull(message = "futureExogenous must not be null")
    private List<List<Double>> futureExogenous;

    @Min(0) private int p;
    @Min(0) private int d;
    @Min(0) private int q;
    @Min(1) private int maxIterations = 200;
    private double tolerance = 1e-8;

    public List<Double> getData() { return data; }
    public void setData(List<Double> data) { this.data = data; }
    public List<List<Double>> getExogenous() { return exogenous; }
    public void setExogenous(List<List<Double>> exogenous) { this.exogenous = exogenous; }
    public List<List<Double>> getFutureExogenous() { return futureExogenous; }
    public void setFutureExogenous(List<List<Double>> futureExogenous) { this.futureExogenous = futureExogenous; }
    public int getP() { return p; }
    public void setP(int p) { this.p = p; }
    public int getD() { return d; }
    public void setD(int d) { this.d = d; }
    public int getQ() { return q; }
    public void setQ(int q) { this.q = q; }
    public int getMaxIterations() { return maxIterations; }
    public void setMaxIterations(int maxIterations) { this.maxIterations = maxIterations; }
    public double getTolerance() { return tolerance; }
    public void setTolerance(double tolerance) { this.tolerance = tolerance; }
}
