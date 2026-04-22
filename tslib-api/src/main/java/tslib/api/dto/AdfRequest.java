package tslib.api.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class AdfRequest {
    @NotEmpty(message = "data must not be empty")
    private List<Double> data;

    private Integer lag;

    public List<Double> getData() { return data; }
    public void setData(List<Double> data) { this.data = data; }
    public Integer getLag() { return lag; }
    public void setLag(Integer lag) { this.lag = lag; }
}
