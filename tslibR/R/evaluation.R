#' Compute forecast accuracy metrics
#'
#' @param client A [`tslib_client`] object.
#' @param actual Numeric vector of actual (realised) values.
#' @param forecast Numeric vector of forecast values (same length as `actual`).
#' @param seasonal_period Seasonal period used for the MASE denominator
#'   (default 1, i.e. non-seasonal naive).
#' @param training_series Optional in-sample training series used to compute
#'   the MASE denominator (improves MASE when provided).
#' @return Named list with `mae`, `rmse`, `mape`, `smape`, `mase`, and
#'   `mean_error`.
#' @export
tslib_metrics <- function(client, actual, forecast,
                          seasonal_period  = 1L,
                          training_series  = NULL) {
  body <- .compact(list(
    actual          = as.list(actual),
    forecast        = as.list(forecast),
    seasonalPeriod  = seasonal_period,
    trainingSeries  = if (!is.null(training_series)) as.list(training_series)
  ))
  .tslib_post(client, "/api/evaluate/metrics", body)
}

#' Rolling-origin backtest
#'
#' Performs a rolling-origin (time-series cross-validation) backtest for the
#' specified model.
#'
#' @param client A [`tslib_client`] object.
#' @param data Numeric vector of observations.
#' @param model_spec Named list describing the model. Required key: `type`
#'   (e.g. `"ARIMA"`, `"AUTO_ARIMA"`, `"HOLT_WINTERS"`). Optional keys
#'   depend on model type: `p`, `d`, `q`, `seasonalP`, `seasonalD`,
#'   `seasonalQ`, `seasonalPeriod`, `alpha`, `beta`, `gamma`,
#'   `initializationMethod`, `period`, `maxP`, `maxD`, `maxQ`, `criterion`.
#' @param min_train_size Minimum training set size (default 20).
#' @param horizon Forecast horizon per origin (default 1).
#' @param step_size Step between consecutive origins (default 1).
#' @param seasonal_period Seasonal period for MASE (default 1).
#' @return Named list with `actual`, `forecast`, `origins` arrays, and
#'   accuracy metrics.
#' @export
tslib_backtest <- function(client, data, model_spec,
                           min_train_size  = 20L,
                           horizon         = 1L,
                           step_size       = 1L,
                           seasonal_period = 1L) {
  body <- list(
    data            = as.list(data),
    modelSpec       = model_spec,
    minTrainSize    = min_train_size,
    horizon         = horizon,
    stepSize        = step_size,
    seasonalPeriod  = seasonal_period
  )
  .tslib_post(client, "/api/evaluate/backtest", body)
}

#' Split a series into training and test sets
#'
#' Exactly one of `train_size` or `train_ratio` must be supplied.
#'
#' @param client A [`tslib_client`] object.
#' @param data Numeric vector of observations.
#' @param train_size Integer number of training observations.
#' @param train_ratio Fraction of observations for training (a value in
#'   (0, 1)).
#' @return Named list with `train`, `test` vectors and `train_size`,
#'   `test_size` integers.
#' @export
tslib_train_test_split <- function(client, data,
                                   train_size  = NULL,
                                   train_ratio = NULL) {
  body <- .compact(list(
    data        = as.list(data),
    trainSize   = train_size,
    trainRatio  = train_ratio
  ))
  .tslib_post(client, "/api/evaluate/train-test-split", body)
}

#' Benchmark multiple models side-by-side
#'
#' Runs a rolling-origin backtest for each named model and returns a
#' comparable accuracy summary.
#'
#' @param client A [`tslib_client`] object.
#' @param data Numeric vector of observations.
#' @param models List of named model entries. Each entry must be a list with
#'   `name` (character) and `spec` (a model-spec list as in
#'   [tslib_backtest()]).
#' @param min_train_size Minimum training size (default 20).
#' @param horizon Forecast horizon (default 1).
#' @param step_size Step between origins (default 1).
#' @param seasonal_period Seasonal period for MASE (default 1).
#' @return List of named lists, one per model, each containing `model_name`
#'   and accuracy metrics.
#' @export
tslib_benchmark <- function(client, data, models,
                            min_train_size  = 20L,
                            horizon         = 1L,
                            step_size       = 1L,
                            seasonal_period = 1L) {
  body <- list(
    data            = as.list(data),
    models          = models,
    minTrainSize    = min_train_size,
    horizon         = horizon,
    stepSize        = step_size,
    seasonalPeriod  = seasonal_period
  )
  .tslib_post(client, "/api/evaluate/benchmark", body)
}
