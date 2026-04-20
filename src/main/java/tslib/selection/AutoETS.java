package tslib.selection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.univariate.BrentOptimizer;
import org.apache.commons.math3.optim.univariate.SearchInterval;
import org.apache.commons.math3.optim.univariate.UnivariateObjectiveFunction;
import org.apache.commons.math3.optim.univariate.UnivariatePointValuePair;
import tslib.evaluation.RollingOriginBacktest;
import tslib.model.expsmoothing.DoubleExpSmoothing;
import tslib.model.expsmoothing.ExponentialSmoothing;
import tslib.model.expsmoothing.SingleExpSmoothing;
import tslib.model.expsmoothing.TripleExpSmoothing;

/**
 * Optimizer-based selector for exponential smoothing models.
 * Uses Brent's method with coordinate descent instead of a coarse grid search,
 * giving precise parameter estimates over the continuous [0.01, 0.99] domain.
 */
public class AutoETS {

    public enum ModelType {
        SINGLE,
        DOUBLE,
        TRIPLE
    }

    private static final int MAX_EVAL = 200;
    private static final double PARAM_LO = 0.01;
    private static final double PARAM_HI = 0.99;
    private static final int COORD_ITERS = 4;

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
        BrentOptimizer brent = new BrentOptimizer(1e-4, 1e-6);

        // SES: single Brent pass over alpha in [PARAM_LO, PARAM_HI]
        UnivariatePointValuePair sesResult = brent.optimize(
                new MaxEval(MAX_EVAL),
                GoalType.MINIMIZE,
                new SearchInterval(PARAM_LO, PARAM_HI, 0.3),
                new UnivariateObjectiveFunction(a -> {
                    ExponentialSmoothing c = new SingleExpSmoothing(a);
                    return backtest.run(data, c::forecast).getRmse();
                }));
        consider(ModelType.SINGLE,
                new double[]{sesResult.getPoint()},
                new SingleExpSmoothing(sesResult.getPoint()),
                sesResult.getValue());

        // DES: coordinate descent over alpha and gamma, repeated for each init style
        for (int init = 0; init <= 2; init++) {
            final int fixedInit = init;
            double alpha = 0.3, gamma = 0.3;
            for (int iter = 0; iter < COORD_ITERS; iter++) {
                final double g = gamma;
                UnivariatePointValuePair ra = brent.optimize(
                        new MaxEval(MAX_EVAL),
                        GoalType.MINIMIZE,
                        new SearchInterval(PARAM_LO, PARAM_HI, alpha),
                        new UnivariateObjectiveFunction(a -> {
                            ExponentialSmoothing c = new DoubleExpSmoothing(a, g, fixedInit);
                            return backtest.run(data, c::forecast).getRmse();
                        }));
                alpha = ra.getPoint();
                final double a = alpha;
                UnivariatePointValuePair rg = brent.optimize(
                        new MaxEval(MAX_EVAL),
                        GoalType.MINIMIZE,
                        new SearchInterval(PARAM_LO, PARAM_HI, gamma),
                        new UnivariateObjectiveFunction(gv -> {
                            ExponentialSmoothing c = new DoubleExpSmoothing(a, gv, fixedInit);
                            return backtest.run(data, c::forecast).getRmse();
                        }));
                gamma = rg.getPoint();
            }
            ExponentialSmoothing desBest = new DoubleExpSmoothing(alpha, gamma, fixedInit);
            consider(ModelType.DOUBLE,
                    new double[]{alpha, gamma, fixedInit},
                    desBest,
                    backtest.run(data, desBest::forecast).getRmse());
        }

        // TES: coordinate descent over alpha, beta, gamma (only if seasonal data is available)
        if (seasonalPeriod != null && seasonalPeriod > 1 && data.size() >= seasonalPeriod * 2) {
            double alpha = 0.3, beta = 0.1, gamma = 0.3;
            for (int iter = 0; iter < COORD_ITERS; iter++) {
                final double b0 = beta, g0 = gamma;
                UnivariatePointValuePair ra = brent.optimize(
                        new MaxEval(MAX_EVAL),
                        GoalType.MINIMIZE,
                        new SearchInterval(PARAM_LO, PARAM_HI, alpha),
                        new UnivariateObjectiveFunction(a -> {
                            ExponentialSmoothing c = new TripleExpSmoothing(a, b0, g0, seasonalPeriod, false);
                            return backtest.run(data, c::forecast).getRmse();
                        }));
                alpha = ra.getPoint();
                final double a1 = alpha, g1 = gamma;
                UnivariatePointValuePair rb = brent.optimize(
                        new MaxEval(MAX_EVAL),
                        GoalType.MINIMIZE,
                        new SearchInterval(PARAM_LO, PARAM_HI, beta),
                        new UnivariateObjectiveFunction(bv -> {
                            ExponentialSmoothing c = new TripleExpSmoothing(a1, bv, g1, seasonalPeriod, false);
                            return backtest.run(data, c::forecast).getRmse();
                        }));
                beta = rb.getPoint();
                final double a2 = alpha, b2 = beta;
                UnivariatePointValuePair rg = brent.optimize(
                        new MaxEval(MAX_EVAL),
                        GoalType.MINIMIZE,
                        new SearchInterval(PARAM_LO, PARAM_HI, gamma),
                        new UnivariateObjectiveFunction(gv -> {
                            ExponentialSmoothing c = new TripleExpSmoothing(a2, b2, gv, seasonalPeriod, false);
                            return backtest.run(data, c::forecast).getRmse();
                        }));
                gamma = rg.getPoint();
            }
            ExponentialSmoothing tesBest = new TripleExpSmoothing(alpha, beta, gamma, seasonalPeriod, false);
            consider(ModelType.TRIPLE,
                    new double[]{alpha, beta, gamma, seasonalPeriod},
                    tesBest,
                    backtest.run(data, tesBest::forecast).getRmse());
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
