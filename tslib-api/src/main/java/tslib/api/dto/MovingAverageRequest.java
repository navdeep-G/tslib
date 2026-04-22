package tslib.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class MovingAverageRequest {
    @NotEmpty(message = "data must not be empty")
    private List<Double> data;

    @Min(1) private int period = 5;
    private String type = "SIMPLE";

    // EMA-specific
    private Double alpha;

    // WMA — uses period for window size

    public List<Double> getData() { return data; }
    public void setData(List<Double> data) { this.data = data; }
    public int getPeriod() { return period; }
    public void setPeriod(int period) { this.period = period; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Double getAlpha() { return alpha; }
    public void setAlpha(Double alpha) { this.alpha = alpha; }
}
