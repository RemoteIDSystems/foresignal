package ai.foresignal.source.adapters.transit;

import ai.foresignal.source.spi.NormalizedRecord;
import com.google.transit.realtime.GtfsRealtime.Alert;
import com.google.transit.realtime.GtfsRealtime.EntitySelector;
import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.google.transit.realtime.GtfsRealtime.TimeRange;
import com.google.transit.realtime.GtfsRealtime.TranslatedString;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Maps a ServiceAlerts {@code FeedMessage} to {@code transit-alert} records (prompt 070), following the alert time
 * mapping of prompt 067.
 *
 * <ul>
 *   <li><strong>Identity and time:</strong> {@code externalId = entity.id}; {@code observedAt = header.timestamp}
 *       (an alert carries no timestamp of its own).
 *   <li><strong>Validity:</strong> {@code validFrom} and {@code eventTime} are the earliest start of
 *       {@code impact_period}, else of the deprecated {@code active_period}; null when there is no period or a period
 *       has no start (open since forever). {@code validUntil} is the latest end of those periods only when every
 *       period has an end (upstream-asserted); otherwise null.
 *   <li><strong>Payload:</strong> {@code headerText}, {@code descriptionText} and {@code url} in the translation of
 *       {@code language} (exact tag, then primary subtag, then the first translation), each at most
 *       {@value #MAX_TEXT_BYTES} bytes; {@code cause}, {@code effect}, {@code severityLevel} by proto name;
 *       {@code causeDetail}, {@code effectDetail}; {@code communicationPeriods} and {@code impactPeriods} (the impact
 *       list falls back to {@code active_period}) as ISO {@code {start, end}} pairs, at most {@value #MAX_PERIODS};
 *       {@code informed}, at most {@value #MAX_INFORMED} {@code {agencyId, routeId, routeType, stopId, tripId}}.
 *   <li><strong>No geometry:</strong> stops would need static GTFS, which is out of scope.
 *   <li><strong>Deletions:</strong> an entity with {@code is_deleted} is reported in {@link Result#deleted()}.
 * </ul>
 *
 * <p>Gtfs-realtime-bindings 0.2.0 already has {@code communication_period}, {@code impact_period},
 * {@code cause_detail}, {@code effect_detail}, {@code image} and {@code SPECIAL_EVENT}, so no unknown-field reading is
 * needed. {@code image} is not stored.
 */
public final class GtfsRtAlertParser {

    /** The record kind. */
    public static final String KIND = "transit-alert";

    /** The default translation language. */
    public static final String DEFAULT_LANGUAGE = "en";

    /** The cap on each text field, in UTF-8 bytes. */
    public static final int MAX_TEXT_BYTES = 4096;

    /** The cap on each period list. */
    public static final int MAX_PERIODS = 20;

    /** The cap on informed entities. */
    public static final int MAX_INFORMED = 50;

    /** The payload keys, in order. */
    public static final List<String> PAYLOAD_FIELDS = List.of("headerText", "descriptionText", "url", "cause", "effect",
            "severityLevel", "causeDetail", "effectDetail", "communicationPeriods", "impactPeriods", "informed");

    /**
     * What one message produced.
     *
     * @param records one record per alert entity
     * @param deleted the ids of entities marked {@code is_deleted}
     * @param fullDataset true unless the header says DIFFERENTIAL
     * @param observedAt the header timestamp, or null
     */
    public record Result(List<NormalizedRecord> records, Set<String> deleted, boolean fullDataset, Instant observedAt) {

        /** Copies the collections. */
        public Result {
            records = List.copyOf(records);
            deleted = Set.copyOf(deleted);
        }
    }

    private GtfsRtAlertParser() {}

    /**
     * Maps every alert of {@code message}.
     *
     * @param language the preferred translation, for example {@code en} or {@code fi}; blank means {@code en}
     */
    public static Result parse(FeedMessage message, String language) {
        String preferred = language == null || language.isBlank() ? DEFAULT_LANGUAGE : language.strip();
        Instant observedAt = GtfsRt.headerTime(message);
        List<NormalizedRecord> records = new ArrayList<>();
        Set<String> deleted = new LinkedHashSet<>();
        Set<String> seen = new LinkedHashSet<>();
        for (FeedEntity entity : message.getEntityList()) {
            String id = GtfsRt.bounded(entity.getId());
            if (id == null || id == GtfsRt.OVERSIZE) {
                continue;
            }
            if (entity.getIsDeleted()) {
                deleted.add(id);
                continue;
            }
            if (!entity.hasAlert() || !seen.add(id)) {
                continue;
            }
            records.add(map(id, entity.getAlert(), observedAt, preferred));
        }
        return new Result(records, deleted, GtfsRt.fullDataset(message), observedAt);
    }

    static NormalizedRecord map(String id, Alert alert, Instant observedAt, String language) {
        List<TimeRange> impact = alert.getImpactPeriodCount() > 0 ? alert.getImpactPeriodList()
                : alert.getActivePeriodList();
        Instant from = earliestStart(impact);
        Instant until = latestEnd(impact);
        NormalizedRecord.Builder builder = NormalizedRecord.builder(id, KIND)
                .observedAt(observedAt)
                .eventTime(from)
                .validFrom(from)
                .validUntil(until)
                .put("headerText", text(alert.hasHeaderText() ? alert.getHeaderText() : null, language))
                .put("descriptionText", text(alert.hasDescriptionText() ? alert.getDescriptionText() : null, language))
                .put("url", text(alert.hasUrl() ? alert.getUrl() : null, language))
                .put("cause", alert.hasCause() ? alert.getCause().name() : null)
                .put("effect", alert.hasEffect() ? alert.getEffect().name() : null)
                .put("severityLevel", alert.hasSeverityLevel() ? alert.getSeverityLevel().name() : null)
                .put("causeDetail", text(alert.hasCauseDetail() ? alert.getCauseDetail() : null, language))
                .put("effectDetail", text(alert.hasEffectDetail() ? alert.getEffectDetail() : null, language))
                .put("communicationPeriods", periods(alert.getCommunicationPeriodList()))
                .put("impactPeriods", periods(impact))
                .put("informed", informed(alert.getInformedEntityList()));
        return builder.build();
    }

    /** The earliest start, or null when there is no period or one has no start. */
    static Instant earliestStart(List<TimeRange> periods) {
        Instant earliest = null;
        for (TimeRange period : periods) {
            if (!period.hasStart() || period.getStart() == 0) {
                return null;
            }
            Instant start = Instant.ofEpochSecond(period.getStart());
            if (earliest == null || start.isBefore(earliest)) {
                earliest = start;
            }
        }
        return earliest;
    }

    /** The latest end when every period has one, else null (an open-ended period asserts no expiry). */
    static Instant latestEnd(List<TimeRange> periods) {
        Instant latest = null;
        for (TimeRange period : periods) {
            if (!period.hasEnd() || period.getEnd() == 0) {
                return null;
            }
            Instant end = Instant.ofEpochSecond(period.getEnd());
            if (latest == null || end.isAfter(latest)) {
                latest = end;
            }
        }
        return latest;
    }

    /** ISO {@code {start, end}} pairs, at most {@link #MAX_PERIODS}; null when empty. */
    static List<Map<String, Object>> periods(List<TimeRange> periods) {
        if (periods.isEmpty()) {
            return null;
        }
        List<Map<String, Object>> out = new ArrayList<>();
        for (TimeRange period : periods) {
            if (out.size() == MAX_PERIODS) {
                break;
            }
            Map<String, Object> pair = new LinkedHashMap<>();
            if (period.hasStart() && period.getStart() > 0) {
                pair.put("start", Instant.ofEpochSecond(period.getStart()).toString());
            }
            if (period.hasEnd() && period.getEnd() > 0) {
                pair.put("end", Instant.ofEpochSecond(period.getEnd()).toString());
            }
            out.add(pair);
        }
        return out;
    }

    /** At most {@link #MAX_INFORMED} selectors with their non-blank fields; null when empty. */
    static List<Map<String, Object>> informed(List<EntitySelector> selectors) {
        if (selectors.isEmpty()) {
            return null;
        }
        List<Map<String, Object>> out = new ArrayList<>();
        for (EntitySelector selector : selectors) {
            if (out.size() == MAX_INFORMED) {
                break;
            }
            Map<String, Object> entry = new LinkedHashMap<>();
            putBounded(entry, "agencyId", selector.hasAgencyId() ? selector.getAgencyId() : null);
            putBounded(entry, "routeId", selector.hasRouteId() ? selector.getRouteId() : null);
            if (selector.hasRouteType()) {
                entry.put("routeType", selector.getRouteType());
            }
            putBounded(entry, "stopId", selector.hasStopId() ? selector.getStopId() : null);
            putBounded(entry, "tripId", selector.hasTrip() && selector.getTrip().hasTripId()
                    ? selector.getTrip().getTripId() : null);
            if (!entry.isEmpty()) {
                out.add(entry);
            }
        }
        return out.isEmpty() ? null : out;
    }

    private static void putBounded(Map<String, Object> entry, String key, String value) {
        String bounded = GtfsRt.bounded(value);
        if (bounded != null && bounded != GtfsRt.OVERSIZE) {
            entry.put(key, bounded);
        }
    }

    /**
     * The translation of {@code language}: an exact tag match (case-insensitive), then the first translation whose
     * primary subtag matches, then the first translation; capped at {@link #MAX_TEXT_BYTES}. Null when there is none.
     */
    static String text(TranslatedString value, String language) {
        if (value == null || value.getTranslationCount() == 0) {
            return null;
        }
        String wanted = language.toLowerCase(Locale.ROOT);
        String primary = wanted.split("-", 2)[0];
        TranslatedString.Translation chosen = null;
        for (TranslatedString.Translation t : value.getTranslationList()) {
            if (t.hasLanguage() && t.getLanguage().strip().toLowerCase(Locale.ROOT).equals(wanted)) {
                chosen = t;
                break;
            }
        }
        if (chosen == null) {
            for (TranslatedString.Translation t : value.getTranslationList()) {
                String tag = t.hasLanguage() ? t.getLanguage().strip().toLowerCase(Locale.ROOT) : "";
                if (tag.split("-", 2)[0].equals(primary)) {
                    chosen = t;
                    break;
                }
            }
        }
        if (chosen == null) {
            chosen = value.getTranslation(0);
        }
        String text = chosen.getText().strip();
        return text.isEmpty() ? null : capBytes(text, MAX_TEXT_BYTES);
    }

    /** {@code text} cut to at most {@code maxBytes} UTF-8 bytes at a code point boundary. */
    static String capBytes(String text, int maxBytes) {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        if (bytes.length <= maxBytes) {
            return text;
        }
        int end = 0;
        int used = 0;
        while (end < text.length()) {
            int cp = text.codePointAt(end);
            int size = new String(Character.toChars(cp)).getBytes(StandardCharsets.UTF_8).length;
            if (used + size > maxBytes) {
                break;
            }
            used += size;
            end += Character.charCount(cp);
        }
        return text.substring(0, end);
    }
}
