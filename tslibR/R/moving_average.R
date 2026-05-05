#' Compute a moving average
#'
#' @param client A [`tslib_client`] object.
#' @param data Numeric vector of observations.
#' @param period Window size (default 5).
#' @param type Moving average type: `"SIMPLE"` (SMA), `"EMA"` (exponential),
#'   `"WMA"` (weighted), or `"CMA"` (cumulative). Default `"SIMPLE"`.
#' @param alpha Smoothing factor for EMA (optional; server uses a default when
#'   omitted).
#' @return Numeric vector of moving-average values.
#' @export
tslib_moving_average <- function(client, data,
                                 period = 5L,
                                 type   = "SIMPLE",
                                 alpha  = NULL) {
  body <- .compact(list(
    data   = as.list(data),
    period = period,
    type   = type,
    alpha  = alpha
  ))
  resp <- .tslib_post(client, "/api/moving-average", body)
  unlist(resp[["forecasts"]])
}
