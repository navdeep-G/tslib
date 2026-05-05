package tslib.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LjungBoxResponse {
    private double statistic;
    private double pValue;
    private int lags;
    private boolean rejectsAtFivePercent;

    public double getStatistic() { return statistic; }
    public void setStatistic(double statistic) { this.statistic = statistic; }
    @JsonProperty("pValue")
    public double getPValue() { return pValue; }
    public void setPValue(double pValue) { this.pValue = pValue; }
    public int getLags() { return lags; }
    public void setLags(int lags) { this.lags = lags; }
    public boolean isRejectsAtFivePercent() { return rejectsAtFivePercent; }
    public void setRejectsAtFivePercent(boolean rejectsAtFivePercent) { this.rejectsAtFivePercent = rejectsAtFivePercent; }
}
