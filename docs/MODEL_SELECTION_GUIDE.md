# Model selection guide

## Quick heuristics

Use **single/double/triple exponential smoothing** when you want lightweight forecasting and your data pattern is mostly level, trend, or trend+seasonality.

Use **ARIMA** when the series becomes stable after differencing and you want autoregressive / moving-average dynamics.

Use **SARIMA** when the series has a repeating seasonal pattern and you want an explicit seasonal Box-Jenkins model.

Use **ARIMAX** when you have one or more external regressors that should explain part of the signal.

Use **LocalLevelModel** when you want a compact state-space baseline with smooth latent level estimates and simple uncertainty estimates.

Use **STL + another forecaster** when you want decomposition first, especially to inspect trend/seasonality/residual behavior.

## Suggested workflow

1. Clean the series with the data-quality helpers.
2. Check transformations and differencing.
3. Inspect stationarity with ADF and KPSS.
4. Compare at least two model families with rolling-origin backtests.
5. Prefer the simplest model that performs competitively.
6. Add prediction intervals before shipping forecasts downstream.

## Recommended comparisons

- level/trend only: DoubleExpSmoothing vs ARIMA vs LocalLevelModel
- clear seasonality: TripleExpSmoothing vs SARIMA
- external drivers: ARIMAX vs ARIMA
- noisy operational series: LocalLevelModel vs ARIMA

## Metrics to monitor

- MAE for absolute error in data units
- RMSE when large misses matter more
- sMAPE for percentage-style comparison across scales
- MASE for scale-free comparison against naive baselines

## Diagnostics

- Ljung-Box p-values near zero suggest residual autocorrelation remains
- ADF rejects a unit root more often on stationary series
- KPSS tends to flag residual trend/non-stationarity from the opposite direction
