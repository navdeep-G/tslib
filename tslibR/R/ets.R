#' Single exponential smoothing (SES) forecast
#'
#' @param client A [`tslib_client`] object.
#' @param data Numeric vector of observations.
#' @param alpha Smoothing parameter α ∈ (0, 1) (default 0.3).
#' @param steps Forecast steps (default 1).
#' @param confidence_level Confidence level for prediction intervals
#'   (default 0.95).
#' @return Named list with `forecasts` and `intervals`.
#' @export
tslib_ses <- function(client, data, alpha = 0.3, steps = 1L,
                      confidence_level = 0.95) {
  body <- list(
    data            = as.list(data),
    alpha           = alpha,
    steps           = steps,
    confidenceLevel = confidence_level
  )
  .tslib_post(client, "/api/ets/single/forecast", body)
}

#' Double exponential smoothing (Holt) forecast
#'
#' @param client A [`tslib_client`] object.
#' @param data Numeric vector of observations.
#' @param alpha Level smoothing parameter (default 0.3).
#' @param gamma Trend smoothing parameter (default 0.1).
#' @param initialization_method Initialization method integer code
#'   (default 0).
#' @param steps Forecast steps (default 1).
#' @param confidence_level Confidence level (default 0.95).
#' @return Named list with `forecasts` and `intervals`.
#' @export
tslib_holt <- function(client, data, alpha = 0.3, gamma = 0.1,
                       initialization_method = 0L,
                       steps = 1L, confidence_level = 0.95) {
  body <- list(
    data                 = as.list(data),
    alpha                = alpha,
    gamma                = gamma,
    initializationMethod = initialization_method,
    steps                = steps,
    confidenceLevel      = confidence_level
  )
  .tslib_post(client, "/api/ets/double/forecast", body)
}

#' Triple exponential smoothing (Holt-Winters) forecast
#'
#' @param client A [`tslib_client`] object.
#' @param data Numeric vector of observations.
#' @param alpha Level smoothing parameter (default 0.3).
#' @param beta Trend smoothing parameter (default 0.1).
#' @param gamma Seasonal smoothing parameter (default 0.1).
#' @param period Seasonal period (default 12).
#' @param steps Forecast steps (default 1).
#' @param confidence_level Confidence level (default 0.95).
#' @return Named list with `forecasts` and `intervals`.
#' @export
tslib_holt_winters <- function(client, data, alpha = 0.3, beta = 0.1,
                               gamma = 0.1, period = 12L,
                               steps = 1L, confidence_level = 0.95) {
  body <- list(
    data            = as.list(data),
    alpha           = alpha,
    beta            = beta,
    gamma           = gamma,
    period          = period,
    steps           = steps,
    confidenceLevel = confidence_level
  )
  .tslib_post(client, "/api/ets/triple/forecast", body)
}
