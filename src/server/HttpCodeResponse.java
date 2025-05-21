package server;

public enum HttpCodeResponse {

    OK(200, "OK"),
    MODIFIED(201, "Created or Modified"),
    NOT_FOUND(404, "Not Found"),
    NOT_ALLOWED(405, "Method Not Allowed"),
    OVERLAP(406, "Overlap Error"),
    SERVER_ERROR(500, "Internal Server Error");

    private final int code;
    private final String reason;

    HttpCodeResponse(int code, String reason) {
        this.code = code;
        this.reason = reason;
    }

    public int getCode() {
        return code;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public String toString() {
        return code + " " + reason;
    }
}