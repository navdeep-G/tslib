package tslib.api.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class WinsorizerRequest {
    @NotEmpty(message = "data must not be empty")
    private List<Double> data;

    @DecimalMin("0.0") @DecimalMax("1.0")
    private double lowerProbability = 0.05;

    @DecimalMin("0.0") @DecimalMax("1.0")
    private double upperProbability = 0.95;

    public List<Double> getData() { return data; }
    public void setData(List<Double> data) { this.data = data; }
    public double getLowerProbability() { return lowerProbability; }
    public void setLowerProbability(double lowerProbability) { this.lowerProbability = lowerProbability; }
    public double getUpperProbability() { return upperProbability; }
    public void setUpperProbability(double upperProbability) { this.upperProbability = upperProbability; }
}
