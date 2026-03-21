# Changelog

## Unreleased

- renamed the Gradle root project to `tslib`
- replaced `jcenter()` with `mavenCentral()`
- added `tslib.model.*` compatibility wrappers
- added `tslib.model.ARIMA` compatibility wrapper
- added `tslib.stats.Stats` compatibility wrapper
- added `tslib.transform.Differencing` with first-order, higher-order, seasonal, and inverse differencing helpers
- added `tslib.model.arima.ARIMA` with iterative conditional least squares fitting and forecasting
- refreshed README and example programs
- added regression tests for stats, transforms, moving averages, differencing, ARIMA, and compatibility aliases
