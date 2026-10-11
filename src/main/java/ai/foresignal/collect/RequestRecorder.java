package ai.foresignal.collect;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Captures the exchanges of one probe run.
 *
 * <p>Created per probe and discarded with it, so nothing accumulates: the probe feature in prompt
 * 008 shows an operator what an upstream actually returned, which is the fastest way to diagnose an
 * adapter that broke because a feed changed shape.
 *
 * <p>Bodies are capped so probing a 32 MB feed does not build a 32 MB response.
 */
public class RequestRecorder {

    /** Enough of a response to see its shape. */
    public static final int DEFAULT_BODY_CAP = 64 * 1024;

    /** Appended to a body cut at the cap. */
    public static final String TRUNCATION_MARKER = "… (truncated)";

    /** More exchanges than any single probe should make. */
    public static final int DEFAULT_MAX_EXCHANGES = 20;

    private final int bodyCap;
    private final int maxExchanges;
    private final List<RecordedExchange> exchanges = new CopyOnWriteArrayList<>();

    public RequestRecorder() {
        this(DEFAULT_BODY_CAP, DEFAULT_MAX_EXCHANGES);
    }

    public RequestRecorder(int bodyCap, int maxExchanges) {
        this.bodyCap = bodyCap;
        this.maxExchanges = maxExchanges;
    }

    /** How much of each body is kept. */
    public int bodyCap() {
        return bodyCap;
    }

    /** Records one exchange, unless the recorder is already full. */
    public void record(RecordedExchange exchange) {
        if (exchanges.size() >= maxExchanges) {
            return;
        }
        exchanges.add(exchange);
    }

    /** The exchanges captured, in order. */
    public List<RecordedExchange> exchanges() {
        return List.copyOf(exchanges);
    }

    /** True when no more exchanges will be recorded. */
    public boolean isFull() {
        return exchanges.size() >= maxExchanges;
    }

    /** Truncates {@code body} to the recorder's cap. */
    public String capBody(String body) {
        if (body == null) {
            return null;
        }
        return body.length() <= bodyCap ? body : body.substring(0, bodyCap) + TRUNCATION_MARKER;
    }
}
