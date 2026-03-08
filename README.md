# tslib

A small Java library for time-series analysis and forecasting.

## What is included

- exponential smoothing: single, double, and triple
- moving averages: simple, cumulative, exponential, and weighted
- transformations: log, roots, and Box-Cox
- statistics: mean, variance, autocovariance, ACF, and PACF
- stationarity testing with Augmented Dickey-Fuller

## Build

The project targets Java 17 and uses Gradle.

```bash
gradle test
```

This snapshot does not include the generated `gradle-wrapper.jar`, so `gradle` is the most reliable entry point unless you regenerate the wrapper locally.

## Package map

Primary implementation packages:

- `tslib.collect`
- `tslib.model.expsmoothing`
- `tslib.movingaverage`
- `tslib.tests`
- `tslib.transform`
- `tslib.util`

Compatibility aliases added in this patch:

- `tslib.model.*` forwards to `tslib.model.expsmoothing.*`
- `tslib.stats.Stats` forwards to `tslib.util.Stats`

These aliases make the public API line up with the README examples without breaking existing imports.

## Quick start

```java
import java.util.List;
import tslib.model.ExponentialSmoothing;
import tslib.model.TripleExpSmoothing;
import tslib.movingaverage.MovingAverage;
import tslib.movingaverage.SimpleMovingAverage;
import tslib.transform.Transform;
import tslib.util.Util;

List<Double> data = Util.readFile("data/hotel.txt");

ExponentialSmoothing model = new TripleExpSmoothing(0.5, 0.3, 0.2, 12, false);
List<Double> forecast = model.forecast(data, 5);

MovingAverage sma = new SimpleMovingAverage(3);
List<Double> smoothed = sma.compute(data);

double lambda = Transform.boxCoxLambdaSearch(data);
List<Double> transformed = Transform.boxCox(data, lambda);
```

## Examples

See the `examples/` directory for runnable snippets.

## Release notes

This patch makes the following repo-level improvements:

- fixes the Gradle project name from `expsmoothing` to `tslib`
- removes deprecated `jcenter()` in favor of `mavenCentral()`
- adds API compatibility aliases for the package names shown in the docs
- refreshes CI to use current Gradle and Java setup
- adds focused regression tests and example programs
- adds a changelog stub for future releases
