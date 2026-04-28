# Roadmap

This document describes what has been built, what is actively planned, and what is being considered for the longer term. Items move from "Planned" to "In Progress" when a branch opens and to "Done" when merged to `master`.

---

## Done

### Library core (Phases 1–11)
- **Transforms** — log, sqrt, cbrt, root, Box-Cox (with optimal lambda search), differencing, seasonal differencing, inverse transforms
- **Statistics** — mean, variance, std dev, min/max, autocovariance, ACF, PACF
- **Stationarity tests** — Augmented Dickey-Fuller, KPSS
- **Decomposition** — STL (with configurable trend/seasonal windows and robustness iterations)
- **Moving averages** — simple, exponential, weighted, cumulative
- **ARIMA / SARIMA** — OLS-based fitting, order search (AIC / BIC / AICc), prediction intervals
- **ARIMAX** — ARIMA with contemporaneous exogenous regressors
- **VAR** — Vector Autoregression with optimal lag selection
- **Exponential smoothing** — single (SES), double (Holt), triple (Holt-Winters)
- **State space** — Local Level model (Kalman MLE via Brent), full Kalman filter
- **Auto-selection** — AutoARIMA, AutoETS
- **Evaluation** — train/test split, rolling-origin backtest, MAE/RMSE/MAPE/sMAPE/MASE/MeanError, Ljung-Box diagnostics, model benchmark comparison
- **Data quality** — missing-value imputation (four strategies), z-score / IQR outlier detection, quantile winsorization
- **Model serialization** — all fitted model classes implement `java.io.Serializable`; `tslib/util/ModelSerializer` provides `save` / `load` helpers for file persistence
- **`TimeSeriesModel` interface** — common `fit(data)` → `forecast(steps)` → `forecastWithIntervals(steps, level)` contract implemented by every univariate model (ARIMA, SARIMA, LocalLevelModel, AutoARIMA, AutoETS, SES, Holt, Holt-Winters)
- **In-memory `Collect` constructors** — two new constructors accept `List<Double>` or `double[]` directly, bypassing file I/O
- **Release tooling** — Gradle publishing, signing, CI/CD workflows, JaCoCo coverage enforcement (≥70%), Checkstyle, SpotBugs

### REST API (`tslib-api`, Phase 12)
- Spring Boot 3.2 subproject exposing every user-callable library class as a JSON endpoint
- 34 POST endpoints across 12 controller groups
- Input validation (`@Valid`) and global exception handler with typed error responses
- Swagger UI / OpenAPI 3 spec at `/swagger-ui` and `/api-docs`
- Dockerfile and Docker Compose for container deployment
- Working client examples for curl, Python (`requests`), and R (`httr2`)

---

## Planned (near-term)

### REST API hardening
- [ ] Optional API-key authentication (header-based, configurable via `application.properties`)
- [ ] Request size and rate limiting to prevent abuse on public deployments
- [ ] Health and readiness endpoints (`/actuator/health`) for load-balancer integration
- [ ] Publish a Docker image to Docker Hub under `navdeep-g/tslib-api`
- [ ] OpenAPI spec export as a checked-in file so clients can generate typed SDKs without a running server

### Client libraries
- [ ] **Python package** (`tslib-py`) — thin `requests`-based wrapper with typed dataclasses for every request/response, installable via `pip`
- [ ] **R package** (`tslibR`) — thin `httr2`-based wrapper matching the Python surface, installable via `devtools::install_github`
- [ ] Auto-generate both clients from the OpenAPI spec using `openapi-generator`

### New library features
- [ ] **TBATS** — trigonometric seasonality + Box-Cox + ARMA errors + trend + seasonal, for series with complex or multiple seasonal periods
- [ ] **MSTL** — Multiple Seasonal-Trend decomposition (extends STL to handle more than one seasonal period, e.g. hourly data with daily + weekly patterns)
- [ ] **Exponential smoothing with Box-Cox** — pre/post-transform wrapper around the ETS family to handle heteroscedastic series
- [ ] **Probabilistic metrics** — CRPS (Continuous Ranked Probability Score) and Winkler score for evaluating prediction intervals, not just point forecasts
- [ ] **Seasonal naive baseline** — `SeasonalNaive` model implementing `TimeSeriesModel` as a fast, hard-to-beat benchmark for MASE denominators and comparisons
- [ ] **Cross-validation helper** — time-series cross-validation with configurable expanding vs. sliding windows, parallel execution via `ForkJoinPool`

### Tooling
- [ ] Benchmark result persistence — write `ModelBenchmark` output to `docs/BENCHMARK_RESULTS.md` as part of CI so regressions are visible in PRs
- [ ] Java 21 compatibility verification added to CI matrix (Phase 11 scoped Java 17/21 CI but this completes the 21 branch)
- [ ] Publish Javadoc site to GitHub Pages on every release tag

---

## Planned (medium-term)

### Hierarchical forecasting
- [ ] `HierarchicalReconciler` — bottom-up, top-down, and OLS-based MinT reconciliation for grouped time series (e.g. regional totals that must sum to a national total)
- [ ] Coherence checking utilities to validate hierarchical constraints before and after reconciliation

### Anomaly detection
- [ ] `AnomalyDetector` — seasonal-hybrid ESD (S-H-ESD) and STL-residual threshold methods
- [ ] REST endpoint `POST /api/anomalies/detect` returning flagged indices and severity scores

### Forecasting with external data
- [ ] **Transfer function / dynamic regression** — extend ARIMAX to support lagged exogenous effects (distributed lag model)
- [ ] **VARX** — extend VARModel to accept exogenous regressors

### Streaming / online forecasting
- [ ] `OnlineARIMA` — incremental coefficient update as each observation arrives, without full refitting
- [ ] `OnlineETS` — state update without full refit, suitable for real-time inference behind the REST API

---

## Under consideration (longer-term)

These are not committed. They require broader design discussion before any work begins.

- **Neural / ML-based forecasters** — N-BEATS, N-HiTS, or a simple MLP baseline implementing `TimeSeriesModel`, keeping the dependency-light library ethos by shipping model weights separately
- **Conformal prediction intervals** — distribution-free coverage guarantees via split conformal or jackknife+ methods, complementing the existing normal-approximation intervals
- **Multivariate anomaly detection** — extend the univariate AnomalyDetector to leverage VAR residuals
- **Spark / batch integration** — a thin connector so `RollingOriginBacktest` can distribute folds across a Spark cluster for large-scale evaluations
- **gRPC transport layer** — an alternative to the REST API for low-latency, high-throughput inference use cases

---

## Version targets

| Version | Contents |
|---|---|
| `0.1.x` | Current library (Phases 1–11) |
| `1.0.0` | REST API stable release + Python + R client packages |
| `1.1.0` | TBATS, MSTL, probabilistic metrics, seasonal naive |
| `1.2.0` | Hierarchical reconciliation, anomaly detection |
| `2.0.0` | Online forecasting, VARX, conformal intervals |
