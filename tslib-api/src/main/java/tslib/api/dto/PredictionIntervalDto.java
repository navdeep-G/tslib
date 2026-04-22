package tslib.api.dto;

import tslib.evaluation.PredictionInterval;

public class PredictionIntervalDto {
    private int step;
    private double pointForecast;
    private double lower;
    private double upper;
    private double confidenceLevel;

    public PredictionIntervalDto() {}

    public static PredictionIntervalDto from(PredictionInterval pi) {
        var dto = new PredictionIntervalDto();
        dto.step = pi.getStep();
        dto.pointForecast = pi.getPointForecast();
        dto.lower = pi.getLower();
        dto.upper = pi.getUpper();
        dto.confidenceLevel = pi.getConfidenceLevel();
        return dto;
    }

    public int getStep() { return step; }
    public double getPointForecast() { return pointForecast; }
    public double getLower() { return lower; }
    public double getUpper() { return upper; }
    public double getConfidenceLevel() { return confidenceLevel; }
}
