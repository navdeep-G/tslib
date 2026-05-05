test_that("tslib_adf returns stationarity result", {
  with_webmock({
    mock_post("/api/tests/adf", list(
      statistic  = -3.5,
      pValue     = 0.01,
      lag        = 1L,
      stationary = TRUE,
      needsDiff  = FALSE
    ))
    cl     <- default_client()
    result <- tslib_adf(cl, c(1, 2, 3))
    expect_true(result$stationary)
    expect_false(result$needsDiff)
    expect_equal(result$lag, 1L)
  })
})

test_that("tslib_kpss returns stationarity flags and critical values", {
  with_webmock({
    mock_post("/api/tests/kpss", list(
      statistic                = 0.1,
      lags                     = 3L,
      regressionType           = "LEVEL",
      stationaryAtFivePercent  = TRUE,
      stationaryAtOnePercent   = TRUE,
      criticalValueFivePercent = 0.463,
      criticalValueOnePercent  = 0.739
    ))
    cl     <- default_client()
    result <- tslib_kpss(cl, c(1, 2, 3))
    expect_true(result$stationaryAtFivePercent)
    expect_equal(result$criticalValueFivePercent, 0.463)
  })
})
