package tslib.api.dto;

import java.util.List;

public class KalmanFilterResponse {
    private List<Double> predictedStates;
    private List<Double> filteredStates;
    private List<Double> filteredCovariances;
    private List<Double> innovations;
    private double logLikelihood;
    private List<Double> forecasts;
    private List<Double> forecastVariances;

    public List<Double> getPredictedStates() { return predictedStates; }
    public void setPredictedStates(List<Double> v) { this.predictedStates = v; }
    public List<Double> getFilteredStates() { return filteredStates; }
    public void setFilteredStates(List<Double> v) { this.filteredStates = v; }
    public List<Double> getFilteredCovariances() { return filteredCovariances; }
    public void setFilteredCovariances(List<Double> v) { this.filteredCovariances = v; }
    public List<Double> getInnovations() { return innovations; }
    public void setInnovations(List<Double> v) { this.innovations = v; }
    public double getLogLikelihood() { return logLikelihood; }
    public void setLogLikelihood(double logLikelihood) { this.logLikelihood = logLikelihood; }
    public List<Double> getForecasts() { return forecasts; }
    public void setForecasts(List<Double> forecasts) { this.forecasts = forecasts; }
    public List<Double> getForecastVariances() { return forecastVariances; }
    public void setForecastVariances(List<Double> v) { this.forecastVariances = v; }
}
