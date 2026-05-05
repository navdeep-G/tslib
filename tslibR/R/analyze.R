#' Analyze a time series
#'
#' Computes descriptive statistics, ACF/PACF, an ADF stationarity test, and
#' common transforms (log, first difference, rolling average) in one call.
#'
#' @param client A [`tslib_client`] object.
#' @param data Numeric vector of observations.
#' @param k Integer lag for autocovariance (default 1).
#' @param n Number of ACF/PACF lags to return (default 10).
#' @param window_size Rolling-average window size (default 5).
#' @return Named list with statistics, ACF/PACF arrays, ADF result, and
#'   pre-computed transforms.
#' @export
tslib_analyze <- function(client, data, k = 1L, n = 10L, window_size = 5L) {
  body <- list(data = as.list(data), k = k, n = n, windowSize = window_size)
  .tslib_post(client, "/api/analyze", body)
}
