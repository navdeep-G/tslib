package tslib.selection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import tslib.evaluation.ForecastFunction;
import tslib.evaluation.ForecastMetrics;
import tslib.evaluation.RollingOriginBacktest;
import tslib.model.expsmoothing.DoubleExpSmoothing;
import tslib.model.expsmoothing.ExponentialSmoothing;
import tslib.model.expsmoothing.SingleExpSmoothing;
import tslib.model.expsmoothing.TripleExpSmoothing;

/**
 * Simple grid-search selector for exponential smoothing models.
 */
public class AutoETS {

    public enum ModelType {
        SINGLE,
        DOUBLE,
        TRIPLE
    }

    private static final double[] GRID = {0.2, 0.4, 0.6, 0.8};

    private final Integer seasonalPeriod;
    private ModelType bestType;
    private double[] bestParameters;
    private double bestScore = Double.POSITIVE_INFINITY;
    private ExponentialSmoothing bestModel;
    private List<Double> trainingData;

    public AutoETS() {
        this(null);
    }

    public AutoETS(Integer seasonalPeriod) {
        this.seasonalPeriod = seasonalPeriod;
    }

    public AutoETS fit(List<Double> data) {
        if (data == null || data.size() < 4) {
            throw new IllegalArgumentException("Data must contain at least 4 points for AutoETS");
        }
        this.trainingData = new ArrayList<>(data);
        RollingOriginBacktest backtest = new RollingOriginBacktest(Math.max(3, data.size() / 2), 1);

        for (double alpha : GRID) {
            ExponentialSmoothing candidate = new SingleExpSmoothing(alpha);
            double score = backtest.run(data, candidate::forecast).getRmse();
            consider(ModelType.SINGLE, new double[] {alpha}, candidate, score);
        }

        for (double alpha : GRID) {
            for (double gamma : GRID) {
                for (int init = 0; init <= 2; init++) {
                    ExponentialSmoothing candidate = new DoubleExpSmoothing(alpha, gamma, init);
                    double score = backtest.run(data, candidate::forecast).getRmse();
                    consider(ModelType.DOUBLE, new double[] {alpha, gamma, init}, candidate, score);
                }
            }
        }

        if (seasonalPeriod != null && seasonalPeriod > 1 && data.size() >= seasonalPeriod * 2) {
            for (double alpha : GRID) {
                for (double beta : GRID) {
                    for (double gamma : GRID) {
                        ExponentialSmoothing candidate = new TripleExpSmoothing(alpha, beta, gamma, seasonalPeriod, false);
                        ForecastFunction function = candidate::forecast;
                        double score = backtest.run(data, function).getRmse();
                        consider(ModelType.TRIPLE, new double[] {alpha, beta, gamma, seasonalPeriod}, candidate, score);
                    }
                }
            }
        }

        return this;
    }

    public List<Double> forecast(int steps) {
        requireFit();
        return bestModel.forecast(trainingData, steps).subList(trainingData.size(), trainingData.size() + steps);
    }

    public ModelType getBestType() {
        requireFit();
        return bestType;
    }

    public double[] getBestParameters() {
        requireFit();
        return Arrays.copyOf(bestParameters, bestParameters.length);
    }

    public double getBestScore() {
        requireFit();
        return bestScore;
    }

    private void consider(ModelType type, double[] parameters, ExponentialSmoothing candidate, double score) {
        if (score < bestScore) {
            bestScore = score;
            bestType = type;
            bestParameters = Arrays.copyOf(parameters, parameters.length);
            bestModel = candidate;
        }
    }

    private void requireFit() {
        if (bestModel == null) {
            throw new IllegalStateException("AutoETS must be fitted before calling this method");
        }
    }
}
