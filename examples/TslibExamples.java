/**
 * Main runner for all tslib usage examples.
 *
 * Run this class to see live output from every example. Each example is
 * self-contained — it generates its own synthetic data and prints annotated
 * results to stdout so you can follow along without any external files.
 *
 * Examples:
 *   01 — Data ingestion & descriptive statistics (Collect, Stats)
 *   02 — Moving averages: SMA, EMA, WMA, CMA
 *   03 — Data quality: imputation, outlier detection, Winsorization
 *   04 — Transformations: log, Box-Cox, differencing, inverses
 *   05 — Stationarity tests: ADF and KPSS
 *   06 — ARIMA / SARIMA / ARIMAX forecasting with prediction intervals
 *   07 — Exponential smoothing: SES, DES (Holt), TES (Holt-Winters)
 *   08 — State-space models: KalmanFilter, LocalLevelModel (MLE)
 *   09 — VAR model: multivariate forecasting
 *   10 — STL decomposition: trend + seasonal + remainder
 *   11 — Auto model selection: AutoARIMA, AutoETS
 *   12 — Evaluation: metrics, rolling-origin backtest, benchmark, Ljung-Box
 *   13 — Model serialization: save and reload fitted models
 */
public class TslibExamples {

    public static void main(String[] args) {
        Example01_DataIngestion.run();
        Example02_MovingAverages.run();
        Example03_DataQuality.run();
        Example04_Transformations.run();
        Example05_StationarityTests.run();
        Example06_ARIMAForecasting.run();
        Example07_ExponentialSmoothing.run();
        Example08_StateSpaceModels.run();
        Example09_VARModel.run();
        Example10_STLDecomposition.run();
        Example11_AutoSelection.run();
        Example12_Evaluation.run();
        Example13_Serialization.run();

        System.out.println("All examples completed.");
    }
}
