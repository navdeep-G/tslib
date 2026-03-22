package tslib.model.arima;

import java.util.List;

/**
 * Grid-search helpers for manual ARIMA/SARIMA order selection.
 */
public final class ArimaOrderSearch {

    private ArimaOrderSearch() {}

    public enum Criterion {
        AIC,
        BIC,
        AICC
    }

    public static OrderScore searchBestArima(List<Double> data, int maxP, int maxD, int maxQ, Criterion criterion) {
        validateBounds(maxP, maxD, maxQ);
        OrderScore best = null;
        for (int p = 0; p <= maxP; p++) {
            for (int d = 0; d <= maxD; d++) {
                for (int q = 0; q <= maxQ; q++) {
                    if (p == 0 && d == 0 && q == 0) {
                        continue;
                    }
                    try {
                        ARIMA model = new ARIMA(p, d, q).fit(data);
                        OrderScore score = OrderScore.forArima(model, criterion);
                        if (best == null || score.getScore() < best.getScore()) {
                            best = score;
                        }
                    } catch (RuntimeException ignored) {
                        // Skip unstable or invalid combinations.
                    }
                }
            }
        }
        if (best == null) {
            throw new IllegalArgumentException("No valid ARIMA model could be fitted in the requested grid");
        }
        return best;
    }

    public static OrderScore searchBestSarima(
            List<Double> data,
            int maxP,
            int maxD,
            int maxQ,
            int maxSeasonalP,
            int maxSeasonalD,
            int maxSeasonalQ,
            int seasonalPeriod,
            Criterion criterion) {
        validateBounds(maxP, maxD, maxQ);
        validateBounds(maxSeasonalP, maxSeasonalD, maxSeasonalQ);
        if (seasonalPeriod < 1) {
            throw new IllegalArgumentException("Seasonal period must be >= 1");
        }

        OrderScore best = null;
        for (int p = 0; p <= maxP; p++) {
            for (int d = 0; d <= maxD; d++) {
                for (int q = 0; q <= maxQ; q++) {
                    for (int P = 0; P <= maxSeasonalP; P++) {
                        for (int D = 0; D <= maxSeasonalD; D++) {
                            for (int Q = 0; Q <= maxSeasonalQ; Q++) {
                                if (p == 0 && d == 0 && q == 0 && P == 0 && D == 0 && Q == 0) {
                                    continue;
                                }
                                try {
                                    SARIMA model = new SARIMA(p, d, q, P, D, Q, seasonalPeriod).fit(data);
                                    OrderScore score = OrderScore.forSarima(model, criterion);
                                    if (best == null || score.getScore() < best.getScore()) {
                                        best = score;
                                    }
                                } catch (RuntimeException ignored) {
                                    // Skip unstable or invalid combinations.
                                }
                            }
                        }
                    }
                }
            }
        }
        if (best == null) {
            throw new IllegalArgumentException("No valid SARIMA model could be fitted in the requested grid");
        }
        return best;
    }

    private static void validateBounds(int maxP, int maxD, int maxQ) {
        if (maxP < 0 || maxD < 0 || maxQ < 0) {
            throw new IllegalArgumentException("Search bounds must be >= 0");
        }
    }

    public static final class OrderScore {
        private final String modelType;
        private final int p;
        private final int d;
        private final int q;
        private final int seasonalP;
        private final int seasonalD;
        private final int seasonalQ;
        private final int seasonalPeriod;
        private final Criterion criterion;
        private final double score;

        private OrderScore(
                String modelType,
                int p,
                int d,
                int q,
                int seasonalP,
                int seasonalD,
                int seasonalQ,
                int seasonalPeriod,
                Criterion criterion,
                double score) {
            this.modelType = modelType;
            this.p = p;
            this.d = d;
            this.q = q;
            this.seasonalP = seasonalP;
            this.seasonalD = seasonalD;
            this.seasonalQ = seasonalQ;
            this.seasonalPeriod = seasonalPeriod;
            this.criterion = criterion;
            this.score = score;
        }

        public static OrderScore forArima(ARIMA model, Criterion criterion) {
            double score = evaluate(model.getResiduals(), 1 + model.getP() + model.getQ(), criterion);
            return new OrderScore("ARIMA", model.getP(), model.getD(), model.getQ(), 0, 0, 0, 0, criterion, score);
        }

        public static OrderScore forSarima(SARIMA model, Criterion criterion) {
            int parameterCount = 1 + model.getP() + model.getQ() + model.getSeasonalP() + model.getSeasonalQ();
            double score = evaluate(model.getResiduals(), parameterCount, criterion);
            return new OrderScore(
                    "SARIMA",
                    model.getP(),
                    model.getD(),
                    model.getQ(),
                    model.getSeasonalP(),
                    model.getSeasonalD(),
                    model.getSeasonalQ(),
                    model.getSeasonalPeriod(),
                    criterion,
                    score);
        }

        private static double evaluate(List<Double> residuals, int parameterCount, Criterion criterion) {
            double rss = 0.0;
            for (double residual : residuals) {
                rss += residual * residual;
            }
            int n = Math.max(1, residuals.size());
            rss = Math.max(rss, 1e-12);
            switch (criterion) {
                case BIC:
                    return InformationCriteria.bic(rss, n, parameterCount);
                case AICC:
                    return InformationCriteria.aicc(rss, n, parameterCount);
                case AIC:
                default:
                    return InformationCriteria.aic(rss, n, parameterCount);
            }
        }

        public String getModelType() {
            return modelType;
        }

        public int getP() {
            return p;
        }

        public int getD() {
            return d;
        }

        public int getQ() {
            return q;
        }

        public int getSeasonalP() {
            return seasonalP;
        }

        public int getSeasonalD() {
            return seasonalD;
        }

        public int getSeasonalQ() {
            return seasonalQ;
        }

        public int getSeasonalPeriod() {
            return seasonalPeriod;
        }

        public Criterion getCriterion() {
            return criterion;
        }

        public double getScore() {
            return score;
        }
    }
}
