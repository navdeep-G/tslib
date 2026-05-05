test_that("tslib_analyze returns statistics and transform results", {
  with_webmock({
    mock_post("/api/analyze", list(
      average           = 2.0,
      variance          = 1.0,
      standardDeviation = 1.0,
      min               = 1.0,
      max               = 3.0,
      minIndex          = 0L,
      maxIndex          = 2L,
      autocorrelation   = 0.5,
      autocovariance    = 0.5,
      acf               = c(1.0, 0.5),
      pacf              = c(1.0, 0.3),
      adfStatistic      = -2.5,
      stationary        = TRUE,
      logTransformed    = c(0.0, 0.693, 1.099),
      firstDifference   = c(1.0, 1.0),
      rollingAverage    = c(1.5, 2.0, 2.5)
    ))
    cl     <- default_client()
    result <- tslib_analyze(cl, c(1, 2, 3))
    expect_equal(result$average, 2.0)
    expect_true(result$stationary)
    expect_length(result$acf, 2L)
    expect_equal(unlist(result$firstDifference), c(1.0, 1.0))
  })
})
