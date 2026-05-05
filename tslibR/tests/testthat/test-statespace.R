test_that("tslib_local_level_forecast returns forecasts", {
  with_webmock({
    mock_post("/api/statespace/local-level/forecast",
              list(forecasts = c(3.5), intervals = list()))
    cl     <- default_client()
    result <- tslib_local_level_forecast(cl, c(1, 2, 3))
    expect_equal(unlist(result$forecasts), 3.5)
  })
})

test_that("tslib_local_level_filter returns filtered states and log-likelihood", {
  with_webmock({
    mock_post("/api/statespace/local-level/filter", list(
      filteredStates     = c(1.0, 2.0),
      smoothedSignal     = c(1.1, 2.1),
      processVariance    = 0.5,
      observationVariance = 1.0,
      logLikelihood      = -4.2,
      forecasts          = c(3.0),
      forecastVariances  = c(0.4),
      intervals          = list()
    ))
    cl     <- default_client()
    result <- tslib_local_level_filter(cl, c(1, 2))
    expect_equal(unlist(result$filteredStates), c(1.0, 2.0))
    expect_equal(result$logLikelihood, -4.2)
  })
})

test_that("tslib_kalman_filter returns predicted and filtered states", {
  with_webmock({
    mock_post("/api/statespace/kalman/filter", list(
      predictedStates     = c(1.0, 2.0),
      filteredStates      = c(1.1, 2.1),
      filteredCovariances = c(0.5, 0.4),
      innovations         = c(0.1, 0.1),
      logLikelihood       = -5.0,
      forecasts           = list(),
      forecastVariances   = list()
    ))
    cl     <- default_client()
    result <- tslib_kalman_filter(cl, c(1, 2))
    expect_equal(unlist(result$filteredStates), c(1.1, 2.1))
    expect_equal(result$logLikelihood, -5.0)
  })
})
