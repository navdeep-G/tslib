package tslib.examples;

import tslib.model.arima.ArimaOrderSearch;
import tslib.selection.AutoArima;
import tslib.selection.AutoETS;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Example 11: Automated model selection — AutoARIMA and AutoETS.
 *
 * AutoARIMA performs a grid search over ARIMA or SARIMA orders and selects the
 * best by AIC, BIC, or AICc.
 *
 * AutoETS uses Brent's method with coordinate descent to optimise smoothing
 * parameters continuously, then selects the best exponential smoothing variant
 * (SES / DES / TES) by rolling-origin RMSE.
 */
public class Example11_AutoSelection {

    public static void run() {
        System.out.println("=== Example 11: Automated Model Selection ===\n");

        // --- 1. AutoARIMA (non-seasonal) ---
        System.out.println("-- AutoARIMA (non-seasonal, criterion=AIC) --");
        List<Double> ar1 = generateAR1(100, 0.7, 0.3, 1.0, 42);

        AutoArima autoArima = new AutoArima(3, 2, 3, ArimaOrderSearch.Criterion.AIC).fit(ar1);
        ArimaOrderSearch.OrderScore best = autoArima.getBestOrder();
        System.out.printf("Best order : ARIMA(%d,%d,%d)%n",
                best.getP(), best.getD(), best.getQ());
        System.out.printf("Best AIC   : %.4f%n", best.getScore());
        System.out.printf("Is seasonal: %b%n", autoArima.isSeasonalModel());

        List<Double> autoForecast = autoArima.forecast(5);
        System.out.println("Auto forecast (h=5): " + fmt(autoForecast));

        // BIC criterion
        System.out.println("\n-- AutoARIMA (BIC) --");
        AutoArima autoArimaBic = new AutoArima(3, 2, 3, ArimaOrderSearch.Criterion.BIC).fit(ar1);
        ArimaOrderSearch.OrderScore bestBic = autoArimaBic.getBestOrder();
        System.out.printf("Best order (BIC) : ARIMA(%d,%d,%d)  BIC=%.4f%n",
                bestBic.getP(), bestBic.getD(), bestBic.getQ(), bestBic.getScore());

        // AICc criterion
        System.out.println("\n-- AutoARIMA (AICc) --");
        AutoArima autoArimaAicc = new AutoArima(2, 2, 2, ArimaOrderSearch.Criterion.AICC).fit(ar1);
        ArimaOrderSearch.OrderScore bestAicc = autoArimaAicc.getBestOrder();
        System.out.printf("Best order (AICc): ARIMA(%d,%d,%d)  AICc=%.4f%n",
                bestAicc.getP(), bestAicc.getD(), bestAicc.getQ(), bestAicc.getScore());

        // --- 2. AutoARIMA (seasonal) ---
        System.out.println("\n-- AutoARIMA (seasonal, period=12, criterion=AIC) --");
        List<Double> seasonal = generateSeasonalSeries(120, 12, 2.0, 1.0, 3);

        AutoArima autoSarima = new AutoArima(2, 1, 2, 1, 1, 1, 12,
                ArimaOrderSearch.Criterion.AIC).fit(seasonal);
        ArimaOrderSearch.OrderScore bestS = autoSarima.getBestOrder();
        System.out.printf("Best order: SARIMA(%d,%d,%d)(%d,%d,%d)_%d  AIC=%.4f%n",
                bestS.getP(), bestS.getD(), bestS.getQ(),
                bestS.getSeasonalP(), bestS.getSeasonalD(), bestS.getSeasonalQ(),
                bestS.getSeasonalPeriod(), bestS.getScore());

        List<Double> sarimaForecast = autoSarima.forecast(12);
        System.out.println("SARIMA forecast (h=12): " + fmt(sarimaForecast));

        // Access the fitted SARIMA model directly
        System.out.printf("isSeasonalModel: %b%n", autoSarima.isSeasonalModel());

        // --- 3. AutoETS (non-seasonal) ---
        System.out.println("\n-- AutoETS (non-seasonal) --");
        List<Double> trend = generateTrend(80, 20.0, 1.5, 2.0, 5);

        AutoETS autoEts = new AutoETS().fit(trend);
        System.out.printf("Best model type : %s%n", autoEts.getBestType());
        double[] params = autoEts.getBestParameters();
        System.out.printf("Best parameters : alpha=%.4f%s%n",
                params[0], params.length > 1 ? String.format("  gamma=%.4f", params[1]) : "");
        List<Double> etsForecast = autoEts.forecast(5);
        System.out.println("AutoETS forecast (h=5): " + fmt(etsForecast));

        // --- 4. AutoETS (seasonal) ---
        System.out.println("\n-- AutoETS (seasonal, period=12) --");
        List<Double> sesData = generateSeasonalTrend(120, 12, 50.0, 0.5, 15.0, 3.0, 6);

        AutoETS autoEtsSeasonal = new AutoETS(12).fit(sesData);
        System.out.printf("Best model type (seasonal): %s%n", autoEtsSeasonal.getBestType());
        List<Double> etsSeasonalForecast = autoEtsSeasonal.forecast(12);
        System.out.println("AutoETS seasonal forecast (h=12): " + fmt(etsSeasonalForecast));

        System.out.println();
    }

    static List<Double> generateAR1(int n, double phi, double drift, double sigma, long seed) {
        List<Double> out = new ArrayList<>(n);
        Random rng = new Random(seed);
        double val = 0.0;
        for (int i = 0; i < n; i++) {
            val = drift + phi * val + sigma * rng.nextGaussian();
            out.add(val);
        }
        return out;
    }

    static List<Double> generateSeasonalSeries(int n, int period, double slope, double sigma, long seed) {
        List<Double> out = new ArrayList<>(n);
        Random rng = new Random(seed);
        double trend = 0.0;
        for (int i = 0; i < n; i++) {
            double s = 5.0 * Math.sin(2.0 * Math.PI * i / period);
            trend += slope;
            out.add(trend + s + sigma * rng.nextGaussian());
        }
        return out;
    }

    static List<Double> generateTrend(int n, double start, double slope, double sigma, long seed) {
        List<Double> out = new ArrayList<>(n);
        Random rng = new Random(seed);
        for (int i = 0; i < n; i++) out.add(start + slope * i + sigma * rng.nextGaussian());
        return out;
    }

    static List<Double> generateSeasonalTrend(int n, int period, double base, double slope,
                                               double amplitude, double sigma, long seed) {
        List<Double> out = new ArrayList<>(n);
        Random rng = new Random(seed);
        for (int i = 0; i < n; i++) {
            double t = base + slope * i;
            double s = amplitude * Math.sin(2.0 * Math.PI * i / period);
            out.add(t + s + sigma * rng.nextGaussian());
        }
        return out;
    }

    static List<Double> fmt(List<Double> list) {
        List<Double> out = new ArrayList<>(list.size());
        for (Double v : list) out.add(v == null ? null : Math.round(v * 1000.0) / 1000.0);
        return out;
    }
}
