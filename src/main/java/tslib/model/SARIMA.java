package tslib.model;

import java.util.List;

public class SARIMA extends tslib.model.arima.SARIMA {
    public SARIMA(int p, int d, int q, int seasonalP, int seasonalD, int seasonalQ, int seasonalPeriod) {
        super(p, d, q, seasonalP, seasonalD, seasonalQ, seasonalPeriod);
    }

    public SARIMA(
            int p,
            int d,
            int q,
            int seasonalP,
            int seasonalD,
            int seasonalQ,
            int seasonalPeriod,
            int maxIterations,
            double tolerance) {
        super(p, d, q, seasonalP, seasonalD, seasonalQ, seasonalPeriod, maxIterations, tolerance);
    }

    @Override
    public SARIMA fit(List<Double> data) {
        super.fit(data);
        return this;
    }
}
