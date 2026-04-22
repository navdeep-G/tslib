package tslib.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class OrderSearchRequest {
    @NotEmpty(message = "data must not be empty")
    private List<Double> data;

    @Min(1) private int maxP = 3;
    @Min(0) private int maxD = 2;
    @Min(1) private int maxQ = 3;
    private String criterion = "AIC";

    // Optional — if provided, performs seasonal search
    private Integer maxSeasonalP;
    private Integer maxSeasonalD;
    private Integer maxSeasonalQ;
    private Integer seasonalPeriod;

    public List<Double> getData() { return data; }
    public void setData(List<Double> data) { this.data = data; }
    public int getMaxP() { return maxP; }
    public void setMaxP(int maxP) { this.maxP = maxP; }
    public int getMaxD() { return maxD; }
    public void setMaxD(int maxD) { this.maxD = maxD; }
    public int getMaxQ() { return maxQ; }
    public void setMaxQ(int maxQ) { this.maxQ = maxQ; }
    public String getCriterion() { return criterion; }
    public void setCriterion(String criterion) { this.criterion = criterion; }
    public Integer getMaxSeasonalP() { return maxSeasonalP; }
    public void setMaxSeasonalP(Integer maxSeasonalP) { this.maxSeasonalP = maxSeasonalP; }
    public Integer getMaxSeasonalD() { return maxSeasonalD; }
    public void setMaxSeasonalD(Integer maxSeasonalD) { this.maxSeasonalD = maxSeasonalD; }
    public Integer getMaxSeasonalQ() { return maxSeasonalQ; }
    public void setMaxSeasonalQ(Integer maxSeasonalQ) { this.maxSeasonalQ = maxSeasonalQ; }
    public Integer getSeasonalPeriod() { return seasonalPeriod; }
    public void setSeasonalPeriod(Integer seasonalPeriod) { this.seasonalPeriod = seasonalPeriod; }
}
