package tslib.transform;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.optimization.GoalType;
import org.apache.commons.math3.optimization.univariate.BrentOptimizer;
import org.apache.commons.math3.optimization.univariate.UnivariateOptimizer;

/**
 * Time series transformation utilities including log, root, and Box-Cox transformations.
 *
 * Author: navdeepgill
 */
public class Transform {

    // --- Basic Transformations ---

    public static List<Double> log(List<Double> data) {
        validatePositive(data);
        List<Double> result = new ArrayList<>(data.size());
        for (double val : data) result.add(Math.log(val));
        return result;
    }

    public static List<Double> sqrt(List<Double> data) {
        validatePositive(data);
        List<Double> result = new ArrayList<>(data.size());
        for (double val : data) result.add(Math.sqrt(val));
        return result;
    }

    public static List<Double> cbrt(List<Double> data) {
        List<Double> result = new ArrayList<>(data.size());
        for (double val : data) result.add(Math.cbrt(val));
        return result;
    }

    public static List<Double> root(List<Double> data, double r) {
        List<Double> result = new ArrayList<>(data.size());
        for (double val : data) result.add(Math.pow(val, 1.0 / r));
        return result;
    }

    // --- Box-Cox Transformation ---

    /**
     * Box-Cox transform with specified lambda.
     */
    public static List<Double> boxCox(List<Double> data, double lambda) {
        validatePositive(data);
        List<Double> result = new ArrayList<>(data.size());
        for (double x : data) {
            result.add(lambda == 0 ? Math.log(x) : (Math.pow(x, lambda) - 1.0) / lambda);
        }
        return result;
    }

    /**
     * Box-Cox transform with optimal lambda (search in [-1, 2]).
     */
    public static List<Double> boxCox(List<Double> data) {
        return boxCox(data, boxCoxLambdaSearch(data));
    }

    public static double boxCoxLambdaSearch(List<Double> data) {
        return boxCoxLambdaSearch(data, -1, 2);
    }

    public static double boxCoxLambdaSearch(final List<Double> data, double lower, double upper) {
        validatePositive(data);

        UnivariateOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14);
        return optimizer.optimize(
                100,
                new UnivariateFunction() {
                    @Override
                    public double value(double lambda) {
                        return boxCoxNegLogLikelihood(data, lambda);
                    }
                },
                GoalType.MINIMIZE,
                lower,
                upper
        ).getPoint();
    }

    // --- Internal Helpers ---

    private static double boxCoxNegLogLikelihood(List<Double> data, double lambda) {
        int n = data.size();
        double mean = 0.0;
        double m2 = 0.0;

        for (int i = 0; i < n; i++) {
            double x = data.get(i);
            if (x <= 0) return Double.POSITIVE_INFINITY;
            double t = (lambda == 0) ? Math.log(x) : (Math.pow(x, lambda) - 1.0) / lambda;
            double delta = t - mean;
            mean += delta / (i + 1);
            m2 += delta * (t - mean);
        }

        double variance = m2 / n;
        return Math.log(Math.max(variance, 1e-12));
    }

    private static void validatePositive(List<Double> data) {
        for (double x : data) {
            if (x <= 0) {
                throw new IllegalArgumentException("All values must be > 0 for this transformation.");
            }
        }
    }
}
