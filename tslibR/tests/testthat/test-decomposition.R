test_that("tslib_stl returns trend, seasonal, remainder, reconstructed", {
  with_webmock({
    mock_post("/api/decompose/stl", list(
      trend         = c(1.0),
      seasonal      = c(0.1),
      remainder     = c(-0.1),
      reconstructed = c(1.0)
    ))
    cl     <- default_client()
    result <- tslib_stl(cl, seq_len(24), period = 12L)
    expect_equal(unlist(result$trend),     1.0)
    expect_equal(unlist(result$seasonal),  0.1)
    expect_equal(unlist(result$remainder), -0.1)
  })
})
