package tslib.api.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import java.util.List;

public class OutlierRequest {
    @NotEmpty(message = "data must not be empty")
    private List<Double> data;

    private String method = "Z_SCORE";
    @Positive private double threshold = 3.0;

    public List<Double> getData() { return data; }
    public void setData(List<Double> data) { this.data = data; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public double getThreshold() { return threshold; }
    public void setThreshold(double threshold) { this.threshold = threshold; }
}
