package ai.foresignal.sink;

import java.util.Map;

/**
 * The result of a sink test (prompt 110; OpenAPI {@code SinkTestResult}).
 *
 * @param ok true when the test succeeded
 * @param phase the last phase reached ({@code validate}, {@code connect}, {@code bind}, {@code send})
 * @param message a human message
 * @param latencyMs the latency of the send, or null
 * @param details extra details
 */
public record SinkTestResult(boolean ok, String phase, String message, Long latencyMs, Map<String, Object> details) {

    /** A failure at {@code phase}. */
    public static SinkTestResult failed(String phase, String message) {
        return new SinkTestResult(false, phase, message, null, Map.of());
    }

    /** A success at {@code phase}. */
    public static SinkTestResult ok(String phase, String message) {
        return new SinkTestResult(true, phase, message, null, Map.of());
    }
}
