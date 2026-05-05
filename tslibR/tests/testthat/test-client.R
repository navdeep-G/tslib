test_that("tslib_client stores base_url, api_key, and timeout", {
  cl <- tslib_client("http://example.com", api_key = "secret", timeout = 10)
  expect_equal(cl$base_url, "http://example.com")
  expect_equal(cl$api_key,  "secret")
  expect_equal(cl$timeout,  10)
})

test_that("tslib_client strips trailing slash from base_url", {
  cl <- tslib_client("http://example.com///")
  expect_equal(cl$base_url, "http://example.com")
})

test_that("tslib_client has class tslib_client", {
  cl <- tslib_client()
  expect_s3_class(cl, "tslib_client")
})

test_that("print.tslib_client returns client invisibly", {
  cl <- tslib_client()
  expect_output(print(cl), "tslib_client")
  expect_invisible(print(cl))
})

test_that("API error raises tslib_api_error condition", {
  with_webmock({
    mock_post_error("/api/arima/forecast", 400L, "Bad request")
    cl <- default_client()
    expect_error(
      tslib_arima_forecast(cl, c(1, 2, 3), p = 1, d = 0, q = 0),
      class = "tslib_api_error"
    )
  })
})

test_that("API 500 error surfaces status code in condition", {
  with_webmock({
    mock_post_error("/api/arima/forecast", 500L, "Internal Server Error")
    cl <- default_client()
    err <- tryCatch(
      tslib_arima_forecast(cl, c(1, 2, 3), p = 1, d = 0, q = 0),
      tslib_api_error = function(e) e
    )
    expect_equal(err$status_code, 500L)
  })
})
