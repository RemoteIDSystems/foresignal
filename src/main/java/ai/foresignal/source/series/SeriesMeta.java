package ai.foresignal.source.series;

import ai.foresignal.source.spi.Egress;
import ai.foresignal.source.spi.NormalizedRecord;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * The metadata event studies need about a series (prompt 117, Intelligence-Design §4.3 and §5.3), carried in every
 * {@code series-latest} payload and in the {@code /v1/series} catalog ({@code SeriesCatalogEntry}).
 *
 * <p>Release lags are data: {@code releaseLagDays} states the typical delay between a period and its publication.
 * The version's ingest time is its {@code knownAt}; a point's {@code observedAt} is never back-dated to its period.
 *
 * @param family what the value measures: one of {@link #FAMILIES}
 * @param transform the transform hint for analysis: one of {@link #TRANSFORMS}
 * @param releaseLagDays the typical delay between period and publication, in days (at least 0)
 * @param egress the descriptor's egress ({@code OPEN} or {@code LOCAL_ONLY}), or null when the caller fills it in
 * @param regionCode the 040 region code the series covers, or null
 * @param topicId the attention topic the series belongs to, or null
 */
public record SeriesMeta(
        String family, String transform, double releaseLagDays, String egress, String regionCode, String topicId) {

    /** Price of a traded good or instrument. */
    public static final String PRICE = "price";

    /** Traded or produced volume, stocks. */
    public static final String VOLUME = "volume";

    /** Open interest of a futures market. */
    public static final String OPEN_INTEREST = "open-interest";

    /** A trader category's position. */
    public static final String POSITION = "position";

    /** An interest or exchange rate, or a spread between rates. */
    public static final String RATE = "rate";

    /** Public attention (pageviews, article counts). */
    public static final String ATTENTION = "attention";

    /** The tone of coverage. */
    public static final String TONE = "tone";

    /** A count (demand, generation, events). */
    public static final String COUNT = "count";

    /** An index or a percentage level. */
    public static final String INDEX = "index";

    /** A slow structural covariate (population, income). */
    public static final String STRUCTURAL = "structural";

    /** Every family, as {@code SeriesCatalogEntry.family} enumerates them. */
    public static final Set<String> FAMILIES = Set.of(PRICE, VOLUME, OPEN_INTEREST, POSITION, RATE, ATTENTION, TONE,
            COUNT, INDEX, STRUCTURAL);

    /** Log returns: prices. */
    public static final String LOG_RETURN = "log-return";

    /** Log levels: counts and attention. */
    public static final String LOG_LEVEL = "log-level";

    /** First differences: positions, storage levels. */
    public static final String DIFFERENCE = "difference";

    /** The level itself. */
    public static final String LEVEL = "level";

    /** Every transform hint. */
    public static final Set<String> TRANSFORMS = Set.of(LOG_RETURN, LOG_LEVEL, DIFFERENCE, LEVEL);

    /** Validates the family, transform, lag and egress. */
    public SeriesMeta {
        if (family == null || !FAMILIES.contains(family)) {
            throw new IllegalArgumentException("Unknown series family '" + family + "'");
        }
        if (transform == null || !TRANSFORMS.contains(transform)) {
            throw new IllegalArgumentException("Unknown series transform '" + transform + "'");
        }
        if (!Double.isFinite(releaseLagDays) || releaseLagDays < 0) {
            throw new IllegalArgumentException("releaseLagDays must be a finite number at least 0");
        }
        if (egress != null && !egress.equals(Egress.OPEN.name()) && !egress.equals(Egress.LOCAL_ONLY.name())) {
            throw new IllegalArgumentException("Unknown egress '" + egress + "'");
        }
        regionCode = regionCode == null || regionCode.isBlank() ? null : regionCode;
        topicId = topicId == null || topicId.isBlank() ? null : topicId;
    }

    /** Metadata without egress, region or topic. */
    public static SeriesMeta of(String family, String transform, double releaseLagDays) {
        return new SeriesMeta(family, transform, releaseLagDays, null, null, null);
    }

    /** This metadata with {@code egress}. */
    public SeriesMeta withEgress(Egress egress) {
        return new SeriesMeta(family, transform, releaseLagDays, egress == null ? null : egress.name(), regionCode,
                topicId);
    }

    /** This metadata with {@code regionCode}. */
    public SeriesMeta withRegion(String regionCode) {
        return new SeriesMeta(family, transform, releaseLagDays, egress, regionCode, topicId);
    }

    /** This metadata with {@code topicId}. */
    public SeriesMeta withTopic(String topicId) {
        return new SeriesMeta(family, transform, releaseLagDays, egress, regionCode, topicId);
    }

    /** The payload fields this metadata adds, null values left out. */
    public Map<String, Object> fields() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("family", family);
        out.put("transform", transform);
        out.put("releaseLagDays", releaseLagDays);
        if (egress != null) {
            out.put("egress", egress);
        }
        if (regionCode != null) {
            out.put("regionCode", regionCode);
        }
        if (topicId != null) {
            out.put("topicId", topicId);
        }
        return out;
    }

    /** {@code record} with this metadata's fields added to its payload (an existing key is replaced). */
    public NormalizedRecord applyTo(NormalizedRecord record) {
        Map<String, Object> payload = new LinkedHashMap<>(record.payload());
        payload.putAll(fields());
        return new NormalizedRecord(record.externalId(), record.kind(), record.observedAt(), record.eventTime(),
                record.geometry(), record.validUntil(), record.validFrom(), payload, record.tombstone(),
                record.quality(), record.observers());
    }

    /**
     * The metadata stored in a {@code series-latest} payload, or null when the payload carries no valid family
     * and transform (a record written before prompt 117).
     */
    public static SeriesMeta fromPayload(Map<String, ?> payload) {
        if (payload == null) {
            return null;
        }
        Object family = payload.get("family");
        Object transform = payload.get("transform");
        if (!(family instanceof String f) || !FAMILIES.contains(f) || !(transform instanceof String t)
                || !TRANSFORMS.contains(t)) {
            return null;
        }
        Double lag = SeriesPoint.number(payload.get("releaseLagDays"));
        Object egress = payload.get("egress");
        String egressText = egress instanceof String e
                && (e.equals(Egress.OPEN.name()) || e.equals(Egress.LOCAL_ONLY.name())) ? e : null;
        Object region = payload.get("regionCode");
        Object topic = payload.get("topicId");
        return new SeriesMeta(f, t, lag == null || lag < 0 ? 0 : lag, egressText,
                region instanceof String r ? r : null, topic instanceof String tp ? tp : null);
    }
}
