package tslib.api.dto;

import java.util.List;

public class SarimaModelResponse {
    private List<Double> forecasts;
    private List<PredictionIntervalDto> intervals;
    private double[] arCoefficients;
    private double[] maCoefficients;
    private double[] seasonalArCoefficients;
    private double[] seasonalMaCoefficients;
    private double intercept;
    private double innovationVariance;
    private int p, d, q;
    private int seasonalP, seasonalD, seasonalQ, seasonalPeriod;

    public List<Double> getForecasts() { return forecasts; }
    public void setForecasts(List<Double> forecasts) { this.forecasts = forecasts; }
    public List<PredictionIntervalDto> getIntervals() { return intervals; }
    public void setIntervals(List<PredictionIntervalDto> intervals) { this.intervals = intervals; }
    public double[] getArCoefficients() { return arCoefficients; }
    public void setArCoefficients(double[] arCoefficients) { this.arCoefficients = arCoefficients; }
    public double[] getMaCoefficients() { return maCoefficients; }
    public void setMaCoefficients(double[] maCoefficients) { this.maCoefficients = maCoefficients; }
    public double[] getSeasonalArCoefficients() { return seasonalArCoefficients; }
    public void setSeasonalArCoefficients(double[] v) { this.seasonalArCoefficients = v; }
    public double[] getSeasonalMaCoefficients() { return seasonalMaCoefficients; }
    public void setSeasonalMaCoefficients(double[] v) { this.seasonalMaCoefficients = v; }
    public double getIntercept() { return intercept; }
    public void setIntercept(double intercept) { this.intercept = intercept; }
    public double getInnovationVariance() { return innovationVariance; }
    public void setInnovationVariance(double v) { this.innovationVariance = v; }
    public int getP() { return p; } public void setP(int p) { this.p = p; }
    public int getD() { return d; } public void setD(int d) { this.d = d; }
    public int getQ() { return q; } public void setQ(int q) { this.q = q; }
    public int getSeasonalP() { return seasonalP; } public void setSeasonalP(int v) { this.seasonalP = v; }
    public int getSeasonalD() { return seasonalD; } public void setSeasonalD(int v) { this.seasonalD = v; }
    public int getSeasonalQ() { return seasonalQ; } public void setSeasonalQ(int v) { this.seasonalQ = v; }
    public int getSeasonalPeriod() { return seasonalPeriod; } public void setSeasonalPeriod(int v) { this.seasonalPeriod = v; }
}
