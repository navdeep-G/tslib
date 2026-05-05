#' Local level model forecast
#'
#' Fits a local level (random walk plus noise) state-space model via Kalman
#' MLE and produces out-of-sample forecasts.
#'
#' @param client A [`tslib_client`] object.
#' @param data Numeric vector of observations.
#' @param steps Forecast steps (default 1).
#' @param confidence_level Confidence level for intervals (default 0.95).
#' @return Named list with `forecasts` and `intervals`.
#' @export
tslib_local_level_forecast <- function(client, data, steps = 1L,
                                       confidence_level = 0.95) {
  body <- list(
    data            = as.list(data),
    steps           = steps,
    confidenceLevel = confidence_level
  )
  .tslib_post(client, "/api/statespace/local-level/forecast", body)
}

#' Local level Kalman smoother and forecast
#'
#' Returns filtered and smoothed state estimates in addition to out-of-sample
#' forecasts.
#'
#' @param client A [`tslib_client`] object.
#' @param data Numeric vector of observations.
#' @param steps Forecast steps (default 1).
#' @param confidence_level Confidence level (default 0.95).
#' @return Named list with `filtered_states`, `smoothed_signal`, variance
#'   parameters, `log_likelihood`, `forecasts`, `forecast_variances`, and
#'   `intervals`.
#' @export
tslib_local_level_filter <- function(client, data, steps = 1L,
                                     confidence_level = 0.95) {
  body <- list(
    data            = as.list(data),
    steps           = steps,
    confidenceLevel = confidence_level
  )
  .tslib_post(client, "/api/statespace/local-level/filter", body)
}

#' Kalman filter with explicit noise parameters
#'
#' Runs the Kalman filter with user-supplied process and observation noise
#' variances.
#'
#' @param client A [`tslib_client`] object.
#' @param data Numeric vector of observations.
#' @param process_variance Process noise variance Q (default 1.0).
#' @param observation_variance Observation noise variance R (default 1.0).
#' @param steps Forecast steps beyond the observed data (default 0).
#' @return Named list with `predicted_states`, `filtered_states`,
#'   `filtered_covariances`, `innovations`, `log_likelihood`, `forecasts`,
#'   and `forecast_variances`.
#' @export
tslib_kalman_filter <- function(client, data,
                                process_variance     = 1.0,
                                observation_variance = 1.0,
                                steps                = 0L) {
  body <- list(
    data                 = as.list(data),
    processVariance      = process_variance,
    observationVariance  = observation_variance,
    steps                = steps
  )
  .tslib_post(client, "/api/statespace/kalman/filter", body)
}
