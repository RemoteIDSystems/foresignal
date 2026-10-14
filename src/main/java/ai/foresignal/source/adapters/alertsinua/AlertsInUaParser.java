package ai.foresignal.source.adapters.alertsinua;

import ai.foresignal.reference.ukr.UaRegionIndex;
import ai.foresignal.source.adapters.conflict.ConflictSources;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.TreeSet;
import tools.jackson.databind.JsonNode;

/**
 * Maps the alerts.in.ua {@code /v1/alerts/active.json} document (prompt 081).
 *
 * <p>One record per alert: {@code kind} {@value #AIR_RAID_KIND} for {@code alert_type = air_raid}, else
 * {@value #THREAT_KIND}; {@code externalId = id}; the geometry is the oblast or raion polygon of
 * {@code location_uid} from {@link UaRegionIndex} (a hromada or city of oblast significance gets its raion's polygon
 * and {@code geometryApproximate: true}; an unknown UID gets none); {@code observedAt = updated_at},
 * {@code eventTime = started_at}. An alert that arrives with {@code finished_at} is a tombstone at once.
 *
 * <p>Payload: {@code location, locationEn, locationType, oblast, raion, alertType, alertLevel, threatTypes} (the
 * distinct {@code threat_type} values, sorted), {@code notes} (at most {@value #NOTES_MAX_BYTES} bytes, untrusted text)
 * and {@code geometryApproximate}. The threats' {@code source_message} texts are dropped: they relay unverified
 * channel posts.
 */
public final class AlertsInUaParser {

    /** The kind of an air-raid alert. */
    public static final String AIR_RAID_KIND = "air-raid-alert";

    /** The kind of every other alert type (artillery shelling, urban fights, chemical, nuclear). */
    public static final String THREAT_KIND = "threat-alert";

    /** The largest stored {@code notes}, UTF-8 bytes. */
    public static final int NOTES_MAX_BYTES = 1024;

    /** The skip reason of an alert without an id. */
    public static final String MISSING_ID = "missing-id";

    /** The skip reason of an alert without {@code updated_at}. */
    public static final String MISSING_UPDATED_AT = "missing-updated-at";

    /** The skip reason of a repeated id. */
    public static final String DUPLICATE_ID = "duplicate-id";

    /**
     * One parsed document.
     *
     * @param records the alerts in force and the tombstones of finished ones, in document order
     * @param activeIds the ids of the alerts in force, by kind (the absence scopes)
     * @param skipped skip reason to count
     */
    public record Result(List<NormalizedRecord> records, Map<String, List<String>> activeIds,
            Map<String, Integer> skipped) {

        /** Copies the collections. */
        public Result {
            records = List.copyOf(records);
            activeIds = Map.copyOf(activeIds);
            skipped = Map.copyOf(skipped);
        }
    }

    private AlertsInUaParser() {}

    /**
     * Parses a document.
     *
     * @param body the response body
     * @param regions the region index
     * @param now the tombstone time of a finished alert without {@code updated_at}
     * @throws UpstreamFormatException when the body is not an object with an {@code alerts} array
     */
    public static Result parse(byte[] body, UaRegionIndex regions, Instant now) {
        JsonNode root = ConflictSources.json(body, "alerts.in.ua active alerts");
        JsonNode alerts = root.isObject() ? root.get("alerts") : null;
        if (alerts == null || !alerts.isArray()) {
            throw new UpstreamFormatException("alerts.in.ua active alerts: no alerts array", "");
        }
        List<NormalizedRecord> records = new ArrayList<>();
        Map<String, List<String>> active = new TreeMap<>();
        active.put(AIR_RAID_KIND, new ArrayList<>());
        active.put(THREAT_KIND, new ArrayList<>());
        Map<String, Integer> skipped = new TreeMap<>();
        java.util.Set<String> seen = new java.util.HashSet<>();
        for (JsonNode alert : alerts) {
            String id = ConflictSources.text(alert.get("id"));
            if (id == null) {
                skipped.merge(MISSING_ID, 1, Integer::sum);
                continue;
            }
            if (!seen.add(id)) {
                skipped.merge(DUPLICATE_ID, 1, Integer::sum);
                continue;
            }
            String kind = kindOf(ConflictSources.text(alert.get("alert_type")));
            Instant updated = ConflictSources.instant(ConflictSources.text(alert.get("updated_at")));
            if (ConflictSources.text(alert.get("finished_at")) != null) {
                records.add(NormalizedRecord.tombstone(id, kind, updated == null ? now : updated));
                continue;
            }
            if (updated == null) {
                skipped.merge(MISSING_UPDATED_AT, 1, Integer::sum);
                continue;
            }
            records.add(record(alert, id, kind, updated, regions));
            active.get(kind).add(id);
        }
        return new Result(records, active, skipped);
    }

    /** The kind of an {@code alert_type}: air raid or threat. */
    public static String kindOf(String alertType) {
        return "air_raid".equalsIgnoreCase(alertType) ? AIR_RAID_KIND : THREAT_KIND;
    }

    /** Maps one alert in force. */
    static NormalizedRecord record(JsonNode alert, String id, String kind, Instant updated, UaRegionIndex regions) {
        Optional<UaRegionIndex.Region> region = regions.byAlertsUid(ConflictSources.text(alert.get("location_uid")));
        NormalizedRecord.Builder b = NormalizedRecord.builder(id, kind)
                .observedAt(updated)
                .eventTime(ConflictSources.instant(ConflictSources.text(alert.get("started_at"))))
                .put("location", ConflictSources.text(alert.get("location_title")))
                .put("locationEn", ConflictSources.text(alert.get("location_title_en")))
                .put("locationType", lower(ConflictSources.text(alert.get("location_type"))))
                .put("oblast", ConflictSources.text(alert.get("location_oblast")))
                .put("raion", ConflictSources.text(alert.get("location_raion")))
                .put("alertType", lower(ConflictSources.text(alert.get("alert_type"))))
                .put("alertLevel", lower(ConflictSources.text(alert.get("alert_level"))))
                .put("threatTypes", threatTypes(alert.get("threats")))
                .put("notes", ConflictSources.truncateUtf8(ConflictSources.text(alert.get("notes")), NOTES_MAX_BYTES));
        region.ifPresent(r -> {
            b.geometry(r.geometry());
            if (r.approximate()) {
                b.put("geometryApproximate", true);
            }
        });
        return b.build();
    }

    /** The distinct threat types, sorted, or null when there are none. */
    static List<String> threatTypes(JsonNode threats) {
        if (threats == null || !threats.isArray()) {
            return null;
        }
        TreeSet<String> types = new TreeSet<>();
        for (JsonNode threat : threats) {
            String type = lower(ConflictSources.text(threat.get("threat_type")));
            if (type != null) {
                types.add(type);
            }
        }
        return types.isEmpty() ? null : List.copyOf(types);
    }

    private static String lower(String value) {
        return value == null ? null : value.toLowerCase(Locale.ROOT);
    }
}
