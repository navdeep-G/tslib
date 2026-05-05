test_that("tslib_impute fills missing values", {
  with_webmock({
    mock_post("/api/dataquality/impute",
              list(forecasts = c(1.0, 1.5, 2.0)))
    cl     <- default_client()
    result <- tslib_impute(cl, c(1.0, NA, 2.0))
    expect_equal(result, c(1.0, 1.5, 2.0))
  })
})

test_that("tslib_outliers returns outlier indices", {
  with_webmock({
    mock_post("/api/dataquality/outliers",
              list(outlierIndices = c(4L)))
    cl     <- default_client()
    result <- tslib_outliers(cl, c(1, 1.1, 0.9, 1.0, 100.0))
    expect_equal(unlist(result$outlierIndices), 4L)
  })
})

test_that("tslib_winsorize clips extreme values", {
  with_webmock({
    mock_post("/api/dataquality/winsorize",
              list(forecasts = c(2.0, 2.0, 3.0, 8.0, 8.0)))
    cl     <- default_client()
    result <- tslib_winsorize(cl, c(1, 2, 3, 8, 10))
    expect_length(result, 5L)
    expect_equal(result[1L], 2.0)
  })
})
