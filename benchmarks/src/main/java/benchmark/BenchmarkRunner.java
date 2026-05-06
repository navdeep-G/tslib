package benchmark;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

import tslib.model.arima.ARIMA;
import tslib.model.arima.SARIMA;
import tslib.model.arima.VARModel;
import tslib.model.arima.ArimaOrderSearch;
import tslib.model.expsmoothing.SingleExpSmoothing;
import tslib.model.expsmoothing.DoubleExpSmoothing;
import tslib.model.expsmoothing.TripleExpSmoothing;
import tslib.model.statespace.LocalLevelModel;
import tslib.model.statespace.KalmanFilter;
import tslib.movingaverage.SimpleMovingAverage;
import tslib.movingaverage.ExponentialMovingAverage;
import tslib.movingaverage.WeightedMovingAverage;
import tslib.decomposition.STLDecomposition;
import tslib.tests.AugmentedDickeyFuller;
import tslib.tests.KPSSTest;
import tslib.diagnostics.LjungBoxTest;
import tslib.dataquality.MissingValueImputer;
import tslib.transform.Transform;
import tslib.selection.AutoArima;
import tslib.evaluation.ForecastMetrics;

/**
 * Runs all tslib algorithm benchmarks and writes results to benchmarks/results/java_results.csv.
 *
 * Run from the project root via:
 *   ./gradlew benchmarks:run
 *
 * Output format: library,algorithm,dataset,metric,value
 * Metrics: MAE, RMSE, MAPE (for forecasting), exec_ms, stat, pvalue (for tests),
 *          remainder_var (for decomposition), smoothing_mae (for moving averages).
 */
public class BenchmarkRunner {

    private static final int WARMUP_RUNS = 2;
    private static final int TIMED_RUNS = 5;
    private static final String RESULTS_FILE = "benchmarks/results/java_results.csv";
    private static final String DATA_DIR = "data";
    private static final String AP_CSV = "benchmarks/data/airpassengers.csv";

    // -------------------------------------------------------------------------
    // Entry point
    // -------------------------------------------------------------------------

    public static void main(String[] args) throws Exception {
        List<double[]> hotel = loadTxt(DATA_DIR + "/hotel.txt");
        List<double[]> jj = loadTxt(DATA_DIR + "/jj.txt");
        List<double[]> ap = loadCsv(AP_CSV);
        List<double[]> caRaw = loadTxt(DATA_DIR + "/CA_Unemployment_Rate.txt");
        List<double[]> ca = caRaw.subList(0, 168);

        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"library", "algorithm", "dataset", "metric", "value"});

        System.out.println("=== tslib Benchmarks ===\n");

        rows.addAll(benchmarkArima(hotel, "hotel"));
        rows.addAll(benchmarkSarima(ap, "airpassengers"));
        rows.addAll(benchmarkSingleEts(jj, "jj"));
        rows.addAll(benchmarkDoubleEts(hotel, "hotel"));
        rows.addAll(benchmarkTripleEts(ap, "airpassengers"));
        rows.addAll(benchmarkSma(hotel, "hotel"));
        rows.addAll(benchmarkEma(hotel, "hotel"));
        rows.addAll(benchmarkStl(ap, "airpassengers"));
        rows.addAll(benchmarkVar(hotel, ca));
        rows.addAll(benchmarkAdf(hotel, "hotel"));
        rows.addAll(benchmarkKpss(hotel, "hotel"));
        rows.addAll(benchmarkLocalLevel(jj, "jj"));
        rows.addAll(benchmarkAutoArima(hotel, "hotel"));
        rows.addAll(benchmarkWma(hotel, "hotel"));
        rows.addAll(benchmarkLjungBox(hotel, "hotel"));
        rows.addAll(benchmarkImputation(hotel, "hotel"));
        rows.addAll(benchmarkBoxCox(ap, "airpassengers"));

        writeCsv(rows, RESULTS_FILE);
        System.out.println("\nResults written to: " + RESULTS_FILE);
    }

    // -------------------------------------------------------------------------
    // Forecasting benchmarks
    // -------------------------------------------------------------------------

    static List<String[]> benchmarkArima(List<double[]> data, String dataset) {
        System.out.print("ARIMA(1,1,1) ... ");
        int holdout = 12;
        List<Double> train = toList(data, 0, data.size() - holdout);
        List<Double> test = toList(data, data.size() - holdout, data.size());

        // warmup
        for (int i = 0; i < WARMUP_RUNS; i++) {
            new ARIMA(1, 1, 1).fit(train).forecast(holdout);
        }

        long[] nanos = new long[TIMED_RUNS];
        List<Double> forecasts = null;
        for (int i = 0; i < TIMED_RUNS; i++) {
            long t0 = System.nanoTime();
            forecasts = new ARIMA(1, 1, 1).fit(train).forecast(holdout);
            nanos[i] = System.nanoTime() - t0;
        }

        double execMs = medianMs(nanos);
        double mae = ForecastMetrics.mae(test, forecasts);
        double rmse = ForecastMetrics.rmse(test, forecasts);
        double mape = ForecastMetrics.mape(test, forecasts);

        printRow("ARIMA(1,1,1)", dataset, mae, rmse, mape, execMs);
        return rows("tslib", "ARIMA_1_1_1", dataset, mae, rmse, mape, execMs);
    }

    static List<String[]> benchmarkSarima(List<double[]> data, String dataset) {
        System.out.print("SARIMA(1,1,1)(1,1,0,12) ... ");
        int holdout = 12;
        List<Double> train = toList(data, 0, data.size() - holdout);
        List<Double> test = toList(data, data.size() - holdout, data.size());

        for (int i = 0; i < WARMUP_RUNS; i++) {
            new SARIMA(1, 1, 1, 1, 1, 0, 12).fit(train).forecast(holdout);
        }

        long[] nanos = new long[TIMED_RUNS];
        List<Double> forecasts = null;
        for (int i = 0; i < TIMED_RUNS; i++) {
            long t0 = System.nanoTime();
            forecasts = new SARIMA(1, 1, 1, 1, 1, 0, 12).fit(train).forecast(holdout);
            nanos[i] = System.nanoTime() - t0;
        }

        double execMs = medianMs(nanos);
        double mae = ForecastMetrics.mae(test, forecasts);
        double rmse = ForecastMetrics.rmse(test, forecasts);
        double mape = ForecastMetrics.mape(test, forecasts);

        printRow("SARIMA(1,1,1)(1,1,0,12)", dataset, mae, rmse, mape, execMs);
        return rows("tslib", "SARIMA_1_1_1_1_1_0_12", dataset, mae, rmse, mape, execMs);
    }

    static List<String[]> benchmarkSingleEts(List<double[]> data, String dataset) {
        System.out.print("SingleETS(α=0.3) ... ");
        int holdout = 8;
        List<Double> train = toList(data, 0, data.size() - holdout);
        List<Double> test = toList(data, data.size() - holdout, data.size());

        for (int i = 0; i < WARMUP_RUNS; i++) {
            new SingleExpSmoothing(0.3).fit(train).forecast(holdout);
        }

        long[] nanos = new long[TIMED_RUNS];
        List<Double> forecasts = null;
        for (int i = 0; i < TIMED_RUNS; i++) {
            long t0 = System.nanoTime();
            forecasts = new SingleExpSmoothing(0.3).fit(train).forecast(holdout);
            nanos[i] = System.nanoTime() - t0;
        }

        double execMs = medianMs(nanos);
        double mae = ForecastMetrics.mae(test, forecasts);
        double rmse = ForecastMetrics.rmse(test, forecasts);
        double mape = ForecastMetrics.mape(test, forecasts);

        printRow("SingleETS(α=0.3)", dataset, mae, rmse, mape, execMs);
        return rows("tslib", "SingleETS_a0.3", dataset, mae, rmse, mape, execMs);
    }

    static List<String[]> benchmarkDoubleEts(List<double[]> data, String dataset) {
        System.out.print("DoubleETS(α=0.3,γ=0.1) ... ");
        int holdout = 12;
        List<Double> train = toList(data, 0, data.size() - holdout);
        List<Double> test = toList(data, data.size() - holdout, data.size());

        // tslib DoubleExpSmoothing: alpha=level, gamma=trend
        for (int i = 0; i < WARMUP_RUNS; i++) {
            new DoubleExpSmoothing(0.3, 0.1, 0).fit(train).forecast(holdout);
        }

        long[] nanos = new long[TIMED_RUNS];
        List<Double> forecasts = null;
        for (int i = 0; i < TIMED_RUNS; i++) {
            long t0 = System.nanoTime();
            forecasts = new DoubleExpSmoothing(0.3, 0.1, 0).fit(train).forecast(holdout);
            nanos[i] = System.nanoTime() - t0;
        }

        double execMs = medianMs(nanos);
        double mae = ForecastMetrics.mae(test, forecasts);
        double rmse = ForecastMetrics.rmse(test, forecasts);
        double mape = ForecastMetrics.mape(test, forecasts);

        printRow("DoubleETS(α=0.3,γ=0.1)", dataset, mae, rmse, mape, execMs);
        return rows("tslib", "DoubleETS_a0.3_g0.1", dataset, mae, rmse, mape, execMs);
    }

    static List<String[]> benchmarkTripleEts(List<double[]> data, String dataset) {
        System.out.print("TripleETS/HW(α=0.3,β=0.2,γ=0.1,s=12) ... ");
        // tslib TripleExpSmoothing: alpha=level, beta=seasonal, gamma=trend
        int holdout = 12;
        List<Double> train = toList(data, 0, data.size() - holdout);
        List<Double> test = toList(data, data.size() - holdout, data.size());

        for (int i = 0; i < WARMUP_RUNS; i++) {
            new TripleExpSmoothing(0.3, 0.2, 0.1, 12).fit(train).forecast(holdout);
        }

        long[] nanos = new long[TIMED_RUNS];
        List<Double> forecasts = null;
        for (int i = 0; i < TIMED_RUNS; i++) {
            long t0 = System.nanoTime();
            forecasts = new TripleExpSmoothing(0.3, 0.2, 0.1, 12).fit(train).forecast(holdout);
            nanos[i] = System.nanoTime() - t0;
        }

        double execMs = medianMs(nanos);
        double mae = ForecastMetrics.mae(test, forecasts);
        double rmse = ForecastMetrics.rmse(test, forecasts);
        double mape = ForecastMetrics.mape(test, forecasts);

        printRow("TripleETS/HW(α=0.3,β=0.2,γ=0.1,s=12)", dataset, mae, rmse, mape, execMs);
        return rows("tslib", "HoltWinters_a0.3_b0.2_g0.1_s12", dataset, mae, rmse, mape, execMs);
    }

    static List<String[]> benchmarkLocalLevel(List<double[]> data, String dataset) {
        System.out.print("LocalLevel (MLE) ... ");
        int holdout = 8;
        List<Double> train = toList(data, 0, data.size() - holdout);
        List<Double> test = toList(data, data.size() - holdout, data.size());

        for (int i = 0; i < WARMUP_RUNS; i++) {
            new LocalLevelModel().fit(train).forecast(holdout);
        }

        long[] nanos = new long[TIMED_RUNS];
        List<Double> forecasts = null;
        for (int i = 0; i < TIMED_RUNS; i++) {
            long t0 = System.nanoTime();
            forecasts = new LocalLevelModel().fit(train).forecast(holdout);
            nanos[i] = System.nanoTime() - t0;
        }

        double execMs = medianMs(nanos);
        double mae = ForecastMetrics.mae(test, forecasts);
        double rmse = ForecastMetrics.rmse(test, forecasts);
        double mape = ForecastMetrics.mape(test, forecasts);

        printRow("LocalLevel (MLE)", dataset, mae, rmse, mape, execMs);
        return rows("tslib", "LocalLevel_MLE", dataset, mae, rmse, mape, execMs);
    }

    static List<String[]> benchmarkAutoArima(List<double[]> data, String dataset) {
        System.out.print("AutoARIMA(max p,d,q=3) ... ");
        int holdout = 12;
        List<Double> train = toList(data, 0, data.size() - holdout);
        List<Double> test = toList(data, data.size() - holdout, data.size());

        // Warmup
        for (int i = 0; i < WARMUP_RUNS; i++) {
            new AutoArima(3, 2, 3, ArimaOrderSearch.Criterion.AIC).fit(train).forecast(holdout);
        }

        long[] nanos = new long[TIMED_RUNS];
        List<Double> forecasts = null;
        for (int i = 0; i < TIMED_RUNS; i++) {
            long t0 = System.nanoTime();
            forecasts = new AutoArima(3, 2, 3, ArimaOrderSearch.Criterion.AIC).fit(train).forecast(holdout);
            nanos[i] = System.nanoTime() - t0;
        }

        double execMs = medianMs(nanos);
        double mae = ForecastMetrics.mae(test, forecasts);
        double rmse = ForecastMetrics.rmse(test, forecasts);
        double mape = ForecastMetrics.mape(test, forecasts);

        printRow("AutoARIMA(max p,d,q=3)", dataset, mae, rmse, mape, execMs);
        return rows("tslib", "AutoARIMA_max3_AIC", dataset, mae, rmse, mape, execMs);
    }

    // -------------------------------------------------------------------------
    // Moving average benchmarks (smoothing quality, not holdout forecast)
    // -------------------------------------------------------------------------

    static List<String[]> benchmarkSma(List<double[]> data, String dataset) {
        System.out.print("SMA(window=7) ... ");
        List<Double> series = toList(data, 0, data.size());

        for (int i = 0; i < WARMUP_RUNS; i++) {
            new SimpleMovingAverage(7).compute(series);
        }

        long[] nanos = new long[TIMED_RUNS];
        List<Double> smoothed = null;
        for (int i = 0; i < TIMED_RUNS; i++) {
            long t0 = System.nanoTime();
            smoothed = new SimpleMovingAverage(7).compute(series);
            nanos[i] = System.nanoTime() - t0;
        }

        double execMs = medianMs(nanos);
        // Compare smoothed values (skip initial nulls) to actual for a smoothing-quality MAE
        List<Double> actual = new ArrayList<>();
        List<Double> preds = new ArrayList<>();
        for (int i = 0; i < smoothed.size(); i++) {
            if (smoothed.get(i) != null) {
                actual.add(series.get(i));
                preds.add(smoothed.get(i));
            }
        }
        double smoothingMae = ForecastMetrics.mae(actual, preds);
        double smoothingRmse = ForecastMetrics.rmse(actual, preds);

        System.out.printf("smoothing_mae=%.4f  rmse=%.4f  exec=%.3f ms%n",
                smoothingMae, smoothingRmse, execMs);

        List<String[]> out = new ArrayList<>();
        out.add(new String[]{"tslib", "SMA_window7", dataset, "smoothing_mae", fmt(smoothingMae)});
        out.add(new String[]{"tslib", "SMA_window7", dataset, "smoothing_rmse", fmt(smoothingRmse)});
        out.add(new String[]{"tslib", "SMA_window7", dataset, "exec_ms", fmt(execMs)});
        return out;
    }

    static List<String[]> benchmarkEma(List<double[]> data, String dataset) {
        System.out.print("EMA(α=0.2) ... ");
        List<Double> series = toList(data, 0, data.size());

        for (int i = 0; i < WARMUP_RUNS; i++) {
            new ExponentialMovingAverage(0.2).compute(series);
        }

        long[] nanos = new long[TIMED_RUNS];
        List<Double> smoothed = null;
        for (int i = 0; i < TIMED_RUNS; i++) {
            long t0 = System.nanoTime();
            smoothed = new ExponentialMovingAverage(0.2).compute(series);
            nanos[i] = System.nanoTime() - t0;
        }

        double execMs = medianMs(nanos);
        double smoothingMae = ForecastMetrics.mae(series, smoothed);
        double smoothingRmse = ForecastMetrics.rmse(series, smoothed);

        System.out.printf("smoothing_mae=%.4f  rmse=%.4f  exec=%.3f ms%n",
                smoothingMae, smoothingRmse, execMs);

        List<String[]> out = new ArrayList<>();
        out.add(new String[]{"tslib", "EMA_a0.2", dataset, "smoothing_mae", fmt(smoothingMae)});
        out.add(new String[]{"tslib", "EMA_a0.2", dataset, "smoothing_rmse", fmt(smoothingRmse)});
        out.add(new String[]{"tslib", "EMA_a0.2", dataset, "exec_ms", fmt(execMs)});
        return out;
    }

    // -------------------------------------------------------------------------
    // STL decomposition benchmark
    // -------------------------------------------------------------------------

    static List<String[]> benchmarkStl(List<double[]> data, String dataset) {
        System.out.print("STL(period=12) ... ");
        List<Double> series = toList(data, 0, data.size());

        for (int i = 0; i < WARMUP_RUNS; i++) {
            new STLDecomposition(12).decompose(series);
        }

        long[] nanos = new long[TIMED_RUNS];
        STLDecomposition.Result result = null;
        for (int i = 0; i < TIMED_RUNS; i++) {
            long t0 = System.nanoTime();
            result = new STLDecomposition(12).decompose(series);
            nanos[i] = System.nanoTime() - t0;
        }

        double execMs = medianMs(nanos);
        double remainderVar = variance(result.getRemainder());
        double seasonalVar = variance(result.getSeasonal());
        double trendVar = variance(result.getTrend());
        double totalVar = variance(series);
        double seasonalStrength = Math.max(0, 1.0 - remainderVar / (seasonalVar + remainderVar));
        double trendStrength = Math.max(0, 1.0 - remainderVar / (trendVar + remainderVar));

        System.out.printf("seasonal_strength=%.4f  trend_strength=%.4f  remainder_var=%.4f  exec=%.3f ms%n",
                seasonalStrength, trendStrength, remainderVar, execMs);

        List<String[]> out = new ArrayList<>();
        out.add(new String[]{"tslib", "STL_period12", dataset, "seasonal_strength", fmt(seasonalStrength)});
        out.add(new String[]{"tslib", "STL_period12", dataset, "trend_strength", fmt(trendStrength)});
        out.add(new String[]{"tslib", "STL_period12", dataset, "remainder_var", fmt(remainderVar)});
        out.add(new String[]{"tslib", "STL_period12", dataset, "exec_ms", fmt(execMs)});
        return out;
    }

    // -------------------------------------------------------------------------
    // VAR benchmark
    // -------------------------------------------------------------------------

    static List<String[]> benchmarkVar(List<double[]> series1, List<double[]> series2) {
        System.out.print("VAR(1) hotel+ca_unemployment ... ");
        int holdout = 8;
        List<Double> train1 = toList(series1, 0, series1.size() - holdout);
        List<Double> test1 = toList(series1, series1.size() - holdout, series1.size());
        List<Double> train2 = toList(series2, 0, series2.size() - holdout);

        for (int i = 0; i < WARMUP_RUNS; i++) {
            new VARModel(1).fit(List.of(train1, train2)).forecast(holdout);
        }

        long[] nanos = new long[TIMED_RUNS];
        List<List<Double>> forecasts = null;
        for (int i = 0; i < TIMED_RUNS; i++) {
            long t0 = System.nanoTime();
            forecasts = new VARModel(1).fit(List.of(train1, train2)).forecast(holdout);
            nanos[i] = System.nanoTime() - t0;
        }

        double execMs = medianMs(nanos);
        // Report accuracy only for series 1 (hotel)
        double mae = ForecastMetrics.mae(test1, forecasts.get(0));
        double rmse = ForecastMetrics.rmse(test1, forecasts.get(0));
        double mape = ForecastMetrics.mape(test1, forecasts.get(0));

        printRow("VAR(1)", "hotel+ca_unemp", mae, rmse, mape, execMs);
        return rows("tslib", "VAR_p1", "hotel+ca_unemp", mae, rmse, mape, execMs);
    }

    // -------------------------------------------------------------------------
    // Statistical test benchmarks
    // -------------------------------------------------------------------------

    static List<String[]> benchmarkAdf(List<double[]> data, String dataset) {
        System.out.print("ADF test ... ");
        List<Double> series = toList(data, 0, data.size());

        for (int i = 0; i < WARMUP_RUNS; i++) {
            new AugmentedDickeyFuller(series);
        }

        long[] nanos = new long[TIMED_RUNS];
        AugmentedDickeyFuller adf = null;
        for (int i = 0; i < TIMED_RUNS; i++) {
            long t0 = System.nanoTime();
            adf = new AugmentedDickeyFuller(series);
            nanos[i] = System.nanoTime() - t0;
        }

        double execMs = medianMs(nanos);
        double stat = adf.getAdfStat();
        double pval = adf.getPValue();

        System.out.printf("stat=%.4f  p=%.4f  stationary=%b  exec=%.3f ms%n",
                stat, pval, adf.isStationary(), execMs);

        List<String[]> out = new ArrayList<>();
        out.add(new String[]{"tslib", "ADF_test", dataset, "statistic", fmt(stat)});
        out.add(new String[]{"tslib", "ADF_test", dataset, "pvalue", fmt(pval)});
        out.add(new String[]{"tslib", "ADF_test", dataset, "exec_ms", fmt(execMs)});
        return out;
    }

    static List<String[]> benchmarkKpss(List<double[]> data, String dataset) {
        System.out.print("KPSS test ... ");
        List<Double> series = toList(data, 0, data.size());

        for (int i = 0; i < WARMUP_RUNS; i++) {
            new KPSSTest(series);
        }

        long[] nanos = new long[TIMED_RUNS];
        KPSSTest kpss = null;
        for (int i = 0; i < TIMED_RUNS; i++) {
            long t0 = System.nanoTime();
            kpss = new KPSSTest(series);
            nanos[i] = System.nanoTime() - t0;
        }

        double execMs = medianMs(nanos);
        double stat = kpss.getStatistic();

        System.out.printf("stat=%.4f  stationary@5%%=%b  exec=%.3f ms%n",
                stat, kpss.isStationaryAtFivePercent(), execMs);

        List<String[]> out = new ArrayList<>();
        out.add(new String[]{"tslib", "KPSS_test", dataset, "statistic", fmt(stat)});
        out.add(new String[]{"tslib", "KPSS_test", dataset, "stationary_5pct",
                String.valueOf(kpss.isStationaryAtFivePercent())});
        out.add(new String[]{"tslib", "KPSS_test", dataset, "exec_ms", fmt(execMs)});
        return out;
    }

    // -------------------------------------------------------------------------
    // Additional benchmarks
    // -------------------------------------------------------------------------

    // NA positions injected consistently across all three benchmark scripts
    private static final int[] NA_POSITIONS = {15, 30, 45, 60, 75, 90, 105, 120, 135, 150};

    static List<String[]> benchmarkWma(List<double[]> data, String dataset) {
        System.out.print("WMA(period=7) ... ");
        List<Double> series = toList(data, 0, data.size());

        for (int i = 0; i < WARMUP_RUNS; i++) {
            new WeightedMovingAverage(7).compute(series);
        }

        long[] nanos = new long[TIMED_RUNS];
        List<Double> smoothed = null;
        for (int i = 0; i < TIMED_RUNS; i++) {
            long t0 = System.nanoTime();
            smoothed = new WeightedMovingAverage(7).compute(series);
            nanos[i] = System.nanoTime() - t0;
        }

        double execMs = medianMs(nanos);
        List<Double> actual = new ArrayList<>();
        List<Double> preds = new ArrayList<>();
        for (int i = 0; i < smoothed.size(); i++) {
            if (smoothed.get(i) != null) {
                actual.add(series.get(i));
                preds.add(smoothed.get(i));
            }
        }
        double smoothingMae = ForecastMetrics.mae(actual, preds);
        double smoothingRmse = ForecastMetrics.rmse(actual, preds);

        System.out.printf("smoothing_mae=%.4f  rmse=%.4f  exec=%.3f ms%n",
                smoothingMae, smoothingRmse, execMs);

        List<String[]> out = new ArrayList<>();
        out.add(new String[]{"tslib", "WMA_period7", dataset, "smoothing_mae", fmt(smoothingMae)});
        out.add(new String[]{"tslib", "WMA_period7", dataset, "smoothing_rmse", fmt(smoothingRmse)});
        out.add(new String[]{"tslib", "WMA_period7", dataset, "exec_ms", fmt(execMs)});
        return out;
    }

    static List<String[]> benchmarkLjungBox(List<double[]> data, String dataset) {
        System.out.print("Ljung-Box (lags=12, ARIMA residuals) ... ");
        List<Double> series = toList(data, 0, data.size());
        List<Double> residuals = new ARIMA(1, 1, 1).fit(series).getResiduals();

        for (int i = 0; i < WARMUP_RUNS; i++) {
            new LjungBoxTest(residuals, 12);
        }

        long[] nanos = new long[TIMED_RUNS];
        LjungBoxTest lb = null;
        for (int i = 0; i < TIMED_RUNS; i++) {
            long t0 = System.nanoTime();
            lb = new LjungBoxTest(residuals, 12);
            nanos[i] = System.nanoTime() - t0;
        }

        double execMs = medianMs(nanos);
        double stat = lb.getStatistic();
        double pval = lb.getPValue();

        System.out.printf("stat=%.4f  p=%.4f  exec=%.3f ms%n", stat, pval, execMs);

        List<String[]> out = new ArrayList<>();
        out.add(new String[]{"tslib", "LjungBox_lag12", dataset, "statistic", fmt(stat)});
        out.add(new String[]{"tslib", "LjungBox_lag12", dataset, "pvalue", fmt(pval)});
        out.add(new String[]{"tslib", "LjungBox_lag12", dataset, "exec_ms", fmt(execMs)});
        return out;
    }

    static List<String[]> benchmarkImputation(List<double[]> data, String dataset) {
        System.out.print("Imputation (linear interpolation, 10 NAs) ... ");
        List<Double> original = toList(data, 0, data.size());

        List<Double> corrupted = new ArrayList<>(original);
        for (int pos : NA_POSITIONS) {
            corrupted.set(pos, null);
        }

        for (int i = 0; i < WARMUP_RUNS; i++) {
            MissingValueImputer.impute(new ArrayList<>(corrupted),
                    MissingValueImputer.Strategy.LINEAR_INTERPOLATION);
        }

        long[] nanos = new long[TIMED_RUNS];
        List<Double> imputed = null;
        for (int i = 0; i < TIMED_RUNS; i++) {
            long t0 = System.nanoTime();
            imputed = MissingValueImputer.impute(new ArrayList<>(corrupted),
                    MissingValueImputer.Strategy.LINEAR_INTERPOLATION);
            nanos[i] = System.nanoTime() - t0;
        }

        double execMs = medianMs(nanos);
        double mae = 0;
        for (int pos : NA_POSITIONS) {
            mae += Math.abs(original.get(pos) - imputed.get(pos));
        }
        mae /= NA_POSITIONS.length;

        System.out.printf("imputation_mae=%.4f  exec=%.3f ms%n", mae, execMs);

        List<String[]> out = new ArrayList<>();
        out.add(new String[]{"tslib", "Imputation_linear", dataset, "imputation_mae", fmt(mae)});
        out.add(new String[]{"tslib", "Imputation_linear", dataset, "exec_ms", fmt(execMs)});
        return out;
    }

    static List<String[]> benchmarkBoxCox(List<double[]> data, String dataset) {
        System.out.print("Box-Cox lambda search ... ");
        List<Double> series = toList(data, 0, data.size());

        for (int i = 0; i < WARMUP_RUNS; i++) {
            Transform.boxCoxLambdaSearch(series);
        }

        long[] nanos = new long[TIMED_RUNS];
        double lambda = 0;
        for (int i = 0; i < TIMED_RUNS; i++) {
            long t0 = System.nanoTime();
            lambda = Transform.boxCoxLambdaSearch(series);
            nanos[i] = System.nanoTime() - t0;
        }

        double execMs = medianMs(nanos);
        System.out.printf("lambda=%.4f  exec=%.3f ms%n", lambda, execMs);

        List<String[]> out = new ArrayList<>();
        out.add(new String[]{"tslib", "BoxCox_lambda", dataset, "lambda", fmt(lambda)});
        out.add(new String[]{"tslib", "BoxCox_lambda", dataset, "exec_ms", fmt(execMs)});
        return out;
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static List<double[]> loadTxt(String path) throws IOException {
        return Files.readAllLines(Path.of(path)).stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> new double[]{Double.parseDouble(s)})
                .collect(Collectors.toList());
    }

    private static List<double[]> loadCsv(String path) throws IOException {
        return Files.readAllLines(Path.of(path)).stream()
                .skip(1) // header
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> new double[]{Double.parseDouble(s.split(",")[0])})
                .collect(Collectors.toList());
    }

    private static List<Double> toList(List<double[]> data, int from, int to) {
        return data.subList(from, to).stream()
                .map(row -> row[0])
                .collect(Collectors.toList());
    }

    private static double medianMs(long[] nanos) {
        long[] sorted = nanos.clone();
        Arrays.sort(sorted);
        return sorted[sorted.length / 2] / 1_000_000.0;
    }

    private static double variance(List<Double> vals) {
        double mean = vals.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        return vals.stream().mapToDouble(v -> (v - mean) * (v - mean)).average().orElse(0);
    }

    private static String fmt(double v) {
        return String.format("%.6f", v);
    }

    private static List<String[]> rows(String lib, String algo, String dataset,
            double mae, double rmse, double mape, double execMs) {
        return List.of(
                new String[]{lib, algo, dataset, "MAE", fmt(mae)},
                new String[]{lib, algo, dataset, "RMSE", fmt(rmse)},
                new String[]{lib, algo, dataset, "MAPE", fmt(mape)},
                new String[]{lib, algo, dataset, "exec_ms", fmt(execMs)}
        );
    }

    private static void printRow(String algo, String dataset,
            double mae, double rmse, double mape, double execMs) {
        System.out.printf("%-45s  MAE=%-8.4f  RMSE=%-8.4f  MAPE=%-6.2f%%  exec=%.3f ms%n",
                algo + " [" + dataset + "]", mae, rmse, mape, execMs);
    }

    private static void writeCsv(List<String[]> rows, String path) throws IOException {
        Files.createDirectories(Path.of(path).getParent());
        try (PrintWriter pw = new PrintWriter(new FileWriter(path))) {
            for (String[] row : rows) {
                pw.println(String.join(",", row));
            }
        }
    }
}
