package tslib.selection;

import java.util.List;
import tslib.model.arima.ARIMA;
import tslib.model.arima.ArimaOrderSearch;
import tslib.model.arima.SARIMA;

/**
 * Small convenience wrapper around the manual order-search helpers.
 */
public class AutoArima {

    private final int maxP;
    private final int maxD;
    private final int maxQ;
    private final int maxSeasonalP;
    private final int maxSeasonalD;
    private final int maxSeasonalQ;
    private final int seasonalPeriod;
    private final ArimaOrderSearch.Criterion criterion;

    private ARIMA arima;
    private SARIMA sarima;
    private ArimaOrderSearch.OrderScore bestOrder;

    public AutoArima(int maxP, int maxD, int maxQ, ArimaOrderSearch.Criterion criterion) {
        this(maxP, maxD, maxQ, 0, 0, 0, 0, criterion);
    }

    public AutoArima(
            int maxP,
            int maxD,
            int maxQ,
            int maxSeasonalP,
            int maxSeasonalD,
            int maxSeasonalQ,
            int seasonalPeriod,
            ArimaOrderSearch.Criterion criterion) {
        this.maxP = maxP;
        this.maxD = maxD;
        this.maxQ = maxQ;
        this.maxSeasonalP = maxSeasonalP;
        this.maxSeasonalD = maxSeasonalD;
        this.maxSeasonalQ = maxSeasonalQ;
        this.seasonalPeriod = seasonalPeriod;
        this.criterion = criterion;
    }

    public AutoArima fit(List<Double> data) {
        if (seasonalPeriod > 1 && (maxSeasonalP > 0 || maxSeasonalD > 0 || maxSeasonalQ > 0)) {
            bestOrder = ArimaOrderSearch.searchBestSarima(
                    data,
                    maxP,
                    maxD,
                    maxQ,
                    maxSeasonalP,
                    maxSeasonalD,
                    maxSeasonalQ,
                    seasonalPeriod,
                    criterion);
            sarima = new SARIMA(
                    bestOrder.getP(),
                    bestOrder.getD(),
                    bestOrder.getQ(),
                    bestOrder.getSeasonalP(),
                    bestOrder.getSeasonalD(),
                    bestOrder.getSeasonalQ(),
                    bestOrder.getSeasonalPeriod()).fit(data);
            arima = null;
        } else {
            bestOrder = ArimaOrderSearch.searchBestArima(data, maxP, maxD, maxQ, criterion);
            arima = new ARIMA(bestOrder.getP(), bestOrder.getD(), bestOrder.getQ()).fit(data);
            sarima = null;
        }
        return this;
    }

    public List<Double> forecast(int steps) {
        requireFit();
        return sarima != null ? sarima.forecast(steps) : arima.forecast(steps);
    }

    public ArimaOrderSearch.OrderScore getBestOrder() {
        requireFit();
        return bestOrder;
    }

    public boolean isSeasonalModel() {
        requireFit();
        return sarima != null;
    }

    public ARIMA getArimaModel() {
        return arima;
    }

    public SARIMA getSarimaModel() {
        return sarima;
    }

    private void requireFit() {
        if (bestOrder == null) {
            throw new IllegalStateException("AutoArima must be fitted before calling this method");
        }
    }
}
