package ai.foresignal.source.spi;

/**
 * An upstream response could not be parsed.
 *
 * <p>Carries a short snippet of the offending content, which is the single most useful thing when
 * an upstream silently changes its format: it turns "the adapter broke" into "this field is now a
 * string". The snippet is capped at {@value #MAX_SNIPPET_LENGTH} characters so a 32 MB body cannot
 * end up in a log line or an exception chain.
 *
 * <p>Unchecked, unlike its parent, so parsers can stay pure functions (adapter rule 3) without
 * declaring throws on every signature. The collector treats it the same way.
 */
public class UpstreamFormatException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** The snippet cap from the prompt: enough to diagnose, small enough to log. */
    public static final int MAX_SNIPPET_LENGTH = 500;

    private final String snippet;

    public UpstreamFormatException(String message, String content) {
        this(message, content, null);
    }

    public UpstreamFormatException(String message, String content, Throwable cause) {
        super(message, cause);
        this.snippet = snippet(content);
    }

    /** Up to {@value #MAX_SNIPPET_LENGTH} characters of what failed to parse. */
    public String getSnippet() {
        return snippet;
    }

    static String snippet(String content) {
        if (content == null) {
            return "";
        }
        return content.length() <= MAX_SNIPPET_LENGTH
                ? content
                : content.substring(0, MAX_SNIPPET_LENGTH) + "… (truncated)";
    }

    @Override
    public String getMessage() {
        String message = super.getMessage();
        return snippet.isEmpty() ? message : message + " | content: " + snippet;
    }
}
