test_that("tslib_moving_average returns averaged series", {
  with_webmock({
    mock_post("/api/moving-average", list(forecasts = c(2.0, 3.0, 4.0)))
    cl     <- default_client()
    result <- tslib_moving_average(cl, c(1, 2, 3, 4, 5), period = 3L)
    expect_equal(result, c(2.0, 3.0, 4.0))
  })
})

test_that("tslib_moving_average passes type=EMA", {
  with_webmock({
    mock_post("/api/moving-average", list(forecasts = c(1.5, 2.25, 3.125)))
    cl     <- default_client()
    result <- tslib_moving_average(cl, c(1, 2, 3, 4, 5),
                                   type = "EMA", alpha = 0.5)
    expect_length(result, 3L)
  })
})
