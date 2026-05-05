test_that("tslib_metrics returns all accuracy metrics", {
  with_webmock({
    mock_post("/api/evaluate/metrics", list(
      mae       = 1.0,
      rmse      = 1.5,
      mape      = 0.1,
      smape     = 0.09,
      mase      = 0.8,
      meanError = 0.5
    ))
    cl     <- default_client()
    result <- tslib_metrics(cl, c(10, 20), c(11, 21))
    expect_equal(result$mae,  1.0)
    expect_equal(result$rmse, 1.5)
    expect_equal(result$mase, 0.8)
  })
})

test_that("tslib_train_test_split returns correct sizes", {
  with_webmock({
    mock_post("/api/evaluate/train-test-split", list(
      train     = c(1, 2, 3),
      test      = c(4, 5),
      trainSize = 3L,
      testSize  = 2L
    ))
    cl     <- default_client()
    result <- tslib_train_test_split(cl, c(1, 2, 3, 4, 5), train_ratio = 0.6)
    expect_equal(result$trainSize, 3L)
    expect_equal(result$testSize,  2L)
  })
})

test_that("tslib_backtest returns actual, forecast, and metrics", {
  with_webmock({
    mock_post("/api/evaluate/backtest", list(
      actual   = c(10, 11, 12),
      forecast = c(10.5, 11.2, 11.8),
      origins  = c(20L, 21L, 22L),
      mae      = 0.3,
      rmse     = 0.35,
      mape     = 0.02,
      smape    = 0.02,
      mase     = 0.5
    ))
    cl     <- default_client()
    spec   <- list(type = "ARIMA", p = 1L, d = 1L, q = 0L)
    result <- tslib_backtest(cl, seq_len(30), model_spec = spec)
    expect_length(result$actual,   3L)
    expect_equal(result$mae, 0.3)
  })
})

test_that("tslib_benchmark returns list of model summaries", {
  with_webmock({
    mock_post("/api/evaluate/benchmark", list(
      list(modelName = "ARIMA(1,1,0)", mae = 1.0, rmse = 1.2,
           mape = 0.05, smape = 0.05, mase = 0.7),
      list(modelName = "SES",          mae = 1.5, rmse = 1.7,
           mape = 0.08, smape = 0.08, mase = 1.0)
    ))
    cl      <- default_client()
    models  <- list(
      list(name = "ARIMA(1,1,0)", spec = list(type = "ARIMA", p = 1L, d = 1L, q = 0L)),
      list(name = "SES",          spec = list(type = "SES"))
    )
    result <- tslib_benchmark(cl, seq_len(50), models = models)
    expect_length(result, 2L)
    expect_equal(result[[1L]]$modelName, "ARIMA(1,1,0)")
  })
})
