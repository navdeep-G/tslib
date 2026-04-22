package tslib.api.dto;

import java.util.List;

public class AutoEtsResponse {
    private List<Double> forecasts;
    private List<PredictionIntervalDto> intervals;
    private String bestType;
    private double[] bestParameters;
    private double bestScore;

    public List<Double> getForecasts() { return forecasts; }
    public void setForecasts(List<Double> forecasts) { this.forecasts = forecasts; }
    public List<PredictionIntervalDto> getIntervals() { return intervals; }
    public void setIntervals(List<PredictionIntervalDto> intervals) { this.intervals = intervals; }
    public String getBestType() { return bestType; }
    public void setBestType(String bestType) { this.bestType = bestType; }
    public double[] getBestParameters() { return bestParameters; }
    public void setBestParameters(double[] bestParameters) { this.bestParameters = bestParameters; }
    public double getBestScore() { return bestScore; }
    public void setBestScore(double bestScore) { this.bestScore = bestScore; }
}
