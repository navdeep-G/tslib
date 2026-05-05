test_that("tslib_transform_log returns transformed values", {
  with_webmock({
    mock_post("/api/transform/log",
              list(forecasts = c(0.0, 0.693, 1.099)))
    cl     <- default_client()
    result <- tslib_transform_log(cl, c(1, 2, 3))
    expect_equal(result[1L], 0.0)
    expect_type(result, "double")
  })
})

test_that("tslib_transform_sqrt returns values", {
  with_webmock({
    mock_post("/api/transform/sqrt", list(forecasts = c(1.0, 1.414, 1.732)))
    cl <- default_client()
    expect_length(tslib_transform_sqrt(cl, c(1, 2, 3)), 3L)
  })
})

test_that("tslib_transform_cbrt returns values", {
  with_webmock({
    mock_post("/api/transform/cbrt", list(forecasts = c(1.0, 1.26, 1.44)))
    cl <- default_client()
    expect_length(tslib_transform_cbrt(cl, c(1, 2, 3)), 3L)
  })
})

test_that("tslib_transform_root passes r parameter", {
  with_webmock({
    mock_post("/api/transform/root", list(forecasts = c(1.0, 1.189)))
    cl     <- default_client()
    result <- tslib_transform_root(cl, c(1, 2), r = 4)
    expect_length(result, 2L)
  })
})

test_that("tslib_transform_boxcox returns result and lambda", {
  with_webmock({
    mock_post("/api/transform/boxcox",
              list(result = c(0.1, 0.2), lambda = 0.5))
    cl     <- default_client()
    result <- tslib_transform_boxcox(cl, c(1, 2))
    expect_equal(result$lambda, 0.5)
    expect_equal(unlist(result$result), c(0.1, 0.2))
  })
})

test_that("tslib_transform_boxcox_inverse returns original-scale values", {
  with_webmock({
    mock_post("/api/transform/boxcox/inverse",
              list(forecasts = c(1.0, 2.0)))
    cl     <- default_client()
    result <- tslib_transform_boxcox_inverse(cl, c(0.1, 0.2), lambda = 0.5)
    expect_equal(result, c(1.0, 2.0))
  })
})

test_that("tslib_transform_difference returns differenced series", {
  with_webmock({
    mock_post("/api/transform/difference", list(forecasts = c(1.0, 1.0)))
    cl     <- default_client()
    result <- tslib_transform_difference(cl, c(1, 2, 3))
    expect_equal(result, c(1.0, 1.0))
  })
})

test_that("tslib_transform_seasonal_difference passes lag", {
  with_webmock({
    mock_post("/api/transform/seasonal-difference",
              list(forecasts = c(0.5, 0.6)))
    cl <- default_client()
    expect_length(tslib_transform_seasonal_difference(cl, seq_len(24), lag = 12L), 2L)
  })
})

test_that("tslib_transform_difference_inverse restores original scale", {
  with_webmock({
    mock_post("/api/transform/difference/inverse",
              list(forecasts = c(2.0, 3.0, 4.0)))
    cl     <- default_client()
    result <- tslib_transform_difference_inverse(cl, c(1, 1), history = c(1))
    expect_equal(result, c(2.0, 3.0, 4.0))
  })
})
