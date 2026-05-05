#' Ljung-Box portmanteau test
#'
#' Tests whether a sequence of residuals shows significant autocorrelation up
#' to the given number of lags. A small p-value (< 0.05) suggests the residuals
#' are not white noise.
#'
#' @param client A [`tslib_client`] object.
#' @param residuals Numeric vector of model residuals.
#' @param lags Number of lags to include in the test statistic (default 10).
#' @param df_adjustment Degrees-of-freedom adjustment for estimated model
#'   parameters (e.g. p + q for an ARIMA model) (default 0).
#' @return Named list with `statistic`, `pValue`, `lags`, and
#'   `rejectsAtFivePercent` (logical).
#' @export
tslib_ljung_box <- function(client, residuals,
                            lags          = 10L,
                            df_adjustment = 0L) {
  body <- list(
    residuals                    = as.list(residuals),
    lags                         = lags,
    degreesOfFreedomAdjustment   = df_adjustment
  )
  .tslib_post(client, "/api/diagnostics/ljung-box", body)
}
