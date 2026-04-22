package tslib.api.dto;

import tslib.model.arima.ArimaOrderSearch;

public class OrderSearchResponse {
    private String modelType;
    private int p, d, q;
    private int seasonalP, seasonalD, seasonalQ, seasonalPeriod;
    private String criterion;
    private double score;

    public static OrderSearchResponse from(ArimaOrderSearch.OrderScore os) {
        var r = new OrderSearchResponse();
        r.modelType = os.getModelType();
        r.p = os.getP(); r.d = os.getD(); r.q = os.getQ();
        r.seasonalP = os.getSeasonalP(); r.seasonalD = os.getSeasonalD();
        r.seasonalQ = os.getSeasonalQ(); r.seasonalPeriod = os.getSeasonalPeriod();
        r.criterion = os.getCriterion().name();
        r.score = os.getScore();
        return r;
    }

    public String getModelType() { return modelType; }
    public int getP() { return p; } public int getD() { return d; } public int getQ() { return q; }
    public int getSeasonalP() { return seasonalP; }
    public int getSeasonalD() { return seasonalD; }
    public int getSeasonalQ() { return seasonalQ; }
    public int getSeasonalPeriod() { return seasonalPeriod; }
    public String getCriterion() { return criterion; }
    public double getScore() { return score; }
}
