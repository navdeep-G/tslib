#' Create a tslib API client
#'
#' @param base_url Base URL of the tslib REST API (default: `"http://localhost:8080"`).
#' @param api_key Optional API key sent as the `X-API-Key` request header.
#' @param timeout Request timeout in seconds (default: 30).
#' @return A `tslib_client` object.
#' @export
#' @examples
#' client <- tslib_client()
#' client <- tslib_client("https://my-tslib.example.com", api_key = "secret")
tslib_client <- function(base_url = "http://localhost:8080",
                         api_key  = NULL,
                         timeout  = 30) {
  structure(
    list(
      base_url = sub("/+$", "", base_url),
      api_key  = api_key,
      timeout  = timeout
    ),
    class = "tslib_client"
  )
}

#' @export
print.tslib_client <- function(x, ...) {
  cat("<tslib_client>\n")
  cat("  base_url:", x$base_url, "\n")
  cat("  api_key: ", if (is.null(x$api_key)) "<none>" else "<set>", "\n")
  cat("  timeout: ", x$timeout, "s\n")
  invisible(x)
}

# Internal: POST a JSON body to `path` and return the parsed response list.
# Raises a condition of class "tslib_api_error" on non-2xx responses.
#' @importFrom httr2 request req_headers req_timeout req_body_json req_error
#' @importFrom httr2 req_perform resp_status resp_body_json resp_body_string
.tslib_post <- function(client, path, body) {
  req <- request(paste0(client$base_url, path))
  req <- req_headers(req,
    "Content-Type" = "application/json",
    "Accept"       = "application/json"
  )
  if (!is.null(client$api_key)) {
    req <- req_headers(req, "X-API-Key" = client$api_key)
  }
  req <- req_timeout(req, client$timeout)
  req <- req_body_json(req, body)
  req <- req_error(req, is_error = function(resp) FALSE)

  resp   <- req_perform(req)
  status <- resp_status(resp)

  if (status < 200L || status >= 300L) {
    msg <- tryCatch(
      resp_body_json(resp, simplifyVector = FALSE)[["message"]],
      error = function(e) resp_body_string(resp)
    )
    if (is.null(msg) || !nzchar(msg)) msg <- resp_body_string(resp)
    cond <- structure(
      class = c("tslib_api_error", "error", "condition"),
      list(
        message     = paste0("tslib API error [", status, "]: ", msg),
        status_code = status
      )
    )
    stop(cond)
  }

  resp_body_json(resp, simplifyVector = FALSE)
}

# Internal: drop NULL elements from a list (for optional request parameters).
.compact <- function(x) x[!vapply(x, is.null, logical(1L))]
