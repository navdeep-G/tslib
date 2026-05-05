test_that("tslib_auto_arima returns best order and seasonal flag", {
  with_webmock({
    best_order <- list(
      modelType = "ARIMA", p = 1L, d = 1L, q = 0L,
      seasonalP = 0L, seasonalD = 0L, seasonalQ = 0L, seasonalPeriod = 0L,
      criterion = "AIC", score = -50.0
    )
    mock_post("/api/auto/arima/forecast", list(
      forecasts  = c(5.0),
      intervals  = list(),
      bestOrder  = best_order,
      seasonal   = FALSE
    ))
    cl     <- default_client()
    result <- tslib_auto_arima(cl, c(1, 2, 3, 4))
    expect_false(result$seasonal)
    expect_equal(result$bestOrder$p, 1L)
  })
})

test_that("tslib_auto_ets returns best ETS type", {
  with_webmock({
    mock_post("/api/auto/ets/forecast", list(
      forecasts      = c(5.0),
      intervals      = list(),
      bestType       = "TRIPLE",
      bestParameters = c(0.3, 0.1, 0.1),
      bestScore      = -30.0
    ))
    cl     <- default_client()
    result <- tslib_auto_ets(cl, c(1, 2, 3, 4))
    expect_equal(result$bestType, "TRIPLE")
    expect_equal(result$bestScore, -30.0)
  })
})
