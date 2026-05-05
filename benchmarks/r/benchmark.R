#!/usr/bin/env Rscript
# R benchmark: compares forecast / tseries / vars / TTR / KFAS against tslib.
#
# Run from the project root:
#   Rscript benchmarks/r/benchmark.R
#
# Output: benchmarks/results/r_results.csv
# Format: library,algorithm,dataset,metric,value

suppressPackageStartupMessages({
  library(forecast)
  library(tseries)
  library(vars)
  library(TTR)
  library(KFAS)
})

# ── locate project root ────────────────────────────────────────────────────────

args <- commandArgs(trailingOnly = FALSE)
script_file <- sub("--file=", "", args[grep("--file=", args)])
if (length(script_file) == 0) {
  root <- getwd()
} else {
  root <- normalizePath(file.path(dirname(script_file), "..", ".."))
}

# ── constants ─────────────────────────────────────────────────────────────────

WARMUP_RUNS <- 2L
TIMED_RUNS  <- 5L

# ── data loading ──────────────────────────────────────────────────────────────

load_txt <- function(path) {
  as.numeric(readLines(path))
}

load_csv <- function(path) {
  df <- read.csv(path)
  as.numeric(df[[1]])
}

# ── timing helper ─────────────────────────────────────────────────────────────

median_ms <- function(fn, ...) {
  for (i in seq_len(WARMUP_RUNS)) fn(...)
  times <- numeric(TIMED_RUNS)
  result <- NULL
  for (i in seq_len(TIMED_RUNS)) {
    t0 <- as.numeric(Sys.time()) * 1000  # ms, sub-millisecond precision
    result <- fn(...)
    times[i] <- as.numeric(Sys.time()) * 1000 - t0
  }
  list(result = result, ms = median(times))
}

# ── metric helpers ────────────────────────────────────────────────────────────

calc_mae  <- function(a, p) mean(abs(a - p))
calc_rmse <- function(a, p) sqrt(mean((a - p)^2))
calc_mape <- function(a, p) {
  mask <- a != 0
  mean(abs((a[mask] - p[mask]) / a[mask])) * 100
}

# ── CSV writer ────────────────────────────────────────────────────────────────

results <- list()

add_rows <- function(lib, algo, dataset, metrics) {
  for (nm in names(metrics)) {
    results[[length(results) + 1]] <<- c(lib, algo, dataset, nm, metrics[[nm]])
  }
}

write_results <- function(path) {
  dir.create(dirname(path), showWarnings = FALSE, recursive = TRUE)
  df <- as.data.frame(do.call(rbind, results), stringsAsFactors = FALSE)
  colnames(df) <- c("library", "algorithm", "dataset", "metric", "value")
  write.csv(df, path, row.names = FALSE, quote = FALSE)
  cat("\nResults written to:", path, "\n")
}

fmt <- function(x) sprintf("%.6f", x)

# ── forecasting helper ────────────────────────────────────────────────────────

forecast_metrics <- function(actual, pred) {
  list(
    MAE     = fmt(calc_mae(actual, pred)),
    RMSE    = fmt(calc_rmse(actual, pred)),
    MAPE    = fmt(calc_mape(actual, pred))
  )
}

# ── benchmarks ────────────────────────────────────────────────────────────────

cat("=== R Benchmarks ===\n\n")

# 1. ARIMA(1,1,1) ──────────────────────────────────────────────────────────────
bench_arima <- function(data, dataset) {
  cat(sprintf("  ARIMA(1,1,1) [%s] ... ", dataset))
  holdout <- 12L
  train <- data[seq_len(length(data) - holdout)]
  test  <- tail(data, holdout)
  ts_train <- ts(train)

  r <- median_ms(function() {
    fit <- Arima(ts_train, order = c(1, 1, 1), method = "CSS")
    as.numeric(forecast(fit, h = holdout)$mean)
  })
  fc <- r$result; ms <- r$ms
  m <- forecast_metrics(test, fc)
  cat(sprintf("MAE=%.4f  RMSE=%.4f  MAPE=%.2f%%  exec=%.3f ms\n",
              as.numeric(m$MAE), as.numeric(m$RMSE), as.numeric(m$MAPE), ms))
  add_rows("r", "ARIMA_1_1_1", dataset, c(m, exec_ms = fmt(ms)))
}

# 2. SARIMA(1,1,1)(1,1,0,12) ──────────────────────────────────────────────────
bench_sarima <- function(data, dataset) {
  cat(sprintf("  SARIMA(1,1,1)(1,1,0,12) [%s] ... ", dataset))
  holdout <- 12L
  train <- ts(data[seq_len(length(data) - holdout)], frequency = 12)
  test  <- tail(data, holdout)

  r <- median_ms(function() {
    fit <- Arima(train, order = c(1, 1, 1),
                 seasonal = list(order = c(1, 1, 0), period = 12),
                 method = "CSS")
    as.numeric(forecast(fit, h = holdout)$mean)
  })
  fc <- r$result; ms <- r$ms
  m <- forecast_metrics(test, fc)
  cat(sprintf("MAE=%.4f  RMSE=%.4f  MAPE=%.2f%%  exec=%.3f ms\n",
              as.numeric(m$MAE), as.numeric(m$RMSE), as.numeric(m$MAPE), ms))
  add_rows("r", "SARIMA_1_1_1_1_1_0_12", dataset, c(m, exec_ms = fmt(ms)))
}

# 3. Single ETS (α=0.3) ────────────────────────────────────────────────────────
bench_single_ets <- function(data, dataset) {
  cat(sprintf("  SingleETS(a=0.3) [%s] ... ", dataset))
  holdout <- 8L
  train <- data[seq_len(length(data) - holdout)]
  test  <- tail(data, holdout)

  r <- median_ms(function() {
    fit <- ses(train, alpha = 0.3, h = holdout, initial = "simple")
    as.numeric(fit$mean)
  })
  fc <- r$result; ms <- r$ms
  m <- forecast_metrics(test, fc)
  cat(sprintf("MAE=%.4f  RMSE=%.4f  MAPE=%.2f%%  exec=%.3f ms\n",
              as.numeric(m$MAE), as.numeric(m$RMSE), as.numeric(m$MAPE), ms))
  add_rows("r", "SingleETS_a0.3", dataset, c(m, exec_ms = fmt(ms)))
}

# 4. Double ETS / Holt (α=0.3, β=0.1) ────────────────────────────────────────
bench_double_ets <- function(data, dataset) {
  cat(sprintf("  DoubleETS(a=0.3,b=0.1) [%s] ... ", dataset))
  holdout <- 12L
  train <- data[seq_len(length(data) - holdout)]
  test  <- tail(data, holdout)

  # R holt: alpha=level, beta=trend (standard naming)
  # tslib DoubleExpSmoothing: alpha=level, gamma=trend
  r <- median_ms(function() {
    fit <- holt(train, alpha = 0.3, beta = 0.1, h = holdout, initial = "simple")
    as.numeric(fit$mean)
  })
  fc <- r$result; ms <- r$ms
  m <- forecast_metrics(test, fc)
  cat(sprintf("MAE=%.4f  RMSE=%.4f  MAPE=%.2f%%  exec=%.3f ms\n",
              as.numeric(m$MAE), as.numeric(m$RMSE), as.numeric(m$MAPE), ms))
  add_rows("r", "DoubleETS_a0.3_g0.1", dataset, c(m, exec_ms = fmt(ms)))
}

# 5. Triple ETS / Holt-Winters (α=0.3, β=0.1, γ=0.2, s=12) ──────────────────
bench_triple_ets <- function(data, dataset) {
  cat(sprintf("  HoltWinters(a=0.3,b=0.1,g=0.2,s=12) [%s] ... ", dataset))
  holdout <- 12L
  train <- ts(data[seq_len(length(data) - holdout)], frequency = 12)
  test  <- tail(data, holdout)

  # R hw: alpha=level, beta=trend, gamma=seasonal
  # tslib: alpha=level, beta=seasonal, gamma=trend → swap β↔γ for R
  r <- median_ms(function() {
    fit <- hw(train, alpha = 0.3, beta = 0.1, gamma = 0.2,
              seasonal = "multiplicative", h = holdout, initial = "simple")
    as.numeric(fit$mean)
  })
  fc <- r$result; ms <- r$ms
  m <- forecast_metrics(test, fc)
  cat(sprintf("MAE=%.4f  RMSE=%.4f  MAPE=%.2f%%  exec=%.3f ms\n",
              as.numeric(m$MAE), as.numeric(m$RMSE), as.numeric(m$MAPE), ms))
  add_rows("r", "HoltWinters_a0.3_b0.2_g0.1_s12", dataset, c(m, exec_ms = fmt(ms)))
}

# 6. SMA (window=7) ────────────────────────────────────────────────────────────
bench_sma <- function(data, dataset) {
  cat(sprintf("  SMA(window=7) [%s] ... ", dataset))

  r <- median_ms(function() SMA(data, n = 7))
  smoothed <- r$result; ms <- r$ms
  valid <- smoothed[!is.na(smoothed)]
  actual <- data[seq(7, length(data))]
  m <- calc_mae(actual, valid)
  rm <- calc_rmse(actual, valid)
  cat(sprintf("smoothing_mae=%.4f  rmse=%.4f  exec=%.3f ms\n", m, rm, ms))
  add_rows("r", "SMA_window7", dataset, list(
    smoothing_mae  = fmt(m),
    smoothing_rmse = fmt(rm),
    exec_ms        = fmt(ms)
  ))
}

# 7. EMA (α=0.2) ───────────────────────────────────────────────────────────────
bench_ema <- function(data, dataset) {
  cat(sprintf("  EMA(a=0.2) [%s] ... ", dataset))

  # TTR::EMA uses ratio = 2/(n+1), so α=0.2 → n=9
  # For a direct α-based EMA matching tslib, implement manually
  r <- median_ms(function() {
    out <- numeric(length(data))
    out[1] <- data[1]
    for (i in seq(2, length(data))) {
      out[i] <- 0.2 * data[i] + 0.8 * out[i - 1]
    }
    out
  })
  smoothed <- r$result; ms <- r$ms
  m  <- calc_mae(data, smoothed)
  rm <- calc_rmse(data, smoothed)
  cat(sprintf("smoothing_mae=%.4f  rmse=%.4f  exec=%.3f ms\n", m, rm, ms))
  add_rows("r", "EMA_a0.2", dataset, list(
    smoothing_mae  = fmt(m),
    smoothing_rmse = fmt(rm),
    exec_ms        = fmt(ms)
  ))
}

# 8. STL decomposition (period=12) ─────────────────────────────────────────────
bench_stl <- function(data, dataset) {
  cat(sprintf("  STL(period=12) [%s] ... ", dataset))
  ts_data <- ts(data, frequency = 12)

  r <- median_ms(function() stl(ts_data, s.window = 7))
  result <- r$result; ms <- r$ms
  remainder <- result$time.series[, "remainder"]
  seasonal  <- result$time.series[, "seasonal"]
  trend     <- result$time.series[, "trend"]
  rem_var   <- var(remainder)
  seas_str  <- max(0, 1 - rem_var / (var(seasonal) + rem_var))
  trend_str <- max(0, 1 - rem_var / (var(trend)    + rem_var))
  cat(sprintf("seasonal_strength=%.4f  trend_strength=%.4f  remainder_var=%.4f  exec=%.3f ms\n",
              seas_str, trend_str, rem_var, ms))
  add_rows("r", "STL_period12", dataset, list(
    seasonal_strength = fmt(seas_str),
    trend_strength    = fmt(trend_str),
    remainder_var     = fmt(rem_var),
    exec_ms           = fmt(ms)
  ))
}

# 9. VAR(1) ────────────────────────────────────────────────────────────────────
bench_var <- function(data1, data2) {
  cat("  VAR(1) [hotel+ca_unemp] ... ")
  holdout <- 8L
  train1 <- data1[seq_len(length(data1) - holdout)]
  test1  <- tail(data1, holdout)
  train2 <- data2[seq_len(length(data2) - holdout)]
  train  <- cbind(hotel = train1, ca_unemp = train2)

  r <- median_ms(function() {
    fit <- VAR(train, p = 1, type = "const")
    predict(fit, n.ahead = holdout)
  })
  fc_obj <- r$result; ms <- r$ms
  fc1 <- fc_obj$fcst$hotel[, "fcst"]
  m <- forecast_metrics(test1, fc1)
  cat(sprintf("MAE=%.4f  RMSE=%.4f  MAPE=%.2f%%  exec=%.3f ms\n",
              as.numeric(m$MAE), as.numeric(m$RMSE), as.numeric(m$MAPE), ms))
  add_rows("r", "VAR_p1", "hotel+ca_unemp", c(m, exec_ms = fmt(ms)))
}

# 10. ADF test ─────────────────────────────────────────────────────────────────
bench_adf <- function(data, dataset) {
  cat(sprintf("  ADF test [%s] ... ", dataset))

  r <- median_ms(function() adf.test(data))
  result <- r$result; ms <- r$ms
  stat <- result$statistic
  pval <- result$p.value
  cat(sprintf("stat=%.4f  p=%.4f  exec=%.3f ms\n", stat, pval, ms))
  add_rows("r", "ADF_test", dataset, list(
    statistic = fmt(stat),
    pvalue    = fmt(pval),
    exec_ms   = fmt(ms)
  ))
}

# 11. KPSS test ────────────────────────────────────────────────────────────────
bench_kpss <- function(data, dataset) {
  cat(sprintf("  KPSS test [%s] ... ", dataset))

  r <- median_ms(function() kpss.test(data))
  result <- r$result; ms <- r$ms
  stat <- result$statistic
  pval <- result$p.value
  cat(sprintf("stat=%.4f  p=%.4f  exec=%.3f ms\n", stat, pval, ms))
  add_rows("r", "KPSS_test", dataset, list(
    statistic = fmt(stat),
    pvalue    = fmt(pval),
    exec_ms   = fmt(ms)
  ))
}

# 12. Local Level (KFAS) ───────────────────────────────────────────────────────
bench_local_level <- function(data, dataset) {
  cat(sprintf("  LocalLevel (KFAS) [%s] ... ", dataset))
  holdout <- 8L
  train <- data[seq_len(length(data) - holdout)]
  test  <- tail(data, holdout)

  r <- median_ms(function() {
    model <- SSModel(
      ts(train) ~ SSMtrend(degree = 1, Q = NA),
      H = NA
    )
    fit <- fitSSM(model, inits = c(0, 0))$model
    pred <- predict(fit, n.ahead = holdout)
    as.numeric(pred)
  })
  fc <- r$result; ms <- r$ms
  m <- forecast_metrics(test, fc)
  cat(sprintf("MAE=%.4f  RMSE=%.4f  MAPE=%.2f%%  exec=%.3f ms\n",
              as.numeric(m$MAE), as.numeric(m$RMSE), as.numeric(m$MAPE), ms))
  add_rows("r", "LocalLevel_MLE", dataset, c(m, exec_ms = fmt(ms)))
}

# 13. Auto ARIMA ───────────────────────────────────────────────────────────────
bench_auto_arima <- function(data, dataset) {
  cat(sprintf("  AutoARIMA(max p,d,q=3) [%s] ... ", dataset))
  holdout <- 12L
  train <- data[seq_len(length(data) - holdout)]
  test  <- tail(data, holdout)
  ts_train <- ts(train)

  r <- median_ms(function() {
    fit <- auto.arima(ts_train, max.p = 3, max.d = 2, max.q = 3,
                      ic = "aic", stepwise = TRUE, approximation = FALSE)
    as.numeric(forecast(fit, h = holdout)$mean)
  })
  fc <- r$result; ms <- r$ms
  m <- forecast_metrics(test, fc)
  cat(sprintf("MAE=%.4f  RMSE=%.4f  MAPE=%.2f%%  exec=%.3f ms\n",
              as.numeric(m$MAE), as.numeric(m$RMSE), as.numeric(m$MAPE), ms))
  add_rows("r", "AutoARIMA_max3_AIC", dataset, c(m, exec_ms = fmt(ms)))
}

# NA positions consistent with Java and Python benchmark scripts
NA_POSITIONS <- c(15, 30, 45, 60, 75, 90, 105, 120, 135, 150) + 1L  # 1-indexed in R

# 14. WMA (period=7) ───────────────────────────────────────────────────────────
bench_wma <- function(data, dataset) {
  cat(sprintf("  WMA(period=7) [%s] ... ", dataset))
  n <- 7L
  weights <- seq_len(n)

  r <- median_ms(function() {
    series <- zoo::zoo(data)
    as.numeric(zoo::rollapply(series, width = n,
                              FUN = function(x) sum(x * weights) / sum(weights),
                              align = "right", fill = NA))
  })
  smoothed <- r$result; ms <- r$ms
  valid   <- smoothed[!is.na(smoothed)]
  actual  <- data[seq(n, length(data))]
  m  <- calc_mae(actual, valid)
  rm <- calc_rmse(actual, valid)
  cat(sprintf("smoothing_mae=%.4f  rmse=%.4f  exec=%.3f ms\n", m, rm, ms))
  add_rows("r", "WMA_period7", dataset, list(
    smoothing_mae  = fmt(m),
    smoothing_rmse = fmt(rm),
    exec_ms        = fmt(ms)
  ))
}

# 15. Ljung-Box test (lags=12) ────────────────────────────────────────────────
bench_ljung_box <- function(data, dataset) {
  cat(sprintf("  Ljung-Box(lags=12, ARIMA residuals) [%s] ... ", dataset))
  fit <- Arima(ts(data), order = c(1, 1, 1), method = "CSS")
  resids <- residuals(fit)

  r <- median_ms(function() Box.test(resids, lag = 12, type = "Ljung-Box"))
  result <- r$result; ms <- r$ms
  stat <- result$statistic
  pval <- result$p.value
  cat(sprintf("stat=%.4f  p=%.4f  exec=%.3f ms\n", stat, pval, ms))
  add_rows("r", "LjungBox_lag12", dataset, list(
    statistic = fmt(stat),
    pvalue    = fmt(pval),
    exec_ms   = fmt(ms)
  ))
}

# 16. Missing value imputation (linear interpolation) ─────────────────────────
bench_imputation <- function(data, dataset) {
  cat(sprintf("  Imputation (linear interp, 10 NAs) [%s] ... ", dataset))
  original  <- data
  corrupted <- data
  corrupted[NA_POSITIONS] <- NA

  r <- median_ms(function() {
    approx(seq_along(corrupted), corrupted, seq_along(corrupted))$y
  })
  imputed <- r$result; ms <- r$ms
  m <- mean(abs(original[NA_POSITIONS] - imputed[NA_POSITIONS]))
  cat(sprintf("imputation_mae=%.4f  exec=%.3f ms\n", m, ms))
  add_rows("r", "Imputation_linear", dataset, list(
    imputation_mae = fmt(m),
    exec_ms        = fmt(ms)
  ))
}

# 17. Box-Cox lambda search ────────────────────────────────────────────────────
bench_boxcox <- function(data, dataset) {
  cat(sprintf("  Box-Cox lambda search [%s] ... ", dataset))
  ts_data <- ts(data, frequency = 12)

  r <- median_ms(function() BoxCox.lambda(ts_data))
  lam <- r$result; ms <- r$ms
  cat(sprintf("lambda=%.4f  exec=%.3f ms\n", lam, ms))
  add_rows("r", "BoxCox_lambda", dataset, list(
    lambda  = fmt(lam),
    exec_ms = fmt(ms)
  ))
}

# ── run all benchmarks ────────────────────────────────────────────────────────

hotel <- load_txt(file.path(root, "data", "hotel.txt"))
jj    <- load_txt(file.path(root, "data", "jj.txt"))
ap    <- load_csv(file.path(root, "benchmarks", "data", "airpassengers.csv"))
ca    <- load_txt(file.path(root, "data", "CA_Unemployment_Rate.txt"))[seq_len(168)]

bench_arima(hotel, "hotel")
bench_sarima(ap, "airpassengers")
bench_single_ets(jj, "jj")
bench_double_ets(hotel, "hotel")
bench_triple_ets(ap, "airpassengers")
bench_sma(hotel, "hotel")
bench_ema(hotel, "hotel")
bench_stl(ap, "airpassengers")
bench_var(hotel, ca)
bench_adf(hotel, "hotel")
bench_kpss(hotel, "hotel")
bench_local_level(jj, "jj")
bench_auto_arima(hotel, "hotel")
bench_wma(hotel, "hotel")
bench_ljung_box(hotel, "hotel")
bench_imputation(hotel, "hotel")
bench_boxcox(ap, "airpassengers")

write_results(file.path(root, "benchmarks", "results", "r_results.csv"))
