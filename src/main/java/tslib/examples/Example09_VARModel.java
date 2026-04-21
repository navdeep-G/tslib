package tslib.examples;

import tslib.model.arima.VARModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Example 09: Vector Autoregression (VAR) for multivariate time series.
 *
 * A VAR(p) model jointly fits K endogenous series. Each series' next value
 * depends on the previous p values of *all* K series.
 *
 * Demonstrates manual lag selection, optimal lag selection by AIC, and
 * multi-step forecasting.
 */
public class Example09_VARModel {

    public static void run() {
        System.out.println("=== Example 09: VAR Model (Multivariate Forecasting) ===\n");

        // Generate two correlated series: GDP and Consumption
        int n = 100;
        List<List<Double>> bivariate = generateBivariateSeries(n, 0.6, 0.3, 0.5, 0.4, 1.0, 99);

        List<Double> gdp         = bivariate.get(0);
        List<Double> consumption = bivariate.get(1);
        System.out.printf("Series 1 (GDP)         tail [n-5:] : %s%n", fmt(gdp.subList(95, 100)));
        System.out.printf("Series 2 (Consumption) tail [n-5:] : %s%n", fmt(consumption.subList(95, 100)));

        // --- 1. Manual VAR(2) ---
        System.out.println("\n-- VAR(2) manual lag order --");
        VARModel var2 = new VARModel(2).fit(bivariate);

        List<List<Double>> forecast5 = var2.forecast(5);
        System.out.println("GDP forecast         (h=5): " + fmt(forecast5.get(0)));
        System.out.println("Consumption forecast (h=5): " + fmt(forecast5.get(1)));

        // Coefficient matrix: coefficients[k] = intercept + lag1_series0 + lag1_series1 + ...
        double[][] coefs = var2.getCoefficients();
        System.out.printf("%nCoefficient row 0 length: %d  (1 intercept + 2 lags × 2 series)%n",
                coefs[0].length);
        System.out.printf("Eq0 intercept=%.4f  φ₁₁=%.4f  φ₁₂=%.4f%n",
                coefs[0][0], coefs[0][1], coefs[0][2]);
        System.out.printf("Eq1 intercept=%.4f  φ₂₁=%.4f  φ₂₂=%.4f%n",
                coefs[1][0], coefs[1][1], coefs[1][2]);

        // Model quality — AIC of the fitted VAR(2)
        System.out.printf("%nVAR(2) AIC: %.4f%n", var2.getAic());

        // --- 2. VAR with 3 series ---
        System.out.println("\n-- VAR(1) with 3 series --");
        List<List<Double>> trivariate = generateTrivariateSeries(80, 7);
        VARModel var1tri = new VARModel(1).fit(trivariate);
        List<List<Double>> tri3 = var1tri.forecast(3);
        System.out.println("Series 0 forecast (h=3): " + fmt(tri3.get(0)));
        System.out.println("Series 1 forecast (h=3): " + fmt(tri3.get(1)));
        System.out.println("Series 2 forecast (h=3): " + fmt(tri3.get(2)));

        // --- 3. Optimal lag selection by AIC ---
        System.out.println("\n-- fitOptimal: AIC-based lag selection (maxLag=4) --");
        VARModel varOpt = VARModel.fitOptimal(bivariate, 4);
        List<List<Double>> optForecast = varOpt.forecast(3);
        System.out.println("Optimal model GDP forecast (h=3): " + fmt(optForecast.get(0)));

        System.out.println();
    }

    // Two correlated AR(1) series
    static List<List<Double>> generateBivariateSeries(int n,
                                                        double phi11, double phi12,
                                                        double phi21, double phi22,
                                                        double sigma, long seed) {
        List<Double> y1 = new ArrayList<>(n);
        List<Double> y2 = new ArrayList<>(n);
        Random rng = new Random(seed);
        double v1 = 0.0, v2 = 0.0;
        for (int i = 0; i < n; i++) {
            double nv1 = phi11 * v1 + phi12 * v2 + sigma * rng.nextGaussian();
            double nv2 = phi21 * v1 + phi22 * v2 + sigma * rng.nextGaussian();
            v1 = nv1; v2 = nv2;
            y1.add(v1); y2.add(v2);
        }
        return Arrays.asList(y1, y2);
    }

    static List<List<Double>> generateTrivariateSeries(int n, long seed) {
        List<Double> s0 = new ArrayList<>(n), s1 = new ArrayList<>(n), s2 = new ArrayList<>(n);
        Random rng = new Random(seed);
        double a = 0, b = 0, c = 0;
        for (int i = 0; i < n; i++) {
            double na = 0.5 * a - 0.2 * b + rng.nextGaussian();
            double nb = 0.3 * a + 0.4 * b - 0.1 * c + rng.nextGaussian();
            double nc = -0.2 * b + 0.6 * c + rng.nextGaussian();
            a = na; b = nb; c = nc;
            s0.add(a); s1.add(b); s2.add(c);
        }
        return Arrays.asList(s0, s1, s2);
    }

    static List<Double> fmt(List<Double> list) {
        List<Double> out = new ArrayList<>(list.size());
        for (Double v : list) out.add(v == null ? null : Math.round(v * 1000.0) / 1000.0);
        return out;
    }
}
