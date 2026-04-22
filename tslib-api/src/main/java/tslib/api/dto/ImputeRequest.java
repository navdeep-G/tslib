package tslib.api.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class ImputeRequest {
    @NotEmpty(message = "data must not be empty")
    private List<Double> data;

    @NotBlank(message = "strategy must not be blank")
    private String strategy = "LINEAR_INTERPOLATION";

    public List<Double> getData() { return data; }
    public void setData(List<Double> data) { this.data = data; }
    public String getStrategy() { return strategy; }
    public void setStrategy(String strategy) { this.strategy = strategy; }
}
