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

A `Dockerfile` and `docker-compose.yml` are also available in `tslib-api/` for containerized deployments.

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
