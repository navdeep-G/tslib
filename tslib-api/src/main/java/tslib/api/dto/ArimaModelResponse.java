package tslib.api.dto;

import java.util.List;

public class ArimaModelResponse {
    private List<Double> forecasts;
    private List<PredictionIntervalDto> intervals;
    private double[] arCoefficients;
    private double[] maCoefficients;
    private double intercept;
    private double innovationVariance;
    private int p, d, q;

    public List<Double> getForecasts() { return forecasts; }
    public void setForecasts(List<Double> forecasts) { this.forecasts = forecasts; }
    public List<PredictionIntervalDto> getIntervals() { return intervals; }
    public void setIntervals(List<PredictionIntervalDto> intervals) { this.intervals = intervals; }
    public double[] getArCoefficients() { return arCoefficients; }
    public void setArCoefficients(double[] arCoefficients) { this.arCoefficients = arCoefficients; }
    public double[] getMaCoefficients() { return maCoefficients; }
    public void setMaCoefficients(double[] maCoefficients) { this.maCoefficients = maCoefficients; }
    public double getIntercept() { return intercept; }
    public void setIntercept(double intercept) { this.intercept = intercept; }
    public double getInnovationVariance() { return innovationVariance; }
    public void setInnovationVariance(double innovationVariance) { this.innovationVariance = innovationVariance; }
    public int getP() { return p; }
    public void setP(int p) { this.p = p; }
    public int getD() { return d; }
    public void setD(int d) { this.d = d; }
    public int getQ() { return q; }
    public void setQ(int q) { this.q = q; }
}
