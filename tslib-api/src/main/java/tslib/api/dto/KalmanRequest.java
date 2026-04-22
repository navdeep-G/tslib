package tslib.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import java.util.List;

public class KalmanRequest {
    @NotEmpty(message = "data must not be empty")
    private List<Double> data;

    @Positive private double processVariance = 1.0;
    @Positive private double observationVariance = 1.0;
    @Min(0) private int steps = 0;

    public List<Double> getData() { return data; }
    public void setData(List<Double> data) { this.data = data; }
    public double getProcessVariance() { return processVariance; }
    public void setProcessVariance(double processVariance) { this.processVariance = processVariance; }
    public double getObservationVariance() { return observationVariance; }
    public void setObservationVariance(double observationVariance) { this.observationVariance = observationVariance; }
    public int getSteps() { return steps; }
    public void setSteps(int steps) { this.steps = steps; }
}
