package ai.foresignal.source.adapters.uasiren;

import ai.foresignal.reference.ukr.UaRegionIndex;
import ai.foresignal.source.adapters.conflict.ConflictSources;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import tools.jackson.databind.JsonNode;

/**
 * Maps the siren.pp.ua {@code /api/v3/alerts} document (prompt 081): an array of regions, each with its
 * {@code activeAlerts}.
 *
 * <p>One record per region and alert type: {@code externalId = regionId:type} of the alert (a region also lists the
 * alerts of its parents, under the parent's {@code regionId}, so the same alert can appear several times; the copy
 * with the newest {@code lastUpdate} is kept). {@code kind} {@value #AIR_RAID_KIND} for {@code AIR}, else
 * {@value #THREAT_KIND}; the geometry is the region's polygon from {@link UaRegionIndex} (a community gets its raion's
 * polygon and {@code geometryApproximate: true}); {@code observedAt = lastUpdate} of the alert,
 * {@code eventTime} = the earliest {@code createdAt} of its levels.
 *
 * <p>Payload: {@code region, regionEn, regionType, alertType, alertLevel} (the highest of
 * {@code activeAlertLevels}, lower case: {@code red} above {@code yellow}) and {@code geometryApproximate}. Some
 * regions show alerts unchanged since 2022 (occupied areas): that is normal, not stale data. The levels'
 * {@code reason} texts are not stored.
 */
public final class UaSirenParser {

    /** The kind of an air-raid alert. */
    public static final String AIR_RAID_KIND = "air-raid-alert";

    /** The kind of every other alert type (artillery, urban fights). */
    public static final String THREAT_KIND = "threat-alert";

    /** The skip reason of an alert without a region id or type. */
    public static final String MISSING_ID = "missing-id";

    /** The skip reason of an alert without {@code lastUpdate}. */
    public static final String MISSING_LAST_UPDATE = "missing-last-update";

    /**
     * One parsed document.
     *
     * @param records one record per region and type, sorted by external id
     * @param activeIds the external ids, by kind (the absence scopes)
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

    /** The names of a region as the document gives them. */
    record Names(String name, String nameEn, String type) {}

    private UaSirenParser() {}

    /**
     * Parses a document.
     *
     * @throws UpstreamFormatException when the body is not an array of regions
     */
    public static Result parse(byte[] body, UaRegionIndex regions) {
        JsonNode root = ConflictSources.json(body, "siren.pp.ua alerts");
        if (!root.isArray()) {
            throw new UpstreamFormatException("siren.pp.ua alerts: not an array of regions", "");
        }
        Map<String, Names> names = new HashMap<>();
        for (JsonNode region : root) {
            String id = ConflictSources.text(region.get("regionId"));
            if (id != null) {
                names.put(id, new Names(ConflictSources.text(region.get("regionName")),
                        ConflictSources.text(region.get("regionEngName")),
                        ConflictSources.text(region.get("regionType"))));
            }
        }
        Map<String, JsonNode> alerts = new LinkedHashMap<>();
        Map<String, Instant> updated = new HashMap<>();
        Map<String, Integer> skipped = new TreeMap<>();
        for (JsonNode region : root) {
            JsonNode active = region.get("activeAlerts");
            if (active == null || !active.isArray()) {
                continue;
            }
            for (JsonNode alert : active) {
                String regionId = ConflictSources.text(alert.get("regionId"));
                String type = ConflictSources.text(alert.get("type"));
                if (regionId == null || type == null) {
                    skipped.merge(MISSING_ID, 1, Integer::sum);
                    continue;
                }
                Instant last = ConflictSources.instant(ConflictSources.text(alert.get("lastUpdate")));
                if (last == null) {
                    skipped.merge(MISSING_LAST_UPDATE, 1, Integer::sum);
                    continue;
                }
                String externalId = regionId + ":" + type.toUpperCase(Locale.ROOT);
                Instant previous = updated.get(externalId);
                if (previous == null || last.isAfter(previous)) {
                    alerts.put(externalId, alert);
                    updated.put(externalId, last);
                }
            }
        }
        List<NormalizedRecord> records = new ArrayList<>();
        Map<String, List<String>> activeIds = new TreeMap<>();
        activeIds.put(AIR_RAID_KIND, new ArrayList<>());
        activeIds.put(THREAT_KIND, new ArrayList<>());
        for (Map.Entry<String, JsonNode> entry : new TreeMap<>(alerts).entrySet()) {
            NormalizedRecord record = record(entry.getKey(), entry.getValue(), updated.get(entry.getKey()), names,
                    regions);
            records.add(record);
            activeIds.get(record.kind()).add(record.externalId());
        }
        return new Result(records, activeIds, skipped);
    }

    /** The kind of an alert {@code type}: air raid or threat. */
    public static String kindOf(String type) {
        return "AIR".equalsIgnoreCase(type) ? AIR_RAID_KIND : THREAT_KIND;
    }

    /** The higher of two alert levels ({@code red} above {@code yellow} above anything else), lower case. */
    static String higher(String a, String b) {
        return rank(b) > rank(a) ? b : a;
    }

    private static int rank(String level) {
        if (level == null) {
            return -1;
        }
        return switch (level) {
            case "red" -> 2;
            case "yellow" -> 1;
            default -> 0;
        };
    }

    private static NormalizedRecord record(String externalId, JsonNode alert, Instant last, Map<String, Names> names,
            UaRegionIndex regions) {
        String regionId = ConflictSources.text(alert.get("regionId"));
        String type = ConflictSources.text(alert.get("type")).toUpperCase(Locale.ROOT);
        String level = null;
        Instant earliest = null;
        JsonNode levels = alert.get("activeAlertLevels");
        if (levels != null && levels.isArray()) {
            for (JsonNode l : levels) {
                String value = ConflictSources.text(l.get("alertLevel"));
                if (value != null) {
                    level = higher(level, value.toLowerCase(Locale.ROOT));
                }
                Instant created = ConflictSources.instant(ConflictSources.text(l.get("createdAt")));
                if (created != null && (earliest == null || created.isBefore(earliest))) {
                    earliest = created;
                }
            }
        }
        Names known = names.get(regionId);
        String regionType = ConflictSources.text(alert.get("regionType"));
        NormalizedRecord.Builder b = NormalizedRecord.builder(externalId, kindOf(type))
                .observedAt(last)
                .eventTime(earliest)
                .put("region", known != null && known.name() != null ? known.name()
                        : regions.sirenName(regionId).orElse(null))
                .put("regionEn", known == null ? null : known.nameEn())
                .put("regionType", regionType != null ? regionType : known == null ? null : known.type())
                .put("alertType", type)
                .put("alertLevel", level);
        regions.bySirenRegionId(regionId).ifPresent(r -> {
            b.geometry(r.geometry());
            if (r.approximate()) {
                b.put("geometryApproximate", true);
            }
        });
        return b.build();
    }
}
