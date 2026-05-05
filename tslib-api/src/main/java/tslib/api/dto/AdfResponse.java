package tslib.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AdfResponse {
    private double statistic;
    private double pValue;
    private int lag;
    private boolean stationary;
    private boolean needsDiff;

    public double getStatistic() { return statistic; }
    public void setStatistic(double statistic) { this.statistic = statistic; }
    @JsonProperty("pValue")
    public double getPValue() { return pValue; }
    public void setPValue(double pValue) { this.pValue = pValue; }
    public int getLag() { return lag; }
    public void setLag(int lag) { this.lag = lag; }
    public boolean isStationary() { return stationary; }
    public void setStationary(boolean stationary) { this.stationary = stationary; }
    public boolean isNeedsDiff() { return needsDiff; }
    public void setNeedsDiff(boolean needsDiff) { this.needsDiff = needsDiff; }
}
