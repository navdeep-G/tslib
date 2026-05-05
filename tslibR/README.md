# tslibR

An R client for the [tslib](https://github.com/navdeep-G/tslib) time-series REST API. Matches the Python `tslib` client surface and is installable directly from GitHub.

## Installation

```r
# install.packages("devtools")
devtools::install_github("navdeep-G/tslib", subdir = "tslibR")
```

## Quick start

```r
library(tslibR)

# Connect to a running tslib API server
client <- tslib_client("http://localhost:8080")

# Optional API key authentication
client <- tslib_client("http://localhost:8080", api_key = "your-key")

data <- c(112, 118, 132, 129, 121, 135, 148, 148, 136, 119, 104, 118,
          115, 126, 141, 135, 125, 149, 170, 170, 158, 133, 114, 140)

# ARIMA forecast
fit <- tslib_arima_forecast(client, data, p = 1, d = 1, q = 0, steps = 6)
fit$forecasts

# AutoARIMA
auto <- tslib_auto_arima(client, data, steps = 6)
auto$bestOrder   # selected (p, d, q)
auto$forecasts

# STL decomposition
stl <- tslib_stl(client, data, period = 12)
stl$trend
stl$seasonal
stl$remainder

# Accuracy metrics
tslib_metrics(client, actual = tail(data, 6), forecast = unlist(fit$forecasts))
```

## Function reference

| Function | Endpoint |
|---|---|
| `tslib_analyze()` | `POST /api/analyze` |
| `tslib_arima_forecast()` | `POST /api/arima/forecast[‑intervals]` |
| `tslib_sarima_forecast()` | `POST /api/sarima/forecast[‑intervals]` |
| `tslib_arimax_forecast()` | `POST /api/arimax/forecast` |
| `tslib_arima_order_search()` | `POST /api/arima/order-search` |
| `tslib_var_forecast()` | `POST /api/var/forecast` |
| `tslib_ses()` | `POST /api/ets/single/forecast` |
| `tslib_holt()` | `POST /api/ets/double/forecast` |
| `tslib_holt_winters()` | `POST /api/ets/triple/forecast` |
| `tslib_local_level_forecast()` | `POST /api/statespace/local-level/forecast` |
| `tslib_local_level_filter()` | `POST /api/statespace/local-level/filter` |
| `tslib_kalman_filter()` | `POST /api/statespace/kalman/filter` |
| `tslib_auto_arima()` | `POST /api/auto/arima/forecast` |
| `tslib_auto_ets()` | `POST /api/auto/ets/forecast` |
| `tslib_stl()` | `POST /api/decompose/stl` |
| `tslib_transform_log()` | `POST /api/transform/log` |
| `tslib_transform_sqrt()` | `POST /api/transform/sqrt` |
| `tslib_transform_cbrt()` | `POST /api/transform/cbrt` |
| `tslib_transform_root()` | `POST /api/transform/root` |
| `tslib_transform_boxcox()` | `POST /api/transform/boxcox` |
| `tslib_transform_boxcox_inverse()` | `POST /api/transform/boxcox/inverse` |
| `tslib_transform_difference()` | `POST /api/transform/difference` |
| `tslib_transform_seasonal_difference()` | `POST /api/transform/seasonal-difference` |
| `tslib_transform_difference_inverse()` | `POST /api/transform/difference/inverse` |
| `tslib_metrics()` | `POST /api/evaluate/metrics` |
| `tslib_backtest()` | `POST /api/evaluate/backtest` |
| `tslib_train_test_split()` | `POST /api/evaluate/train-test-split` |
| `tslib_benchmark()` | `POST /api/evaluate/benchmark` |
| `tslib_impute()` | `POST /api/dataquality/impute` |
| `tslib_outliers()` | `POST /api/dataquality/outliers` |
| `tslib_winsorize()` | `POST /api/dataquality/winsorize` |
| `tslib_adf()` | `POST /api/tests/adf` |
| `tslib_kpss()` | `POST /api/tests/kpss` |
| `tslib_moving_average()` | `POST /api/moving-average` |
| `tslib_ljung_box()` | `POST /api/diagnostics/ljung-box` |

## Running the API server

```bash
# Docker (quickest)
docker run -p 8080:8080 navdeep-g/tslib-api:latest

# Or from source
./gradlew :tslib-api:bootRun
```

## Running tests

```r
# install.packages(c("testthat", "webmockr", "jsonlite"))
devtools::test("tslibR")
```

## License

MIT
