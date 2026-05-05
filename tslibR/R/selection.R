#' AutoARIMA: automatically select and forecast
#'
#' Searches the (p, d, q) space (and optionally seasonal orders) to find the
#' model that minimises the chosen information criterion, then forecasts.
#'
#' @param client A [`tslib_client`] object.
#' @param data Numeric vector of observations.
#' @param max_p Maximum AR order to search (default 3).
#' @param max_d Maximum differencing order (default 2).
#' @param max_q Maximum MA order to search (default 3).
#' @param steps Forecast steps (default 1).
#' @param confidence_level Confidence level for intervals (default 0.95).
#' @param criterion Information criterion: `"AIC"`, `"BIC"`, or `"AICc"`
#'   (default `"AIC"`).
#' @param max_seasonal_p Optional maximum seasonal AR order.
#' @param max_seasonal_d Optional maximum seasonal differencing order.
#' @param max_seasonal_q Optional maximum seasonal MA order.
#' @param seasonal_period Optional seasonal period.
#' @return Named list with `forecasts`, `intervals`, `bestOrder` (order search
#'   result), and `seasonal` (logical).
#' @export
tslib_auto_arima <- function(client, data,
                             max_p           = 3L,
                             max_d           = 2L,
                             max_q           = 3L,
                             steps           = 1L,
                             confidence_level = 0.95,
                             criterion       = "AIC",
                             max_seasonal_p  = NULL,
                             max_seasonal_d  = NULL,
                             max_seasonal_q  = NULL,
                             seasonal_period = NULL) {
  body <- .compact(list(
    data            = as.list(data),
    maxP            = max_p,
    maxD            = max_d,
    maxQ            = max_q,
    steps           = steps,
    confidenceLevel = confidence_level,
    criterion       = criterion,
    maxSeasonalP    = max_seasonal_p,
    maxSeasonalD    = max_seasonal_d,
    maxSeasonalQ    = max_seasonal_q,
    seasonalPeriod  = seasonal_period
  ))
  .tslib_post(client, "/api/auto/arima/forecast", body)
}

#' AutoETS: automatically select and forecast with exponential smoothing
#'
#' Evaluates SES, Holt, and Holt-Winters models (optionally with a seasonal
#' period) and returns forecasts from the best-scoring one.
#'
#' @param client A [`tslib_client`] object.
#' @param data Numeric vector of observations.
#' @param steps Forecast steps (default 1).
#' @param confidence_level Confidence level (default 0.95).
#' @param seasonal_period Optional seasonal period (enables Holt-Winters
#'   candidates when set).
#' @return Named list with `forecasts`, `intervals`, `bestType` (e.g.
#'   `"TRIPLE"`), `bestParameters`, and `bestScore`.
#' @export
tslib_auto_ets <- function(client, data,
                           steps            = 1L,
                           confidence_level = 0.95,
                           seasonal_period  = NULL) {
  body <- .compact(list(
    data            = as.list(data),
    steps           = steps,
    confidenceLevel = confidence_level,
    seasonalPeriod  = seasonal_period
  ))
  .tslib_post(client, "/api/auto/ets/forecast", body)
}
