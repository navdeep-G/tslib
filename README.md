# tslib

A small Java library for time-series analysis and forecasting.

## What is included

- exponential smoothing: single, double, and triple
- moving averages: simple, cumulative, exponential, and weighted
- transformations: log, roots, Box-Cox, and differencing
- statistics: mean, variance, autocovariance, ACF, and PACF
- stationarity testing with Augmented Dickey-Fuller
- ARIMA forecasting with manual `(p, d, q)` order selection

## Build

The project targets Java 17 and uses Gradle.

```bash
gradle test
```

This snapshot does not include the generated `gradle-wrapper.jar`, so `gradle` is the most reliable entry point unless you regenerate the wrapper locally.

## Package map

Primary implementation packages:

- `tslib.collect`
- `tslib.model.arima`
- `tslib.model.expsmoothing`
- `tslib.movingaverage`
- `tslib.tests`
- `tslib.transform`
- `tslib.util`

Compatibility aliases added in this patch:

- `tslib.model.*` forwards to `tslib.model.expsmoothing.*`
- `tslib.model.ARIMA` forwards to `tslib.model.arima.ARIMA`
- `tslib.stats.Stats` forwards to `tslib.util.Stats`

These aliases make the public API line up with the README examples without breaking existing imports.

## Quick start

```java
import java.util.List;
import tslib.model.ARIMA;
import tslib.model.ExponentialSmoothing;
import tslib.model.TripleExpSmoothing;
import tslib.movingaverage.MovingAverage;
import tslib.movingaverage.SimpleMovingAverage;
import tslib.transform.Differencing;
import tslib.transform.Transform;
import tslib.util.Util;

List<Double> data = Util.readFile("data/hotel.txt");

ExponentialSmoothing smoothing = new TripleExpSmoothing(0.5, 0.3, 0.2, 12, false);
List<Double> smoothedForecast = smoothing.forecast(data, 5);

MovingAverage sma = new SimpleMovingAverage(3);
List<Double> smoothed = sma.compute(data);

List<Double> firstDifference = Differencing.difference(data);
ARIMA arima = new ARIMA(1, 1, 0).fit(data);
List<Double> arimaForecast = arima.forecast(5);

double lambda = Transform.boxCoxLambdaSearch(data);
List<Double> transformed = Transform.boxCox(data, lambda);
```

## Phase 1 additions

This patch adds the first model-based forecasting phase beyond smoothing:

- `tslib.transform.Differencing`
  - first-order differencing
  - higher-order differencing
  - seasonal differencing
  - inverse differencing for restoring forecasts to the original scale
- `tslib.model.arima.ARIMA`
  - manual `ARIMA(p, d, q)` order selection
  - iterative conditional least squares fitting
  - in-sample fitted values and future forecasts
  - coefficient and residual accessors for diagnostics

## Examples

See the `examples/` directory for runnable snippets:

- `ForecastExample.java`
- `TransformExample.java`
- `ArimaExample.java`

## Release notes

This snapshot includes the following repo-level improvements:

- fixes the Gradle project name from `expsmoothing` to `tslib`
- removes deprecated `jcenter()` in favor of `mavenCentral()`
- adds API compatibility aliases for the package names shown in the docs
- adds Phase 1 forecasting features: differencing and ARIMA
- refreshes CI to use current Gradle and Java setup
- adds focused regression tests and example programs
- adds a changelog stub for future releases
