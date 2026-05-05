test_that("tslib_ses returns forecasts", {
  with_webmock({
    mock_post("/api/ets/single/forecast",
              list(forecasts = c(5.0), intervals = list()))
    cl     <- default_client()
    result <- tslib_ses(cl, c(1, 2, 3, 4))
    expect_equal(unlist(result$forecasts), 5.0)
  })
})

test_that("tslib_holt returns forecasts", {
  with_webmock({
    mock_post("/api/ets/double/forecast",
              list(forecasts = c(6.0, 7.0), intervals = list()))
    cl     <- default_client()
    result <- tslib_holt(cl, c(1, 2, 3, 4), steps = 2L)
    expect_equal(unlist(result$forecasts), c(6.0, 7.0))
  })
})

test_that("tslib_holt_winters returns forecasts", {
  with_webmock({
    mock_post("/api/ets/triple/forecast",
              list(forecasts = c(10.0, 11.0), intervals = list()))
    cl     <- default_client()
    result <- tslib_holt_winters(cl, seq_len(24), period = 12L, steps = 2L)
    expect_equal(unlist(result$forecasts), c(10.0, 11.0))
  })
})
