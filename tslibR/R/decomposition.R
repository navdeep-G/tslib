#' STL decomposition
#'
#' Decomposes a time series into trend, seasonal, and remainder components
#' using Seasonal-Trend decomposition with Loess (STL).
#'
#' @param client A [`tslib_client`] object.
#' @param data Numeric vector of observations.
#' @param period Seasonal period (e.g. 12 for monthly data).
#' @param trend_window Optional trend smoother window (must be odd).
#' @param seasonal_window Optional seasonal smoother window (must be odd).
#' @param iterations Optional number of inner robustness iterations.
#' @param outer_iterations Optional number of outer robustness iterations
#'   (set > 0 for robust STL).
#' @return Named list with `trend`, `seasonal`, `remainder`, and
#'   `reconstructed` numeric vectors.
#' @export
tslib_stl <- function(client, data, period,
                      trend_window    = NULL,
                      seasonal_window = NULL,
                      iterations      = NULL,
                      outer_iterations = NULL) {
  body <- .compact(list(
    data            = as.list(data),
    period          = period,
    trendWindow     = trend_window,
    seasonalWindow  = seasonal_window,
    iterations      = iterations,
    outerIterations = outer_iterations
  ))
  .tslib_post(client, "/api/decompose/stl", body)
}
