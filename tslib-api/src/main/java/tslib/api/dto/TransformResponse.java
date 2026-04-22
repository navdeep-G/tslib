package tslib.api.dto;

import java.util.List;

public class TransformResponse {
    private List<Double> result;
    private Double lambda;

    public List<Double> getResult() { return result; }
    public void setResult(List<Double> result) { this.result = result; }
    public Double getLambda() { return lambda; }
    public void setLambda(Double lambda) { this.lambda = lambda; }
}
