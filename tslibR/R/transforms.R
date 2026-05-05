# Shared helper for endpoints that return {"forecasts": [...]}
.transform_simple <- function(client, endpoint, data) {
  resp <- .tslib_post(client, endpoint, list(data = as.list(data)))
  unlist(resp[["forecasts"]])
}

#' Log-transform a series
#' @param client A [`tslib_client`] object.
#' @param data Numeric vector.
#' @return Numeric vector (log-transformed).
#' @export
tslib_transform_log <- function(client, data)
  .transform_simple(client, "/api/transform/log", data)

#' Square-root transform a series
#' @param client A [`tslib_client`] object.
#' @param data Numeric vector.
#' @return Numeric vector (square-root-transformed).
#' @export
tslib_transform_sqrt <- function(client, data)
  .transform_simple(client, "/api/transform/sqrt", data)

#' Cube-root transform a series
#' @param client A [`tslib_client`] object.
#' @param data Numeric vector.
#' @return Numeric vector (cube-root-transformed).
#' @export
tslib_transform_cbrt <- function(client, data)
  .transform_simple(client, "/api/transform/cbrt", data)

#' Arbitrary-root transform
#' @param client A [`tslib_client`] object.
#' @param data Numeric vector.
#' @param r Root degree (e.g. 4 for fourth root).
#' @return Numeric vector.
#' @export
tslib_transform_root <- function(client, data, r) {
  resp <- .tslib_post(client, "/api/transform/root",
                      list(data = as.list(data), r = r))
  unlist(resp[["forecasts"]])
}

#' Box-Cox transform
#'
#' When `lambda` is `NULL` (default), the server searches for the optimal λ
#' within optional `lower_bound` / `upper_bound` constraints.
#'
#' @param client A [`tslib_client`] object.
#' @param data Numeric vector.
#' @param lambda Fixed λ value, or `NULL` to search.
#' @param lower_bound Optional lower bound for λ search.
#' @param upper_bound Optional upper bound for λ search.
#' @return Named list with `result` (transformed numeric vector) and `lambda`
#'   (the λ used or found).
#' @export
tslib_transform_boxcox <- function(client, data,
                                   lambda      = NULL,
                                   lower_bound = NULL,
                                   upper_bound = NULL) {
  body <- .compact(list(
    data       = as.list(data),
    lambda     = lambda,
    lowerBound = lower_bound,
    upperBound = upper_bound
  ))
  .tslib_post(client, "/api/transform/boxcox", body)
}

#' Inverse Box-Cox transform
#' @param client A [`tslib_client`] object.
#' @param data Numeric vector of transformed values.
#' @param lambda λ value used during the forward transform.
#' @return Numeric vector on the original scale.
#' @export
tslib_transform_boxcox_inverse <- function(client, data, lambda) {
  resp <- .tslib_post(client, "/api/transform/boxcox/inverse",
                      list(data = as.list(data), lambda = lambda))
  unlist(resp[["forecasts"]])
}

#' Difference a series
#' @param client A [`tslib_client`] object.
#' @param data Numeric vector.
#' @param order Differencing order (default 1).
#' @return Numeric vector (differenced series).
#' @export
tslib_transform_difference <- function(client, data, order = 1L) {
  resp <- .tslib_post(client, "/api/transform/difference",
                      list(data = as.list(data), order = order))
  unlist(resp[["forecasts"]])
}

#' Seasonal difference a series
#' @param client A [`tslib_client`] object.
#' @param data Numeric vector.
#' @param lag Seasonal lag (e.g. 12 for monthly data with annual seasonality).
#' @param order Differencing order (default 1).
#' @return Numeric vector.
#' @export
tslib_transform_seasonal_difference <- function(client, data, lag,
                                                order = 1L) {
  resp <- .tslib_post(client, "/api/transform/seasonal-difference",
                      list(data = as.list(data), lag = lag, order = order))
  unlist(resp[["forecasts"]])
}

#' Invert differencing
#'
#' Reconstructs the original-scale series from a differenced series and the
#' pre-differencing history.
#'
#' @param client A [`tslib_client`] object.
#' @param data Differenced numeric vector.
#' @param history Original (pre-differencing) numeric vector.
#' @param order Differencing order (default 1).
#' @return Numeric vector on the original scale.
#' @export
tslib_transform_difference_inverse <- function(client, data, history,
                                               order = 1L) {
  resp <- .tslib_post(client, "/api/transform/difference/inverse",
                      list(data    = as.list(data),
                           history = as.list(history),
                           order   = order))
  unlist(resp[["forecasts"]])
}
