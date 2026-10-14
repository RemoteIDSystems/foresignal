package ai.foresignal.source.adapters.acled;

import ai.foresignal.source.adapters.conflict.ConflictSources;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.Quality;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.support.MappedRecord;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import tools.jackson.databind.JsonNode;

/**
 * Maps ACLED API JSON (prompt 081): {@code /api/acled/read} rows to {@value #KIND} records and
 * {@code /api/deleted/read} rows to deletions.
 *
 * <p>The JSON envelope (ACLED endpoint guide, "Returned data – JSON only"): {@code status, success, last_update,
 * count, messages, data, filename, data_query_restrictions}; every row value is a string. A row becomes: {@code
 * externalId = event_id_cnty}, a point at {@code longitude, latitude}, {@code quality.accuracyMeters} from {@code
 * geo_precision} (1 → 2 km, 2 → 25 km, 3 → 100 km), {@code observedAt = timestamp} (epoch seconds, the last upstream
 * change), {@code eventTime = event_date} (00:00 UTC). Payload: {@code eventType, subEventType, disorderType, actor1,
 * actor2, interaction, civilianTargeting, country, admin1, admin2, location, fatalities, sources, notes} (at most
 * {@value #NOTES_MAX_BYTES} bytes, untrusted text), {@code geoPrecision, timePrecision}.
 */
public final class AcledParser {

    /** The kind. */
    public static final String KIND = "conflict-event";

    /** The largest stored {@code notes}, UTF-8 bytes. */
    public static final int NOTES_MAX_BYTES = 2048;

    /** The largest stored {@code sources}, UTF-8 bytes. */
    public static final int SOURCES_MAX_BYTES = 1024;

    /** The skip reason of a row without a usable position. */
    public static final String BAD_POSITION = "bad-position";

    /**
     * One page of events.
     *
     * @param records the events of the page
     * @param rows the rows the page held, mapped or not (a page with fewer rows than the limit is the last)
     * @param maxTimestamp the largest {@code timestamp} of the page, or null
     * @param skipped skip reason to count
     */
    public record Page(List<NormalizedRecord> records, int rows, Long maxTimestamp, Map<String, Integer> skipped) {

        /** Copies the collections. */
        public Page {
            records = List.copyOf(records);
            skipped = Map.copyOf(skipped);
        }
    }

    /**
     * One page of deletions.
     *
     * @param ids the deleted {@code event_id_cnty} values
     * @param rows the rows the page held
     * @param maxDeletedTimestamp the largest {@code deleted_timestamp}, or null
     */
    public record Deleted(List<String> ids, int rows, Long maxDeletedTimestamp) {

        /** Copies the list. */
        public Deleted {
            ids = List.copyOf(ids);
        }
    }

    private AcledParser() {}

    /** The accuracy of a {@code geo_precision} code, meters, or null for any other code. */
    public static Double accuracyMeters(Long geoPrecision) {
        if (geoPrecision == null) {
            return null;
        }
        return switch (geoPrecision.intValue()) {
            case 1 -> 2_000.0;
            case 2 -> 25_000.0;
            case 3 -> 100_000.0;
            default -> null;
        };
    }

    /**
     * Parses one page of {@code /api/acled/read}.
     *
     * @throws UpstreamFormatException when the body is not the JSON envelope with a {@code data} array, or reports
     *     {@code success: false}
     */
    public static Page events(byte[] body) {
        JsonNode data = data(body, "ACLED events");
        List<NormalizedRecord> records = new ArrayList<>();
        Map<String, Integer> skipped = new TreeMap<>();
        Long max = null;
        for (JsonNode row : data) {
            Long ts = ConflictSources.integer(row.get("timestamp"));
            if (ts != null && (max == null || ts > max)) {
                max = ts;
            }
            MappedRecord mapped = event(row);
            if (mapped.accepted()) {
                records.add(mapped.record());
            } else {
                skipped.merge(mapped.reason(), 1, Integer::sum);
            }
        }
        return new Page(records, data.size(), max, skipped);
    }

    /**
     * Parses one page of {@code /api/deleted/read}.
     *
     * @throws UpstreamFormatException when the body is not the JSON envelope with a {@code data} array
     */
    public static Deleted deleted(byte[] body) {
        JsonNode data = data(body, "ACLED deletions");
        List<String> ids = new ArrayList<>();
        Long max = null;
        for (JsonNode row : data) {
            String id = ConflictSources.text(row.get("event_id_cnty"));
            if (id != null) {
                ids.add(id);
            }
            Long ts = ConflictSources.integer(row.get("deleted_timestamp"));
            if (ts != null && (max == null || ts > max)) {
                max = ts;
            }
        }
        return new Deleted(ids, data.size(), max);
    }

    /** Maps one event row. */
    public static MappedRecord event(JsonNode row) {
        String id = ConflictSources.text(row.get("event_id_cnty"));
        if (id == null) {
            return MappedRecord.skipped(MappedRecord.MISSING_ID);
        }
        Long ts = ConflictSources.integer(row.get("timestamp"));
        if (ts == null) {
            return MappedRecord.skipped(MappedRecord.MISSING_OBSERVED_AT);
        }
        Double lat = ConflictSources.number(row.get("latitude"));
        Double lon = ConflictSources.number(row.get("longitude"));
        if (lat == null || lon == null || Math.abs(lat) > 90 || Math.abs(lon) > 180 || (lat == 0.0 && lon == 0.0)) {
            return MappedRecord.skipped(BAD_POSITION);
        }
        Long geoPrecision = ConflictSources.integer(row.get("geo_precision"));
        Long fatalities = ConflictSources.integer(row.get("fatalities"));
        NormalizedRecord record = NormalizedRecord.builder(id, KIND)
                .observedAt(Instant.ofEpochSecond(ts))
                .eventTime(ConflictSources.dayStart(ConflictSources.text(row.get("event_date"))))
                .point(lon, lat)
                .quality(new Quality(accuracyMeters(geoPrecision), null, null, Map.of()))
                .put("eventType", ConflictSources.text(row.get("event_type")))
                .put("subEventType", ConflictSources.text(row.get("sub_event_type")))
                .put("disorderType", ConflictSources.text(row.get("disorder_type")))
                .put("actor1", ConflictSources.text(row.get("actor1")))
                .put("actor2", ConflictSources.text(row.get("actor2")))
                .put("interaction", ConflictSources.text(row.get("interaction")))
                .put("civilianTargeting", ConflictSources.text(row.get("civilian_targeting")))
                .put("country", ConflictSources.text(row.get("country")))
                .put("admin1", ConflictSources.text(row.get("admin1")))
                .put("admin2", ConflictSources.text(row.get("admin2")))
                .put("location", ConflictSources.text(row.get("location")))
                .put("fatalities", fatalities)
                .put("sources", ConflictSources.truncateUtf8(ConflictSources.text(row.get("source")), SOURCES_MAX_BYTES))
                .put("notes", ConflictSources.truncateUtf8(ConflictSources.text(row.get("notes")), NOTES_MAX_BYTES))
                .put("geoPrecision", geoPrecision)
                .put("timePrecision", ConflictSources.integer(row.get("time_precision")))
                .build();
        return MappedRecord.of(record);
    }

    private static JsonNode data(byte[] body, String what) {
        JsonNode root = ConflictSources.json(body, what);
        if (!root.isObject()) {
            throw new UpstreamFormatException(what + ": not a JSON object", "");
        }
        JsonNode success = root.get("success");
        if (success != null && success.isBoolean() && !success.booleanValue()) {
            throw new UpstreamFormatException(what + ": the API reported success=false (status "
                    + ConflictSources.text(root.get("status")) + ")", "");
        }
        JsonNode data = root.get("data");
        if (data == null || !data.isArray()) {
            throw new UpstreamFormatException(what + ": no data array", "");
        }
        return data;
    }
}
