# Changelog

## Unreleased

### Phase 11
- add release-candidate hardening with broader regression tests
- add benchmark markdown export helper and benchmark-results template
- add README model chooser and testing guide
- add package-level Javadocs for core public packages
- add Java 17/21 CI matrix and nightly verification workflow


## Unreleased

### Repo maintenance
- align Gradle metadata with `tslib`
- replace `jcenter()` with `mavenCentral()`
- refresh README, examples, and CI
- add API compatibility aliases used in the docs

### Phase 1
- add differencing and inverse differencing utilities
- add manual ARIMA forecasting support
- add ARIMA examples and regression tests

### Phase 2
- add seasonal SARIMA forecasting support
- add AIC, BIC, and AICc helpers
- add ARIMA/SARIMA order-search utilities

### Phase 3
- add STL-style decomposition
- add KPSS stationarity testing

### Phase 4
- add one-dimensional Kalman filtering
- add local-level state-space forecasting model
- add examples and tests for state-space workflows

### Phase 5
- add forecast metrics including MAE, RMSE, MAPE, sMAPE, and MASE
- add train/test split helpers and rolling-origin backtesting
- add Ljung-Box residual diagnostics

### Phase 6
- add normal-approximation prediction intervals
- add interval support for ARIMA, SARIMA, and local-level models

### Phase 7
- add AutoArima wrapper around ARIMA/SARIMA order search
- add AutoETS model selection for exponential smoothing families

### Phase 8
- add ARIMAX with contemporaneous exogenous regressors
- add compatibility alias `tslib.model.ARIMAX`

### Phase 9
- add missing-value imputation helpers
- add z-score and IQR outlier detection
- add quantile winsorization utilities
