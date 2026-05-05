library(webmockr)

BASE <- "http://localhost:8080"

# Enable webmockr httr2 adapter for the duration of a test block.
# Usage:  with_webmock({ ... })
with_webmock <- function(code) {
  webmockr::enable(adapter = "httr2", quiet = TRUE)
  on.exit({
    webmockr::stub_registry_clear()
    webmockr::disable(adapter = "httr2", quiet = TRUE)
  }, add = TRUE)
  force(code)
}

# Register a POST stub that returns `body` (a plain R list) as JSON.
mock_post <- function(path, body) {
  url <- paste0(BASE, path)
  webmockr::stub_request("post", url) |>
    webmockr::to_return(
      body    = jsonlite::toJSON(body, auto_unbox = TRUE, null = "null"),
      headers = list("Content-Type" = "application/json"),
      status  = 200L
    )
}

# Register a POST stub that returns an HTTP error.
mock_post_error <- function(path, status, message) {
  url <- paste0(BASE, path)
  webmockr::stub_request("post", url) |>
    webmockr::to_return(
      body    = jsonlite::toJSON(list(message = message), auto_unbox = TRUE),
      headers = list("Content-Type" = "application/json"),
      status  = status
    )
}

default_client <- function() tslib_client(BASE)
