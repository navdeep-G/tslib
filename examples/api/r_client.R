# tslib REST API — R client examples
# Install: install.packages(c("httr2", "jsonlite"))
# Start the server: cd tslib-api && ../gradlew bootRun
# Swagger UI: http://localhost:8080/swagger-ui

library(httr2)
library(jsonlite)

BASE <- "http://localhost:8080/api"

# AirPassengers monthly data (24 observations)
DATA <- c(112, 118, 132, 129, 121, 135, 148, 148, 136, 119, 104, 118,
          115, 126, 141, 135, 125, 149, 170, 170, 158, 133, 114, 140)

tslib_post <- function(path, body) {
  request(paste0(BASE, path)) |>
    req_method("POST") |>
    req_headers("Content-Type" = "application/json") |>
    req_body_raw(toJSON(body, auto_unbox = TRUE, digits = NA)) |>
    req_perform() |>
    resp_body_string() |>
    fromJSON()
}

cat("\n=== ARIMA Forecast ===\n")
result <- tslib_post("/arima/forecast", list(
  data = DATA, p = 1L, d = 1L, q = 1L, steps = 6L
))
cat("Forecasts:", result$forecasts, "\n")
cat("AR coefficients:", result$arCoefficients, "\n")

cat("\n=== ARIMA with 95% Prediction Intervals ===\n")
result <- tslib_post("/arima/forecast-intervals", list(
  data = DATA, p = 1L, d = 1L, q = 1L, steps = 6L, confidenceLevel = 0.95
))
intervals <- result$intervals
for (i in seq_len(nrow(intervals))) {
  cat(sprintf("  Step %d: %.2f [%.2f, %.2f]\n",
              intervals$step[i], intervals$pointForecast[i],
              intervals$lower[i], intervals$upper[i]))
}

cat("\n=== SARIMA(1,1,1)(1,1,1)12 ===\n")
result <- tslib_post("/sarima/forecast", list(
  data = DATA, p = 1L, d = 1L, q = 1L,
  seasonalP = 1L, seasonalD = 1L, seasonalQ = 1L, seasonalPeriod = 12L,
  steps = 12L
))
cat("12-step forecast:", result$forecasts, "\n")

cat("\n=== AutoARIMA ===\n")
result <- tslib_post("/auto/arima/forecast", list(
  data = DATA, maxP = 3L, maxD = 2L, maxQ = 3L, steps = 6L, criterion = "AIC"
))
ord <- result$bestOrder
cat(sprintf("Best: ARIMA(%d,%d,%d)  score=%.4f\n", ord$p, ord$d, ord$q, ord$score))
cat("Forecasts:", result$forecasts, "\n")

cat("\n=== AutoETS ===\n")
result <- tslib_post("/auto/ets/forecast", list(
  data = DATA, steps = 6L, seasonalPeriod = 12L
))
cat(sprintf("Best type: %s, score=%.4f\n", result$bestType, result$bestScore))
cat("Forecasts:", result$forecasts, "\n")

cat("\n=== Holt-Winters (Triple ETS) ===\n")
result <- tslib_post("/ets/triple/forecast", list(
  data = DATA, alpha = 0.3, beta = 0.1, gamma = 0.1, period = 12L, steps = 12L
))
cat("Forecasts:", result$forecasts, "\n")

cat("\n=== VAR (two series) ===\n")
result <- tslib_post("/var/forecast", list(
  series = list(DATA, DATA), steps = 4L
))
cat(sprintf("Lag order: %d, AIC: %.4f\n", result$lagOrder, result$aic))

cat("\n=== Forecast Metrics ===\n")
result <- tslib_post("/evaluate/metrics", list(
  actual   = c(100, 110, 120, 115, 125),
  forecast = c(102, 108, 122, 113, 127),
  trainingSeries = c(90, 95, 100, 105),
  seasonalPeriod = 1L
))
cat(sprintf("MAE=%.4f  RMSE=%.4f  MAPE=%.4f  SMAPE=%.4f\n",
            result$mae, result$rmse, result$mape, result$smape))

cat("\n=== Rolling-Origin Backtest ===\n")
result <- tslib_post("/evaluate/backtest", list(
  data = DATA,
  modelSpec = list(type = "ARIMA", p = 1L, d = 1L, q = 1L),
  minTrainSize = 12L, horizon = 3L
))
cat(sprintf("Backtest MAE=%.4f  RMSE=%.4f\n", result$mae, result$rmse))

cat("\n=== ADF Stationarity Test ===\n")
result <- tslib_post("/tests/adf", list(data = DATA))
cat(sprintf("ADF stat=%.4f, p=%.4f, stationary=%s\n",
            result$statistic, result$pValue, result$stationary))

cat("\n=== KPSS Test ===\n")
result <- tslib_post("/tests/kpss", list(data = DATA, regressionType = "LEVEL"))
cat(sprintf("KPSS stat=%.4f, stationary@5%%=%s\n",
            result$statistic, result$stationaryAtFivePercent))

cat("\n=== STL Decomposition ===\n")
result <- tslib_post("/decompose/stl", list(data = DATA, period = 12L))
cat("Trend (first 6):", head(result$trend, 6), "\n")
cat("Seasonal (first 12):", head(result$seasonal, 12), "\n")

cat("\n=== Box-Cox Transform (auto lambda) ===\n")
result <- tslib_post("/transform/boxcox", list(data = DATA))
cat(sprintf("Optimal lambda: %.4f\n", result$lambda))

cat("\n=== First Differencing ===\n")
result <- tslib_post("/transform/difference", list(data = DATA, order = 1L))
cat("Differenced (first 6):", head(result$forecasts, 6), "\n")

cat("\n=== Comprehensive Analysis ===\n")
result <- tslib_post("/analyze", list(data = DATA, k = 1L, n = 6L, windowSize = 3L))
cat(sprintf("Mean=%.2f, SD=%.2f, ADF=%.4f, Stationary=%s\n",
            result$average, result$standardDeviation,
            result$adfStatistic, result$stationary))
cat("ACF (6 lags):", round(result$acf, 3), "\n")

cat("\n=== Moving Averages ===\n")
for (ma_type in c("SIMPLE", "EMA", "WEIGHTED", "CUMULATIVE")) {
  result <- tslib_post("/moving-average", list(data = DATA, period = 3L, type = ma_type))
  cat(sprintf("  %s (first 6): %s\n", ma_type,
              paste(round(head(result$forecasts, 6), 2), collapse = " ")))
}
