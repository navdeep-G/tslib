package tslib.api.dto;

import java.util.List;

public class AnalyzeResponse {
    private double average;
    private double variance;
    private double standardDeviation;
    private double min;
    private double max;
    private int minIndex;
    private int maxIndex;
    private double autocorrelation;
    private double autocovariance;
    private double[] acf;
    private double[] pacf;
    private double adfStatistic;
    private boolean stationary;
    private List<Double> logTransformed;
    private List<Double> firstDifference;
    private List<Double> rollingAverage;

    public double getAverage() { return average; }
    public void setAverage(double average) { this.average = average; }
    public double getVariance() { return variance; }
    public void setVariance(double variance) { this.variance = variance; }
    public double getStandardDeviation() { return standardDeviation; }
    public void setStandardDeviation(double standardDeviation) { this.standardDeviation = standardDeviation; }
    public double getMin() { return min; }
    public void setMin(double min) { this.min = min; }
    public double getMax() { return max; }
    public void setMax(double max) { this.max = max; }
    public int getMinIndex() { return minIndex; }
    public void setMinIndex(int minIndex) { this.minIndex = minIndex; }
    public int getMaxIndex() { return maxIndex; }
    public void setMaxIndex(int maxIndex) { this.maxIndex = maxIndex; }
    public double getAutocorrelation() { return autocorrelation; }
    public void setAutocorrelation(double autocorrelation) { this.autocorrelation = autocorrelation; }
    public double getAutocovariance() { return autocovariance; }
    public void setAutocovariance(double autocovariance) { this.autocovariance = autocovariance; }
    public double[] getAcf() { return acf; }
    public void setAcf(double[] acf) { this.acf = acf; }
    public double[] getPacf() { return pacf; }
    public void setPacf(double[] pacf) { this.pacf = pacf; }
    public double getAdfStatistic() { return adfStatistic; }
    public void setAdfStatistic(double adfStatistic) { this.adfStatistic = adfStatistic; }
    public boolean isStationary() { return stationary; }
    public void setStationary(boolean stationary) { this.stationary = stationary; }
    public List<Double> getLogTransformed() { return logTransformed; }
    public void setLogTransformed(List<Double> logTransformed) { this.logTransformed = logTransformed; }
    public List<Double> getFirstDifference() { return firstDifference; }
    public void setFirstDifference(List<Double> firstDifference) { this.firstDifference = firstDifference; }
    public List<Double> getRollingAverage() { return rollingAverage; }
    public void setRollingAverage(List<Double> rollingAverage) { this.rollingAverage = rollingAverage; }
}
