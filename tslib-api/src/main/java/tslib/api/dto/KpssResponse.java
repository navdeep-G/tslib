package tslib.api.dto;

public class KpssResponse {
    private double statistic;
    private int lags;
    private String regressionType;
    private boolean stationaryAtFivePercent;
    private boolean stationaryAtOnePercent;
    private double criticalValueFivePercent;
    private double criticalValueOnePercent;

    public double getStatistic() { return statistic; }
    public void setStatistic(double statistic) { this.statistic = statistic; }
    public int getLags() { return lags; }
    public void setLags(int lags) { this.lags = lags; }
    public String getRegressionType() { return regressionType; }
    public void setRegressionType(String regressionType) { this.regressionType = regressionType; }
    public boolean isStationaryAtFivePercent() { return stationaryAtFivePercent; }
    public void setStationaryAtFivePercent(boolean v) { this.stationaryAtFivePercent = v; }
    public boolean isStationaryAtOnePercent() { return stationaryAtOnePercent; }
    public void setStationaryAtOnePercent(boolean v) { this.stationaryAtOnePercent = v; }
    public double getCriticalValueFivePercent() { return criticalValueFivePercent; }
    public void setCriticalValueFivePercent(double v) { this.criticalValueFivePercent = v; }
    public double getCriticalValueOnePercent() { return criticalValueOnePercent; }
    public void setCriticalValueOnePercent(double v) { this.criticalValueOnePercent = v; }
}
