package ai.foresignal.store.query;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The answer of {@code count=true} on {@code listRecords}, {@code queryHistory} and {@code search}
 * (OpenAPI {@code CountResult}, prompt 038): how many items match, with no items.
 *
 * @param count the matches, at most {@value #CAP}
 * @param exact false when more than {@value #CAP} match ({@code count} is then {@value #CAP})
 * @param watermarkSeq the ChangeBus watermark read before counting
 */
public record CountResult(long count, boolean exact, long watermarkSeq) {

    /** The largest exact count. */
    public static final long CAP = 1_000_000L;

    /** The result of counting with {@code LIMIT CAP + 1}. */
    public static CountResult of(long counted, long watermarkSeq) {
        return of(counted, watermarkSeq, CAP);
    }

    /** The result of counting with {@code LIMIT cap + 1} (a lower cap is a test hook). */
    public static CountResult of(long counted, long watermarkSeq, long cap) {
        return counted > cap ? new CountResult(cap, false, watermarkSeq) : new CountResult(counted, true, watermarkSeq);
    }

    /** {@code SELECT count(*) FROM (<inner> LIMIT CAP + 1) c}, for an inner {@code SELECT 1 FROM …}. */
    public static String sql(String inner) {
        return sql(inner, CAP);
    }

    /** {@link #sql(String)} with another cap. */
    public static String sql(String inner, long cap) {
        return "SELECT count(*) FROM (" + inner + " LIMIT " + (cap + 1) + ") c";
    }

    /** The JSON. */
    public Map<String, Object> toMap() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("count", count);
        out.put("exact", exact);
        out.put("watermarkSeq", watermarkSeq);
        return out;
    }
}
