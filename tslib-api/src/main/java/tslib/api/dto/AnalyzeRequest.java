package tslib.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class AnalyzeRequest {
    @NotEmpty(message = "data must not be empty")
    private List<Double> data;

    @Min(0) private int k = 1;
    @Min(1) private int n = 10;
    @Min(1) private int windowSize = 5;

    public List<Double> getData() { return data; }
    public void setData(List<Double> data) { this.data = data; }
    public int getK() { return k; }
    public void setK(int k) { this.k = k; }
    public int getN() { return n; }
    public void setN(int n) { this.n = n; }
    public int getWindowSize() { return windowSize; }
    public void setWindowSize(int windowSize) { this.windowSize = windowSize; }
}
