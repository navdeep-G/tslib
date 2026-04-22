package tslib.api.dto;

import java.util.List;

public class ArimaXForecastResponse {
    private List<Double> forecasts;
    private double[] arCoefficients;
    private double[] maCoefficients;
    private double[] exogenousCoefficients;
    private double innovationVariance;

    public List<Double> getForecasts() { return forecasts; }
    public void setForecasts(List<Double> forecasts) { this.forecasts = forecasts; }
    public double[] getArCoefficients() { return arCoefficients; }
    public void setArCoefficients(double[] arCoefficients) { this.arCoefficients = arCoefficients; }
    public double[] getMaCoefficients() { return maCoefficients; }
    public void setMaCoefficients(double[] maCoefficients) { this.maCoefficients = maCoefficients; }
    public double[] getExogenousCoefficients() { return exogenousCoefficients; }
    public void setExogenousCoefficients(double[] v) { this.exogenousCoefficients = v; }
    public double getInnovationVariance() { return innovationVariance; }
    public void setInnovationVariance(double innovationVariance) { this.innovationVariance = innovationVariance; }
}
