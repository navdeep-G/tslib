#' Impute missing values
#'
#' Fills `NA` gaps in a time series using the chosen strategy.
#'
#' @param client A [`tslib_client`] object.
#' @param data Numeric vector, may contain `NA` for missing observations.
#' @param strategy Imputation strategy. One of `"LINEAR_INTERPOLATION"`,
#'   `"MEAN"`, `"MEDIAN"`, or `"FORWARD_FILL"` (default
#'   `"LINEAR_INTERPOLATION"`).
#' @return Numeric vector with `NA` values replaced.
#' @export
tslib_impute <- function(client, data,
                         strategy = "LINEAR_INTERPOLATION") {
  # R NA → JSON null so the server treats them as missing
  data_list <- lapply(data, function(x) if (is.na(x)) NULL else x)
  resp <- .tslib_post(client, "/api/dataquality/impute",
                      list(data = data_list, strategy = strategy))
  unlist(resp[["forecasts"]])
}

#' Detect outliers
#'
#' @param client A [`tslib_client`] object.
#' @param data Numeric vector of observations.
#' @param method Detection method: `"Z_SCORE"` or `"IQR"` (default
#'   `"Z_SCORE"`).
#' @param threshold Detection threshold (default 3.0 for Z-score,
#'   1.5 is typical for IQR).
#' @return Named list with `outlierIndices` (0-based integer vector matching
#'   server convention).
#' @export
tslib_outliers <- function(client, data,
                           method    = "Z_SCORE",
                           threshold = 3.0) {
  .tslib_post(client, "/api/dataquality/outliers",
              list(data = as.list(data), method = method,
                   threshold = threshold))
}

#' Winsorize a series
#'
#' Clips extreme values to specified quantile bounds, reducing the influence
#' of outliers without removing observations.
#'
#' @param client A [`tslib_client`] object.
#' @param data Numeric vector of observations.
#' @param lower_probability Lower quantile clip point (default 0.05).
#' @param upper_probability Upper quantile clip point (default 0.95).
#' @return Numeric vector with extreme values clipped to the quantile bounds.
#' @export
tslib_winsorize <- function(client, data,
                            lower_probability = 0.05,
                            upper_probability = 0.95) {
  resp <- .tslib_post(client, "/api/dataquality/winsorize",
                      list(data              = as.list(data),
                           lowerProbability  = lower_probability,
                           upperProbability  = upper_probability))
  unlist(resp[["forecasts"]])
}
