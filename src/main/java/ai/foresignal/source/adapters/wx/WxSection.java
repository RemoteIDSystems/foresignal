package ai.foresignal.source.adapters.wx;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * One section of a derived weather payload (prompt 052; Weather-Imagery-Design §7.1), with the
 * "unknown is not clear" rule built in.
 *
 * <ul>
 *   <li>{@link #good} stores fresh metrics: status {@code ok}, no {@code asOf}.
 *   <li>{@link #notOk} keeps the previous metrics, sets the status and its fixed explanation, and sets
 *       {@code asOf} to the time of the last good data (absent before any good data). {@code asOf} does not
 *       move while the section stays not ok.
 *   <li>Before the first good data the section is {@code {status, statusText}} only: metric keys are
 *       omitted, never null.
 * </ul>
 * Mutable; used only by one generator thread.
 */
public final class WxSection {

    /** {@code ok}. */
    public static final String OK = "ok";

    /** {@code stale}. */
    public static final String STALE = "stale";

    /** {@code no-coverage}. */
    public static final String NO_COVERAGE = "no-coverage";

    /** {@code unavailable}. */
    public static final String UNAVAILABLE = "unavailable";

    /** The fixed explanation of {@code stale}. */
    public static final String STALE_TEXT = "input is older than its maximum gap; values are the last known, not current";

    /** The fixed explanation of {@code no-coverage}. */
    public static final String NO_COVERAGE_TEXT = "outside product coverage; this is not evidence of clear weather";

    /** The fixed explanation of {@code unavailable}. */
    public static final String UNAVAILABLE_TEXT =
            "input is unavailable (disabled, failing or no data yet); this is not evidence of clear weather";

    private String status = UNAVAILABLE;
    private Instant lastGood;
    private Map<String, Object> metrics;

    /** A section that has never had data. */
    public WxSection() {}

    /** Stores fresh metrics observed at {@code at}. */
    public void good(Map<String, Object> newMetrics, Instant at) {
        this.metrics = new LinkedHashMap<>(newMetrics);
        this.lastGood = at;
        this.status = OK;
    }

    /**
     * Marks the section not ok, keeping the metrics.
     *
     * @param newStatus {@link #STALE}, {@link #NO_COVERAGE} or {@link #UNAVAILABLE}
     */
    public void notOk(String newStatus) {
        if (OK.equals(newStatus)) {
            throw new IllegalArgumentException("notOk needs a non-ok status");
        }
        this.status = newStatus;
    }

    /** The status. */
    public String status() {
        return status;
    }

    /** True when {@code ok}. */
    public boolean ok() {
        return OK.equals(status);
    }

    /** The metrics, or null before the first good data. */
    public Map<String, Object> metrics() {
        return metrics;
    }

    /** The time of the last good data, or null. */
    public Instant lastGood() {
        return lastGood;
    }

    /** One metric, or null. */
    public Object metric(String key) {
        return metrics == null ? null : metrics.get(key);
    }

    /** The section as payload JSON. */
    public Map<String, Object> toMap() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("status", status);
        if (!ok()) {
            if (lastGood != null) {
                m.put("asOf", lastGood.toString());
            }
            m.put("statusText", text(status));
        }
        if (metrics != null) {
            metrics.forEach((k, v) -> {
                if (v != null) {
                    m.put(k, v);
                }
            });
        }
        return m;
    }

    /** The fixed explanation of a non-ok status. */
    public static String text(String status) {
        return switch (status) {
            case STALE -> STALE_TEXT;
            case NO_COVERAGE -> NO_COVERAGE_TEXT;
            default -> UNAVAILABLE_TEXT;
        };
    }

    /**
     * Restores a section from a stored payload section (reconcile).
     *
     * @param stored the section map of the head's payload, or null
     * @param observedAt the head's observation time, used as the last good time of an ok section
     */
    @SuppressWarnings("unchecked")
    public static WxSection restore(Object stored, Instant observedAt) {
        WxSection s = new WxSection();
        if (!(stored instanceof Map<?, ?> raw)) {
            return s;
        }
        Map<String, Object> map = (Map<String, Object>) raw;
        Object st = map.get("status");
        s.status = st == null ? UNAVAILABLE : st.toString();
        Object asOf = map.get("asOf");
        if (asOf != null) {
            try {
                s.lastGood = Instant.parse(asOf.toString());
            } catch (RuntimeException e) {
                s.lastGood = null;
            }
        } else if (OK.equals(s.status)) {
            s.lastGood = observedAt;
        }
        Map<String, Object> m = new LinkedHashMap<>();
        map.forEach((k, v) -> {
            if (!"status".equals(k) && !"asOf".equals(k) && !"statusText".equals(k)) {
                m.put(k, v);
            }
        });
        s.metrics = m.isEmpty() ? null : m;
        return s;
    }
}
