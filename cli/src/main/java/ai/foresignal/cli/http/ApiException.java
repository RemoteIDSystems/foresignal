package ai.foresignal.cli.http;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import tools.jackson.databind.JsonNode;

/**
 * The API answered with an error status. Carries the RFC 9457 ProblemDetail fields Foresignal returns, above all
 * its stable machine {@code code}; the command exits with code 2.
 */
public final class ApiException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final int status;
    private final String code;
    private final String title;
    private final String detail;
    private final transient JsonNode problem;
    private final transient Duration retryAfter;

    /**
     * Creates the exception.
     *
     * @param status     the HTTP status
     * @param code       the ProblemDetail {@code code}, or {@code HTTP_<status>} when the body carried none
     * @param title      the ProblemDetail {@code title}, or the reason phrase
     * @param detail     the ProblemDetail {@code detail}, or null
     * @param problem    the whole problem document, or null
     * @param retryAfter the {@code Retry-After} delay, or null
     */
    public ApiException(int status, String code, String title, String detail, JsonNode problem,
            Duration retryAfter) {
        super(code + " (HTTP " + status + ")" + (title == null ? "" : ": " + title)
                + (detail == null || detail.equals(title) ? "" : ": " + detail));
        this.status = status;
        this.code = code;
        this.title = title;
        this.detail = detail;
        this.problem = problem;
        this.retryAfter = retryAfter;
    }

    /**
     * The HTTP status.
     *
     * @return e.g. 404
     */
    public int status() {
        return status;
    }

    /**
     * The ProblemDetail {@code code}.
     *
     * @return e.g. {@code NOT_FOUND}
     */
    public String code() {
        return code;
    }

    /**
     * The ProblemDetail {@code title}.
     *
     * @return the title, or null
     */
    public String title() {
        return title;
    }

    /**
     * The ProblemDetail {@code detail}.
     *
     * @return the detail, or null
     */
    public String detail() {
        return detail;
    }

    /**
     * The whole problem document.
     *
     * @return the document, or null when the body was not JSON
     */
    public JsonNode problem() {
        return problem;
    }

    /**
     * The server's {@code Retry-After}.
     *
     * @return the delay, or null
     */
    public Duration retryAfter() {
        return retryAfter;
    }

    /**
     * The field errors of a validation problem, as {@code field: message} lines.
     *
     * @return the lines, empty when there are none
     */
    public List<String> fieldErrors() {
        List<String> lines = new ArrayList<>();
        if (problem != null && problem.path("errors").isArray()) {
            for (JsonNode error : problem.path("errors")) {
                String field = error.path("field").asString("");
                String message = error.path("message").asString("");
                lines.add(field.isEmpty() ? message : field + ": " + message);
            }
        }
        return lines;
    }
}
