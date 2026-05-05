test_that("tslib_arima_forecast returns forecasts and model info", {
  with_webmock({
    mock_post("/api/arima/forecast", list(
      forecasts         = c(101.0, 102.0),
      intervals         = list(),
      arCoefficients    = c(0.5),
      maCoefficients    = list(),
      intercept         = 1.0,
      innovationVariance = 0.1,
      p = 1L, d = 1L, q = 0L
    ))
    cl     <- default_client()
    result <- tslib_arima_forecast(cl, c(1, 2, 3), p = 1, d = 1, q = 0, steps = 2)
    expect_equal(unlist(result$forecasts), c(101.0, 102.0))
    expect_equal(result$p, 1L)
    expect_length(result$intervals, 0L)
  })
})

test_that("tslib_arima_forecast with intervals hits forecast-intervals endpoint", {
  with_webmock({
    interval <- list(step = 1L, pointForecast = 101.0, lower = 95.0,
                     upper = 107.0, confidenceLevel = 0.95)
    mock_post("/api/arima/forecast-intervals", list(
      forecasts         = c(101.0),
      intervals         = list(interval),
      arCoefficients    = list(),
      maCoefficients    = list(),
      intercept         = 0.0,
      innovationVariance = 1.0,
      p = 1L, d = 0L, q = 0L
    ))
    cl     <- default_client()
    result <- tslib_arima_forecast(cl, c(1, 2), p = 1, d = 0, q = 0,
                                   include_intervals = TRUE)
    expect_length(result$intervals, 1L)
    expect_equal(result$intervals[[1L]]$lower, 95.0)
    expect_equal(result$intervals[[1L]]$upper, 107.0)
  })
})

test_that("tslib_sarima_forecast returns seasonal model info", {
  with_webmock({
    mock_post("/api/sarima/forecast", list(
      forecasts              = c(10.0),
      intervals              = list(),
      arCoefficients         = c(0.3),
      maCoefficients         = list(),
      seasonalArCoefficients = c(0.2),
      seasonalMaCoefficients = list(),
      intercept              = 0.0,
      innovationVariance     = 0.5,
      p = 1L, d = 0L, q = 0L,
      seasonalP = 1L, seasonalD = 1L, seasonalQ = 0L,
      seasonalPeriod = 12L
    ))
    cl     <- default_client()
    result <- tslib_sarima_forecast(cl, seq_len(36), p = 1, d = 0, q = 0,
                                    seasonal_p = 1, seasonal_d = 1,
                                    seasonal_q = 0)
    expect_equal(unlist(result$forecasts), 10.0)
    expect_equal(result$seasonalPeriod, 12L)
  })
})

test_that("tslib_arima_order_search returns best order", {
  with_webmock({
    mock_post("/api/arima/order-search", list(
      modelType      = "ARIMA",
      p = 1L, d = 1L, q = 0L,
      seasonalP = 0L, seasonalD = 0L, seasonalQ = 0L, seasonalPeriod = 0L,
      criterion      = "AIC",
      score          = -120.5
    ))
    cl     <- default_client()
    result <- tslib_arima_order_search(cl, rnorm(50))
    expect_equal(result$p, 1L)
    expect_equal(result$criterion, "AIC")
    expect_lt(result$score, 0)
  })
})

test_that("tslib_var_forecast returns multivariate forecasts", {
  with_webmock({
    mock_post("/api/var/forecast", list(
      forecasts  = list(c(1.1, 2.1), c(1.2, 2.2)),
      lagOrder   = 2L,
      numSeries  = 2L,
      aic        = -10.5
    ))
    cl     <- default_client()
    result <- tslib_var_forecast(cl,
                                 series = list(c(1, 2, 3), c(4, 5, 6)),
                                 steps  = 2L)
    expect_length(result$forecasts, 2L)
    expect_equal(result$lagOrder, 2L)
    expect_equal(result$aic, -10.5)
  })
})
