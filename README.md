# tslib

A small Java library for time-series analysis, forecasting, evaluation, and data-quality workflows.

![tslib demo](docs/demo.gif)

## What is included

- exponential smoothing: single, double, and triple
- moving averages: simple, cumulative, exponential, and weighted
- transformations: log, roots, Box-Cox, differencing, and seasonal differencing
- statistics: mean, variance, autocovariance, ACF, and PACF
- stationarity testing with Augmented Dickey-Fuller and KPSS
- model-based forecasting with ARIMA, SARIMA, and ARIMAX
- decomposition with STL-style trend/seasonal/remainder extraction
- compact state-space forecasting with a local-level Kalman model
- forecast evaluation: train/test splits, rolling-origin backtests, metrics, and Ljung-Box diagnostics
- prediction intervals for ARIMA, SARIMA, and local-level forecasts
- auto-selection helpers for ARIMA/SARIMA and exponential smoothing
- data-quality utilities for missing values, outliers, and winsorization
- release-hardening helpers: wrapper bootstrap scripts, publishing scaffolding, CI workflows, and model benchmark utilities

## Build

The project targets Java 17 and uses Gradle.

```bash
./gradlew test
```

This snapshot now includes a lightweight `./gradlew` bootstrap script that reads `gradle/wrapper/gradle-wrapper.properties`, downloads the configured Gradle distribution when needed, and then runs the build. It is intended to keep CI and contributor onboarding working even without committing a generated `gradle-wrapper.jar`.

## Package map

Primary implementation packages:

- `tslib.collect`
- `tslib.dataquality`
- `tslib.decomposition`
- `tslib.diagnostics`
- `tslib.evaluation`
- `tslib.math`
- `tslib.model.arima`
- `tslib.model.expsmoothing`
- `tslib.model.statespace`
- `tslib.movingaverage`
- `tslib.selection`
- `tslib.tests`
- `tslib.transform`
- `tslib.util`

Compatibility aliases added in this patch series:

- `tslib.model.*` forwards to `tslib.model.expsmoothing.*`
- `tslib.model.ARIMA` forwards to `tslib.model.arima.ARIMA`
- `tslib.model.SARIMA` forwards to `tslib.model.arima.SARIMA`
- `tslib.model.ARIMAX` forwards to `tslib.model.arima.ARIMAX`
- `tslib.model.LocalLevelModel` forwards to `tslib.model.statespace.LocalLevelModel`
- `tslib.stats.Stats` forwards to `tslib.util.Stats`

## Model chooser

| Use case | Start with | Compare against | Notes |
| --- | --- | --- | --- |
| Level-only or gently trending series | `DoubleExpSmoothing` | `ARIMA`, `LocalLevelModel` | Good lightweight baseline. |
| Strong repeating seasonality | `TripleExpSmoothing` | `SARIMA` | Prefer `SARIMA` when seasonal autocorrelation matters. |
| Differenced stationary dynamics | `ARIMA` | `LocalLevelModel` | Inspect ADF/KPSS and ACF/PACF first. |
| Seasonality plus differencing | `SARIMA` | `TripleExpSmoothing` | Use the seasonal period consistently across preprocessing and backtests. |
| External drivers available | `ARIMAX` | `ARIMA` | Validate regressor alignment and holdout availability. |
| Smooth latent state / baseline uncertainty | `LocalLevelModel` | `ARIMA` | Good compact state-space benchmark. |

For a longer guide, see `docs/MODEL_SELECTION_GUIDE.md`.

## Quick start

```java
import java.util.List;
import tslib.dataquality.MissingValueImputer;
import tslib.evaluation.BacktestResult;
import tslib.evaluation.IntervalForecast;
import tslib.evaluation.RollingOriginBacktest;
import tslib.model.ARIMA;
import tslib.model.ARIMAX;
import tslib.model.LocalLevelModel;
import tslib.selection.AutoArima;
import tslib.tests.KPSSTest;
import tslib.transform.Differencing;

List<Double> raw = MissingValueImputer.linearInterpolation(java.util.Arrays.asList(10.0, null, 12.0, 13.0, 14.0));
List<Double> diff = Differencing.difference(raw);
KPSSTest kpss = new KPSSTest(diff);

ARIMA arima = new ARIMA(1, 1, 0).fit(raw);
IntervalForecast intervals = arima.forecastWithIntervals(3, 0.95);

ARIMAX arimax = new ARIMAX(0, 0, 0).fit(
        List.of(7.0, 9.0, 11.0, 13.0),
        new double[][] {{1.0}, {2.0}, {3.0}, {4.0}});
List<Double> arimaxForecast = arimax.forecast(new double[][] {{5.0}, {6.0}});

RollingOriginBacktest backtest = new RollingOriginBacktest(4, 1);
BacktestResult result = backtest.run(raw, (train, horizon) -> new ARIMA(0, 1, 0).forecast(train, horizon));

AutoArima autoArima = new AutoArima(2, 1, 2, tslib.model.arima.ArimaOrderSearch.Criterion.AIC).fit(raw);
LocalLevelModel localLevel = new LocalLevelModel().fit(raw);
```

## Performance

tslib is benchmarked against equivalent implementations in Python (statsmodels · pmdarima · pandas · scipy) and R (forecast · tseries · vars · KFAS · TTR · zoo).
Full methodology, raw CSVs, and cross-language comparison are in [`benchmarks/`](benchmarks/).

### Execution speed *(median of 5 timed runs after 2 warm-ups, milliseconds — lower is better)*

| Algorithm | tslib (ms) | Python (ms) | R (ms) | vs Python | vs R |
|---|---:|---:|---:|---:|---:|
| Single ETS (α=0.3) | **0.008** | 0.371 | 4.119 | 46× faster | 515× faster |
| Double ETS (α=0.3, γ=0.1) | **0.020** | 1.008 | 8.651 | 50× faster | 433× faster |
| Holt-Winters (α=0.3, s=12) | **0.045** | 1.441 | 38.360 | 32× faster | 852× faster |
| SARIMA(1,1,1)(1,1,0,12) | **0.626** | 49.420 | 8.535 | 79× faster | 14× faster |
| VAR(1) | **0.064** | 0.321 | 4.734 | 5× faster | 74× faster |
| Local Level (MLE) | **0.668** | 3.821 | 14.024 | 6× faster | 21× faster |
| ARIMA(1,1,1) | **2.649** | 12.549 | 6.661 | 5× faster | 3× faster |
| Auto ARIMA (AIC, max p,d,q=3) | 92.74 | **90.563** | 23.881 | ~tie | 2.6× slower |
| Linear imputation (10 NAs) | **0.018** | 0.046 | 0.020 | 3× faster | ~tie |
| Box-Cox λ search | **0.365** | 3.935 | 0.777 | 11× faster | 2× faster |

*Measured on macOS Apple Silicon (Java 17 · Python 3.12 · R 4.6). CI (Linux x86) timings are higher; ratios are representative.*

### Forecast accuracy — holdout MAE *(lower is better, bold = best)*

| Algorithm | Dataset | tslib | Python | R |
|---|---|---:|---:|---:|
| ARIMA(1,1,1) | hotel (n=168, holdout=12) | **87.41** | 95.20 | 95.20 |
| SARIMA(1,1,1)(1,1,0,12) | airpassengers (n=144, holdout=12) | 18.94 | **16.64** | 16.79 |
| Holt-Winters (α=0.3, β=0.2, γ=0.1) | airpassengers (n=144, holdout=12) | **12.98** | 17.55 | 17.05 |
| Auto ARIMA (AIC) | hotel (n=168, holdout=12) | **88.84** | 95.75 | 95.75 |
| Local Level (MLE) | jj (n=84, holdout=8) | **3.48** | 3.64 | 3.64 |

tslib wins or ties 6 of 8 head-to-heads by MAE. Fixed-parameter models (ETS, VAR, SMA, EMA, WMA) produce numerically identical results across all three libraries.

ARIMA accuracy differences arise from estimation method: tslib uses CLS, Python defaults to innovations-MLE, and R uses CSS. All are valid estimators; CLS generalises better on the hotel series and worse on airpassengers (SARIMA).

### Performance regression CI

The [`benchmark.yml`](.github/workflows/benchmark.yml) workflow runs on every push or PR that touches `src/` or `benchmarks/`. It runs the full Java benchmark suite and then executes `PerformanceGuard`, which fails the build if:

- any **accuracy metric** (MAE, RMSE, MAPE, smoothing error, remainder variance, …) degrades by more than **2%** from the stored baseline
- any **execution time** exceeds **20×** the baseline (generous for CI machine variance)
- any **test statistic or model output** (ADF statistic, KPSS statistic, Ljung-Box Q, Box-Cox λ) drifts more than **5%** from the baseline

To accept an intentional improvement, update `benchmarks/results/baseline.csv`.

## REST API

tslib ships a Spring Boot REST API in the `tslib-api/` module that exposes the library over HTTP.

**Run locally:**
```bash
./gradlew :tslib-api:bootRun
```

**Build a fat JAR and run:**
```bash
./gradlew :tslib-api:bootJar
java -jar tslib-api/build/libs/<jar-name>.jar
```

The server starts on `http://localhost:8080`. All endpoints are `POST` with a JSON body, mounted under `/api/<group>/`.

**Explore interactively** with the Swagger UI:
```
http://localhost:8080/swagger-ui
```

**Example clients** are in `examples/api/`:
- `curl_examples.sh` — curl commands for every endpoint
- `python_client.py` — Python requests
- `r_client.R` — R httr calls

**Docker:**

The `tslib-api/` module includes a `Dockerfile` and `docker-compose.yml`. The image requires the fat JAR to be built first.

1. Build the JAR:
```bash
./gradlew :tslib-api:bootJar
```

2. Build and run the image:
```bash
docker build -t tslib-api tslib-api/
docker run -p 8080:8080 tslib-api
```

Or use Compose (runs from repo root):
```bash
docker compose -f tslib-api/docker-compose.yml up --build
```

The server is available at `http://localhost:8080` in both cases. Compose also wires a healthcheck against `/api-docs` with a 30s interval.

## Phase additions

### Phase 1

- `tslib.transform.Differencing`
- `tslib.model.arima.ARIMA`
- `tslib.model.ARIMA`

### Phase 2

- `tslib.model.arima.SARIMA`
- `tslib.model.arima.InformationCriteria`
- `tslib.model.arima.ArimaOrderSearch`
- `tslib.model.SARIMA`

### Phase 3

- `tslib.decomposition.STLDecomposition`
- `tslib.tests.KPSSTest`

### Phase 4

- `tslib.model.statespace.KalmanFilter`
- `tslib.model.statespace.LocalLevelModel`
- `tslib.model.LocalLevelModel`

### Phase 5

- `tslib.evaluation.ForecastMetrics`
- `tslib.evaluation.TrainTestSplit`
- `tslib.evaluation.RollingOriginBacktest`
- `tslib.evaluation.BacktestResult`
- `tslib.diagnostics.LjungBoxTest`

### Phase 6

- `tslib.evaluation.PredictionInterval`
- `tslib.evaluation.IntervalForecast`
- interval support for ARIMA, SARIMA, and local-level models

### Phase 7

- `tslib.selection.AutoArima`
- `tslib.selection.AutoETS`

### Phase 8

- `tslib.model.arima.ARIMAX`
- `tslib.model.ARIMAX`

### Phase 9

- `tslib.dataquality.MissingValueImputer`
- `tslib.dataquality.OutlierDetector`
- `tslib.dataquality.Winsorizer`

### Phase 10

- release hardening via Gradle bootstrap scripts and publishing config
- CI + tag-based release workflows
- model-selection and release-checklist docs
- `tslib.evaluation.ModelBenchmark`
- `tslib.evaluation.BenchmarkSummary`


### Phase 11

- release-candidate hardening via stronger edge-case and workflow tests
- README model chooser and testing guidance
- benchmark markdown/report helpers and benchmark-results template
- package-level Javadocs for primary public packages
- Java 17/21 CI matrix plus nightly verification workflow

## Release hardening

This snapshot adds release-oriented repo plumbing:

- lightweight `./gradlew` bootstrap scripts for Unix and Windows
- `maven-publish`, signing, and local build-repo publishing tasks
- CI and tag-driven release workflows under `.github/workflows/`
- `docs/MODEL_SELECTION_GUIDE.md`, `docs/RELEASE_CHECKLIST.md`, and `docs/BENCHMARKING.md`
- a small benchmark helper via `tslib.evaluation.ModelBenchmark` and `BenchmarkSummary`

## Publishing

For local validation:

```bash
./gradlew publishToMavenLocal
./gradlew publish
```

For tagged builds, see `docs/RELEASE_CHECKLIST.md` and `.github/workflows/release.yml`.

## Examples

See the `examples/` directory for runnable snippets:

- `ForecastExample.java`
- `TransformExample.java`
- `ArimaExample.java`
- `SarimaExample.java`
- `StlAndKpssExample.java`
- `LocalLevelExample.java`
- `BacktestExample.java`
- `IntervalsExample.java`
- `AutoSelectionExample.java`
- `ArimaxExample.java`
- `DataQualityExample.java`
- `BenchmarkComparisonExample.java`
- `GenerateBenchmarkReportExample.java`
- `HotelBenchmarkComparisonExample.java`

## Testing

Run the full verification flow locally with:

```bash
./gradlew clean test jacocoTestReport javadoc
```

The console is configured to print individual test results. HTML reports are generated under `build/reports/tests/test` and `build/reports/jacoco/test/html`.

For a release candidate pass, also review `docs/RELEASE_CHECKLIST.md`, `docs/TESTING_GUIDE.md`, and `docs/BENCHMARK_RESULTS.md`.

## Notes

The ARIMA/SARIMA, state-space, and evaluation additions are intentionally compact and dependency-light. They are designed for readable forecasting workflows inside this library rather than exhaustive econometric coverage.
