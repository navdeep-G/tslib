package tslib.api.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class TransformRequest {
    @NotEmpty(message = "data must not be empty")
    private List<Double> data;

    private Double lambda;
    private Double r;
    private Double lowerBound;
    private Double upperBound;

    public List<Double> getData() { return data; }
    public void setData(List<Double> data) { this.data = data; }
    public Double getLambda() { return lambda; }
    public void setLambda(Double lambda) { this.lambda = lambda; }
    public Double getR() { return r; }
    public void setR(Double r) { this.r = r; }
    public Double getLowerBound() { return lowerBound; }
    public void setLowerBound(Double lowerBound) { this.lowerBound = lowerBound; }
    public Double getUpperBound() { return upperBound; }
    public void setUpperBound(Double upperBound) { this.upperBound = upperBound; }
}
