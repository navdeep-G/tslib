package tslib.api.dto;

import java.util.List;

public class AutoArimaResponse {
    private List<Double> forecasts;
    private List<PredictionIntervalDto> intervals;
    private OrderSearchResponse bestOrder;
    private boolean seasonal;

    public List<Double> getForecasts() { return forecasts; }
    public void setForecasts(List<Double> forecasts) { this.forecasts = forecasts; }
    public List<PredictionIntervalDto> getIntervals() { return intervals; }
    public void setIntervals(List<PredictionIntervalDto> intervals) { this.intervals = intervals; }
    public OrderSearchResponse getBestOrder() { return bestOrder; }
    public void setBestOrder(OrderSearchResponse bestOrder) { this.bestOrder = bestOrder; }
    public boolean isSeasonal() { return seasonal; }
    public void setSeasonal(boolean seasonal) { this.seasonal = seasonal; }
}
