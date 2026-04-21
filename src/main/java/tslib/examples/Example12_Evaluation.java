package tslib.examples;

import tslib.diagnostics.LjungBoxTest;
import tslib.evaluation.BacktestResult;
import tslib.evaluation.BenchmarkMarkdown;
import tslib.evaluation.BenchmarkSummary;
import tslib.evaluation.ForecastFunction;
import tslib.evaluation.ForecastMetrics;
import tslib.evaluation.ModelBenchmark;
import tslib.evaluation.RollingOriginBacktest;
import tslib.evaluation.TrainTestSplit;
import tslib.model.ARIMA;
import tslib.model.SingleExpSmoothing;
import tslib.model.DoubleExpSmoothing;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Example 12: Model evaluation — train/test split, forecast accuracy metrics,
 * rolling-origin backtesting, multi-model benchmarking, and the Ljung-Box diagnostic.
 */
public class Example12_Evaluation {

    public static void run() {
        System.out.println("=== Example 12: Model Evaluation & Benchmarking ===\n");

        List<Double> series = generateAR1(200, 0.65, 0.5, 1.5, 11);

        // --- 1. Train/Test Split ---
        System.out.println("-- TrainTestSplit --");
        TrainTestSplit splitByIndex = TrainTestSplit.atIndex(series, 160);
        System.out.printf("Split at index 160  → train=%d  test=%d%n",
                splitByIndex.getTrain().size(), splitByIndex.getTest().size());

        TrainTestSplit splitByRatio = TrainTestSplit.ratio(series, 0.8);
        System.out.printf("Split at 80%% ratio  → train=%d  test=%d%n",
                splitByRatio.getTrain().size(), splitByRatio.getTest().size());

        List<Double> train = splitByIndex.getTrain();
        List<Double> test  = splitByIndex.getTest();

        // Fit and forecast on the training set
        ARIMA arima = new ARIMA(1, 0, 1).fit(train);
        List<Double> forecast = arima.forecast(test.size());

        // --- 2. Point Accuracy Metrics ---
        System.out.println("\n-- ForecastMetrics (one-shot) --");
        System.out.printf("MAE   : %.4f%n", ForecastMetrics.mae(test, forecast));
        System.out.printf("RMSE  : %.4f%n", ForecastMetrics.rmse(test, forecast));
        System.out.printf("MAPE  : %.4f%%%n", ForecastMetrics.mape(test, forecast));
        System.out.printf("sMAPE : %.4f%%%n", ForecastMetrics.smape(test, forecast));
        System.out.printf("MASE  : %.4f%n",  ForecastMetrics.mase(test, forecast, train, 1));
        System.out.printf("ME    : %.4f%n",  ForecastMetrics.meanError(test, forecast));

        // --- 3. Rolling-Origin Backtest ---
        System.out.println("\n-- RollingOriginBacktest (minTrain=100, horizon=5) --");
        RollingOriginBacktest backtest = new RollingOriginBacktest(100, 5);
        ForecastFunction arimaForecaster = (data, h) -> new ARIMA(1, 0, 1).fit(data).forecast(h);
        BacktestResult btResult = backtest.run(series, arimaForecaster);

        System.out.printf("Rolling-origin RMSE  : %.4f%n", btResult.getRmse());
        System.out.printf("Rolling-origin MAE   : %.4f%n", btResult.getMae());
        System.out.printf("Rolling-origin MAPE  : %.4f%%%n", btResult.getMape());
        System.out.printf("Rolling-origin sMAPE : %.4f%%%n", btResult.getSmape());
        System.out.printf("Rolling-origin MASE  : %.4f%n", btResult.getMase());
        System.out.printf("Number of origins    : %d%n", btResult.getOrigins().size());

        // Step size and seasonal period override
        RollingOriginBacktest backtestSeasonal = new RollingOriginBacktest(80, 3, 5, 12);
        BacktestResult seasonalResult = backtestSeasonal.run(series, arimaForecaster);
        System.out.printf("Seasonal MASE (period=12): %.4f%n", seasonalResult.getMase());

        // --- 4. ModelBenchmark ---
        System.out.println("\n-- ModelBenchmark: comparing ARIMA, SES, DES --");
        Map<String, ForecastFunction> forecasters = new LinkedHashMap<>();
        forecasters.put("ARIMA(1,0,1)", (data, h) -> new ARIMA(1, 0, 1).fit(data).forecast(h));
        forecasters.put("SES(0.3)",     (data, h) -> new SingleExpSmoothing(0.3).forecast(data, h));
        forecasters.put("DES(0.3,0.1)", (data, h) -> new DoubleExpSmoothing(0.3, 0.1, 1).forecast(data, h));
        forecasters.put("Naive",        (data, h) -> naiveForecast(data, h));

        ModelBenchmark benchmark = new ModelBenchmark(80, 5);
        List<BenchmarkSummary> summaries = benchmark.compare(series, forecasters);

        System.out.println("\nRanking (sorted by RMSE):");
        for (BenchmarkSummary s : summaries) {
            System.out.printf("  %-20s  RMSE=%.4f  MAE=%.4f  MAPE=%.4f%%%n",
                    s.getModelName(), s.getRmse(), s.getMae(), s.getMape());
        }

        // Markdown table
        System.out.println("\nMarkdown table:");
        System.out.println(BenchmarkMarkdown.toMarkdown(summaries));

        // Detailed results
        Map<String, BacktestResult> detailed = benchmark.compareDetailed(series, forecasters);
        System.out.println("Detailed origins per model:");
        detailed.forEach((name, r) ->
                System.out.printf("  %-20s  origins=%d%n", name, r.getOrigins().size()));

        // --- 5. Ljung-Box Test on residuals ---
        System.out.println("\n-- LjungBoxTest on ARIMA residuals --");
        ARIMA fitted = new ARIMA(1, 0, 1).fit(series);
        List<Double> resList = fitted.getResiduals();

        LjungBoxTest lb10 = new LjungBoxTest(resList, 10);
        System.out.printf("Ljung-Box (lags=10): stat=%.4f  p=%.4f  rejectsAt5%%=%b%n",
                lb10.getStatistic(), lb10.getPValue(), lb10.rejectsAtFivePercent());

        LjungBoxTest lb20 = new LjungBoxTest(resList, 20, 2);  // 2 df adjustment for ARIMA params
        System.out.printf("Ljung-Box (lags=20, dfAdj=2): stat=%.4f  p=%.4f%n",
                lb20.getStatistic(), lb20.getPValue());

        System.out.println();
    }

    static List<Double> naiveForecast(List<Double> data, int h) {
        double last = data.get(data.size() - 1);
        List<Double> out = new ArrayList<>(h);
        for (int i = 0; i < h; i++) out.add(last);
        return out;
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
}
