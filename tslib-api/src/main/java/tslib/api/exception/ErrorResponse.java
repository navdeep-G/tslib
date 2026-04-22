package tslib.api.exception;

import java.time.Instant;

public class ErrorResponse {
    private final String error;
    private final String message;
    private final String timestamp;
    private final String path;

    public ErrorResponse(String error, String message, String path) {
        this.error = error;
        this.message = message;
        this.timestamp = Instant.now().toString();
        this.path = path;
    }

    public String getError() { return error; }
    public String getMessage() { return message; }
    public String getTimestamp() { return timestamp; }
    public String getPath() { return path; }
}
