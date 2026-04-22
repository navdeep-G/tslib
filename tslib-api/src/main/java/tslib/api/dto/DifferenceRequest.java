package tslib.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class DifferenceRequest {
    @NotEmpty(message = "data must not be empty")
    private List<Double> data;

    @Min(1) private int order = 1;
    private Integer lag;
    private List<Double> history;

    public List<Double> getData() { return data; }
    public void setData(List<Double> data) { this.data = data; }
    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }
    public Integer getLag() { return lag; }
    public void setLag(Integer lag) { this.lag = lag; }
    public List<Double> getHistory() { return history; }
    public void setHistory(List<Double> history) { this.history = history; }
}
