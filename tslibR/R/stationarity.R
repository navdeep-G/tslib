#' Augmented Dickey-Fuller (ADF) test
#'
#' Tests the null hypothesis that a unit root is present (series is
#' non-stationary).
#'
#' @param client A [`tslib_client`] object.
#' @param data Numeric vector of observations.
#' @param lag Optional number of lags. If `NULL` the server selects the lag
#'   automatically.
#' @return Named list with `statistic`, `pValue`, `lag`, `stationary` (bool),
#'   and `needsDiff` (bool).
#' @export
tslib_adf <- function(client, data, lag = NULL) {
  body <- .compact(list(data = as.list(data), lag = lag))
  .tslib_post(client, "/api/tests/adf", body)
}

#' KPSS stationarity test
#'
#' Tests the null hypothesis that a series is stationary (opposite of ADF).
#' Use together with ADF for a confirmatory pair of tests.
#'
#' @param client A [`tslib_client`] object.
#' @param data Numeric vector of observations.
#' @param regression_type Regression type: `"LEVEL"` (default) tests
#'   level-stationarity; `"TREND"` tests trend-stationarity.
#' @param lags Optional number of lags for the long-run variance estimator.
#' @return Named list with `statistic`, `lags`, `regressionType`, critical
#'   values, and stationarity flags at 1 % and 5 % levels.
#' @export
tslib_kpss <- function(client, data,
                       regression_type = "LEVEL",
                       lags            = NULL) {
  body <- .compact(list(
    data            = as.list(data),
    regressionType  = regression_type,
    lags            = lags
  ))
  .tslib_post(client, "/api/tests/kpss", body)
}
