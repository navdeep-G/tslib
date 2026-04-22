package tslib.api.dto;

import java.util.List;

public class StlResponse {
    private List<Double> trend;
    private List<Double> seasonal;
    private List<Double> remainder;
    private List<Double> reconstructed;

    public List<Double> getTrend() { return trend; }
    public void setTrend(List<Double> trend) { this.trend = trend; }
    public List<Double> getSeasonal() { return seasonal; }
    public void setSeasonal(List<Double> seasonal) { this.seasonal = seasonal; }
    public List<Double> getRemainder() { return remainder; }
    public void setRemainder(List<Double> remainder) { this.remainder = remainder; }
    public List<Double> getReconstructed() { return reconstructed; }
    public void setReconstructed(List<Double> reconstructed) { this.reconstructed = reconstructed; }
}
