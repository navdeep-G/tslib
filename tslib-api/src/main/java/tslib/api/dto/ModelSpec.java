package tslib.api.dto;

import jakarta.validation.constraints.NotBlank;

public class ModelSpec {
    @NotBlank(message = "type must not be blank")
    private String type;

    // ARIMA/SARIMA/ARIMAX params
    private Integer p;
    private Integer d;
    private Integer q;
    private Integer seasonalP;
    private Integer seasonalD;
    private Integer seasonalQ;
    private Integer seasonalPeriod;

    // ETS params
    private Double alpha;
    private Double beta;
    private Double gamma;
    private Integer initializationMethod;
    private Integer period;

    // Auto-selection params
    private Integer maxP;
    private Integer maxD;
    private Integer maxQ;
    private String criterion = "AIC";

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Integer getP() { return p; }
    public void setP(Integer p) { this.p = p; }
    public Integer getD() { return d; }
    public void setD(Integer d) { this.d = d; }
    public Integer getQ() { return q; }
    public void setQ(Integer q) { this.q = q; }
    public Integer getSeasonalP() { return seasonalP; }
    public void setSeasonalP(Integer seasonalP) { this.seasonalP = seasonalP; }
    public Integer getSeasonalD() { return seasonalD; }
    public void setSeasonalD(Integer seasonalD) { this.seasonalD = seasonalD; }
    public Integer getSeasonalQ() { return seasonalQ; }
    public void setSeasonalQ(Integer seasonalQ) { this.seasonalQ = seasonalQ; }
    public Integer getSeasonalPeriod() { return seasonalPeriod; }
    public void setSeasonalPeriod(Integer seasonalPeriod) { this.seasonalPeriod = seasonalPeriod; }
    public Double getAlpha() { return alpha; }
    public void setAlpha(Double alpha) { this.alpha = alpha; }
    public Double getBeta() { return beta; }
    public void setBeta(Double beta) { this.beta = beta; }
    public Double getGamma() { return gamma; }
    public void setGamma(Double gamma) { this.gamma = gamma; }
    public Integer getInitializationMethod() { return initializationMethod; }
    public void setInitializationMethod(Integer initializationMethod) { this.initializationMethod = initializationMethod; }
    public Integer getPeriod() { return period; }
    public void setPeriod(Integer period) { this.period = period; }
    public Integer getMaxP() { return maxP; }
    public void setMaxP(Integer maxP) { this.maxP = maxP; }
    public Integer getMaxD() { return maxD; }
    public void setMaxD(Integer maxD) { this.maxD = maxD; }
    public Integer getMaxQ() { return maxQ; }
    public void setMaxQ(Integer maxQ) { this.maxQ = maxQ; }
    public String getCriterion() { return criterion; }
    public void setCriterion(String criterion) { this.criterion = criterion; }
}
