#' Fit an ARIMA model and forecast
#'
#' @param client A [`tslib_client`] object.
#' @param data Numeric vector of observations.
#' @param p AR order.
#' @param d Differencing order.
#' @param q MA order.
#' @param steps Number of forecast steps (default 1).
#' @param confidence_level Confidence level for prediction intervals (default 0.95).
#' @param include_intervals Return prediction intervals? (default `FALSE`).
#' @param max_iterations Maximum optimizer iterations (default 200).
#' @param tolerance Optimizer convergence tolerance (default 1e-8).
#' @return Named list with `forecasts`, coefficient arrays, model orders, and
#'   optionally `intervals`.
#' @export
tslib_arima_forecast <- function(client, data, p, d, q,
                                 steps             = 1L,
                                 confidence_level  = 0.95,
                                 include_intervals = FALSE,
                                 max_iterations    = 200L,
                                 tolerance         = 1e-8) {
  path <- if (include_intervals) "/api/arima/forecast-intervals" else "/api/arima/forecast"
  body <- list(
    data           = as.list(data),
    p              = p, d = d, q = q,
    steps          = steps,
    confidenceLevel = confidence_level,
    maxIterations  = max_iterations,
    tolerance      = tolerance
  )
  .tslib_post(client, path, body)
}

#' Fit a SARIMA model and forecast
#'
#' @param client A [`tslib_client`] object.
#' @param data Numeric vector of observations.
#' @param p Non-seasonal AR order.
#' @param d Non-seasonal differencing order.
#' @param q Non-seasonal MA order.
#' @param seasonal_p Seasonal AR order.
#' @param seasonal_d Seasonal differencing order.
#' @param seasonal_q Seasonal MA order.
#' @param seasonal_period Seasonal period (default 12).
#' @param steps Forecast steps (default 1).
#' @param confidence_level Confidence level (default 0.95).
#' @param include_intervals Return prediction intervals? (default `FALSE`).
#' @param max_iterations Maximum optimizer iterations (default 200).
#' @param tolerance Convergence tolerance (default 1e-8).
#' @return Named list with forecasts, seasonal and non-seasonal coefficients,
#'   model orders, and optionally intervals.
#' @export
tslib_sarima_forecast <- function(client, data, p, d, q,
                                  seasonal_p, seasonal_d, seasonal_q,
                                  seasonal_period   = 12L,
                                  steps             = 1L,
                                  confidence_level  = 0.95,
                                  include_intervals = FALSE,
                                  max_iterations    = 200L,
                                  tolerance         = 1e-8) {
  path <- if (include_intervals) "/api/sarima/forecast-intervals" else "/api/sarima/forecast"
  body <- list(
    data            = as.list(data),
    p = p, d = d, q = q,
    seasonalP       = seasonal_p,
    seasonalD       = seasonal_d,
    seasonalQ       = seasonal_q,
    seasonalPeriod  = seasonal_period,
    steps           = steps,
    confidenceLevel = confidence_level,
    maxIterations   = max_iterations,
    tolerance       = tolerance
  )
  .tslib_post(client, path, body)
}

#' Fit an ARIMAX model and forecast
#'
#' @param client A [`tslib_client`] object.
#' @param data Numeric vector of observations.
#' @param exogenous Matrix of exogenous regressors with shape
#'   `[n_features, n_obs]` (rows = features, columns = time points).
#' @param future_exogenous Matrix of future exogenous values with shape
#'   `[n_features, steps]`.
#' @param p AR order.
#' @param d Differencing order.
#' @param q MA order.
#' @param max_iterations Maximum optimizer iterations (default 200).
#' @param tolerance Convergence tolerance (default 1e-8).
#' @return Named list with `forecasts` and coefficient arrays.
#' @export
tslib_arimax_forecast <- function(client, data, exogenous, future_exogenous,
                                  p, d, q,
                                  max_iterations = 200L,
                                  tolerance      = 1e-8) {
  to_row_lists <- function(m) lapply(seq_len(nrow(m)), function(i) as.list(m[i, ]))
  body <- list(
    data            = as.list(data),
    exogenous       = to_row_lists(exogenous),
    futureExogenous = to_row_lists(future_exogenous),
    p = p, d = d, q = q,
    maxIterations   = max_iterations,
    tolerance       = tolerance
  )
  .tslib_post(client, "/api/arimax/forecast", body)
}

#' Search for the best ARIMA order
#'
#' Evaluates all (p, d, q) combinations up to the specified maxima and returns
#' the one that minimises the chosen information criterion.
#'
#' @param client A [`tslib_client`] object.
#' @param data Numeric vector of observations.
#' @param max_p Maximum AR order (default 3).
#' @param max_d Maximum differencing order (default 2).
#' @param max_q Maximum MA order (default 3).
#' @param criterion Information criterion: `"AIC"`, `"BIC"`, or `"AICc"`
#'   (default `"AIC"`).
#' @param max_seasonal_p Optional maximum seasonal AR order.
#' @param max_seasonal_d Optional maximum seasonal differencing order.
#' @param max_seasonal_q Optional maximum seasonal MA order.
#' @param seasonal_period Optional seasonal period.
#' @return Named list with the best `p`, `d`, `q` (and seasonal equivalents),
#'   the criterion used, and the criterion score.
#' @export
tslib_arima_order_search <- function(client, data,
                                     max_p = 3L, max_d = 2L, max_q = 3L,
                                     criterion      = "AIC",
                                     max_seasonal_p = NULL,
                                     max_seasonal_d = NULL,
                                     max_seasonal_q = NULL,
                                     seasonal_period = NULL) {
  body <- .compact(list(
    data            = as.list(data),
    maxP            = max_p,
    maxD            = max_d,
    maxQ            = max_q,
    criterion       = criterion,
    maxSeasonalP    = max_seasonal_p,
    maxSeasonalD    = max_seasonal_d,
    maxSeasonalQ    = max_seasonal_q,
    seasonalPeriod  = seasonal_period
  ))
  .tslib_post(client, "/api/arima/order-search", body)
}

#' Fit a VAR model and forecast
#'
#' @param client A [`tslib_client`] object.
#' @param series List of numeric vectors, one element per variable.
#' @param steps Forecast steps (default 1).
#' @param max_lag Maximum lag order to search (default 5).
#' @param lag_order Optional fixed lag order (skips search).
#' @return Named list with multivariate `forecasts`, `lag_order`, `num_series`,
#'   and `aic`.
#' @export
tslib_var_forecast <- function(client, series,
                               steps     = 1L,
                               max_lag   = 5L,
                               lag_order = NULL) {
  body <- .compact(list(
    series   = lapply(series, as.list),
    steps    = steps,
    maxLag   = max_lag,
    lagOrder = lag_order
  ))
  .tslib_post(client, "/api/var/forecast", body)
}
