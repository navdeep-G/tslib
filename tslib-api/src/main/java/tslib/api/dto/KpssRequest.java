package tslib.api.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class KpssRequest {
    @NotEmpty(message = "data must not be empty")
    private List<Double> data;

    private String regressionType = "LEVEL";
    private Integer lags;

    public List<Double> getData() { return data; }
    public void setData(List<Double> data) { this.data = data; }
    public String getRegressionType() { return regressionType; }
    public void setRegressionType(String regressionType) { this.regressionType = regressionType; }
    public Integer getLags() { return lags; }
    public void setLags(Integer lags) { this.lags = lags; }
}
