test_that("tslib_ljung_box returns test statistic and rejection flag", {
  with_webmock({
    mock_post("/api/diagnostics/ljung-box", list(
      statistic             = 5.2,
      pValue                = 0.87,
      lags                  = 10L,
      rejectsAtFivePercent  = FALSE
    ))
    cl     <- default_client()
    result <- tslib_ljung_box(cl, c(0.1, -0.2, 0.05))
    expect_false(result$rejectsAtFivePercent)
    expect_equal(result$pValue, 0.87)
    expect_equal(result$lags,   10L)
  })
})
