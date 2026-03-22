# tslib

A small Java library for time-series analysis and forecasting.

## What is included

- exponential smoothing: single, double, and triple
- moving averages: simple, cumulative, exponential, and weighted
- transformations: log, roots, Box-Cox, differencing, and seasonal differencing
- statistics: mean, variance, autocovariance, ACF, and PACF
- stationarity testing with Augmented Dickey-Fuller and KPSS
- model-based forecasting with ARIMA and SARIMA
- decomposition with STL-style trend/seasonal/remainder extraction
- compact state-space forecasting with a local-level Kalman model
- order-selection helpers with AIC, BIC, and AICc

## Build

The project targets Java 17 and uses Gradle.

```bash
gradle test
```

This snapshot does not include the generated `gradle-wrapper.jar`, so `gradle` is the most reliable entry point unless you regenerate the wrapper locally.

## Package map

Primary implementation packages:

- `tslib.collect`
- `tslib.decomposition`
- `tslib.model.arima`
- `tslib.model.expsmoothing`
- `tslib.model.statespace`
- `tslib.movingaverage`
- `tslib.tests`
- `tslib.transform`
- `tslib.util`

Compatibility aliases added in this patch series:

- `tslib.model.*` forwards to `tslib.model.expsmoothing.*`
- `tslib.model.ARIMA` forwards to `tslib.model.arima.ARIMA`
- `tslib.model.SARIMA` forwards to `tslib.model.arima.SARIMA`
- `tslib.model.LocalLevelModel` forwards to `tslib.model.statespace.LocalLevelModel`
- `tslib.stats.Stats` forwards to `tslib.util.Stats`

## Quick start

```java
import java.util.List;
import tslib.decomposition.STLDecomposition;
import tslib.model.ARIMA;
import tslib.model.LocalLevelModel;
import tslib.model.SARIMA;
import tslib.tests.KPSSTest;
import tslib.transform.Differencing;

List<Double> data = List.of(
        10.0, 20.0, 30.0, 40.0,
        11.0, 21.0, 31.0, 41.0,
        12.0, 22.0, 32.0, 42.0);

ARIMA arima = new ARIMA(1, 1, 0).fit(data);
List<Double> arimaForecast = arima.forecast(5);

SARIMA sarima = new SARIMA(0, 0, 0, 0, 1, 0, 4).fit(data);
List<Double> sarimaForecast = sarima.forecast(4);

List<Double> firstDifference = Differencing.difference(data);
STLDecomposition.Result stl = new STLDecomposition(4).decompose(data);
KPSSTest kpss = new KPSSTest(stl.getRemainder());

LocalLevelModel localLevel = new LocalLevelModel().fit(data);
List<Double> stateSpaceForecast = localLevel.forecast(3);
```

## Phase additions

### Phase 1

- `tslib.transform.Differencing`
- `tslib.model.arima.ARIMA`
- `tslib.model.ARIMA`

### Phase 2

- `tslib.model.arima.SARIMA`
  - seasonal ARIMA forecasting with seasonal differencing
  - seasonal and non-seasonal AR/MA terms
- `tslib.model.arima.InformationCriteria`
  - AIC, BIC, and AICc helpers
- `tslib.model.arima.ArimaOrderSearch`
  - simple grid-search helpers for ARIMA and SARIMA

### Phase 3

- `tslib.decomposition.STLDecomposition`
  - trend, seasonal, and remainder components
- `tslib.tests.KPSSTest`
  - level and trend stationarity options

### Phase 4

- `tslib.model.statespace.KalmanFilter`
  - one-dimensional local-level Kalman filter
- `tslib.model.statespace.LocalLevelModel`
  - basic variance search, filtering, and forecasting helpers
- `tslib.model.LocalLevelModel`
  - compatibility alias

## Examples

See the `examples/` directory for runnable snippets:

- `ForecastExample.java`
- `TransformExample.java`
- `ArimaExample.java`
- `SarimaExample.java`
- `StlAndKpssExample.java`
- `LocalLevelExample.java`

## Notes

The ARIMA/SARIMA and state-space additions are intentionally compact and dependency-light. They are designed for readable forecasting workflows inside this library rather than exhaustive econometric coverage.
