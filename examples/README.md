# tslib Examples

Standalone usage examples for the tslib time-series library.

## Examples

| File | What it shows |
|------|---------------|
| `Example01_DataIngestion.java` | `Collect` setup, summary statistics, ACF/PACF, transformations, ADF test |
| `Example02_MovingAverages.java` | SMA, EMA, WMA, CMA |
| `Example03_DataQuality.java` | Missing-value imputation, outlier detection, Winsorization |
| `Example04_Transformations.java` | Log, Box-Cox, differencing, and their inverses |
| `Example05_StationarityTests.java` | ADF and KPSS stationarity tests |
| `Example06_ARIMAForecasting.java` | ARIMA, SARIMA, ARIMAX forecasting with prediction intervals |
| `Example07_ExponentialSmoothing.java` | SES, DES (Holt), TES (Holt-Winters) |
| `Example08_StateSpaceModels.java` | `KalmanFilter`, `LocalLevelModel` with MLE fitting |
| `Example09_VARModel.java` | Multivariate forecasting with VAR |
| `Example10_STLDecomposition.java` | STL trend + seasonal + remainder decomposition |
| `Example11_AutoSelection.java` | `AutoARIMA` and `AutoETS` order/model selection |
| `Example12_Evaluation.java` | Metrics, rolling-origin backtest, benchmark, Ljung-Box |
| `Example13_Serialization.java` | Save and reload fitted models with `ModelSerializer` |
| `TslibExamples.java` | Runner that executes all 13 examples in sequence |

## Running all examples

```bash
./gradlew runExamples
```

This compiles the examples against the library and runs `TslibExamples`, which prints annotated output for every example to stdout.

## Running a single example

Each example exposes a static `run()` method. To run one in isolation, temporarily comment out the others in `TslibExamples.java` and re-run `./gradlew runExamples`.
