package ai.foresignal.source.adapters.space.swpc;

import ai.foresignal.source.adapters.space.SpaceSources;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.support.JsonProps;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The small SWPC JSON products of prompt 077 as records. Pure.
 *
 * <ul>
 *   <li>{@code products/noaa-scales.json} → {@code space-weather-scales}, entity {@value #SCALES_ID};
 *   <li>{@code json/planetary_k_index_1m.json} → {@code geomagnetic-index} {@value #KP_ESTIMATED_ID} (the newest
 *       one-minute row) and {@code products/noaa-planetary-k-index.json} → {@value #KP_OFFICIAL_ID} (the newest
 *       three-hour row);
 *   <li>{@code json/goes/primary/xray-flares-latest.json} → {@code solar-flare} per flare and the single
 *       {@code xray-current} entity {@value #XRAY_CURRENT_ID}.
 * </ul>
 * Timestamps without a zone are UTC.
 */
public final class SwpcProductsParser {

    /** The scales kind. */
    public static final String SCALES_KIND = "space-weather-scales";

    /** The scales entity. */
    public static final String SCALES_ID = "current";

    /** The Kp kind. */
    public static final String KP_KIND = "geomagnetic-index";

    /** The estimated (one-minute) Kp entity. */
    public static final String KP_ESTIMATED_ID = "kp-estimated";

    /** The official (three-hour) Kp entity. */
    public static final String KP_OFFICIAL_ID = "kp-official";

    /** The length of an official Kp period. */
    public static final Duration KP_PERIOD = Duration.ofHours(3);

    /** The flare kind. */
    public static final String FLARE_KIND = "solar-flare";

    /** The current X-ray kind. */
    public static final String XRAY_CURRENT_KIND = "xray-current";

    /** The current X-ray entity. */
    public static final String XRAY_CURRENT_ID = "current";

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private SwpcProductsParser() {}

    /**
     * The scales entity from {@code noaa-scales.json} ({@code {"0": today, "1".."3": days 1–3, "-1": yesterday}}).
     *
     * <p>Payload: {@code r, s, g} (current scale numbers) with {@code rText, sText, gText}, and for days 1–3
     * {@code dayNRMinorProb, dayNRMajorProb, dayNSProb} (percent) and {@code dayNGScale} (the predicted G scale).
     * {@code observedAt} is today's {@code DateStamp}+{@code TimeStamp}.
     *
     * @throws UpstreamFormatException when the body is not the scales object or today has no readable time
     */
    public static NormalizedRecord scales(byte[] body) {
        JsonNode root = read(body, "noaa-scales");
        JsonNode today = root.isObject() ? root.get("0") : null;
        if (today == null || !today.isObject()) {
            throw new UpstreamFormatException("SWPC noaa-scales has no current entry", "");
        }
        Instant observed = SpaceSources.utc(JsonProps.text(today, "DateStamp") + "T"
                + JsonProps.text(today, "TimeStamp"));
        if (observed == null) {
            throw new UpstreamFormatException("SWPC noaa-scales has no readable DateStamp and TimeStamp", "");
        }
        NormalizedRecord.Builder b = NormalizedRecord.builder(SCALES_ID, SCALES_KIND).observedAt(observed);
        for (String scale : List.of("R", "S", "G")) {
            JsonNode node = today.get(scale);
            String key = scale.toLowerCase(Locale.ROOT);
            b.put(key, integer(JsonProps.text(node, "Scale")));
            b.put(key + "Text", JsonProps.text(node, "Text"));
        }
        for (int day = 1; day <= 3; day++) {
            JsonNode d = root.get(String.valueOf(day));
            if (d == null || !d.isObject()) {
                continue;
            }
            b.put("day" + day + "RMinorProb", integer(JsonProps.text(d.get("R"), "MinorProb")));
            b.put("day" + day + "RMajorProb", integer(JsonProps.text(d.get("R"), "MajorProb")));
            b.put("day" + day + "SProb", integer(JsonProps.text(d.get("S"), "Prob")));
            b.put("day" + day + "GScale", integer(JsonProps.text(d.get("G"), "Scale")));
        }
        return b.build();
    }

    /**
     * {@value #KP_ESTIMATED_ID} from the newest row of {@code planetary_k_index_1m.json}
     * ({@code [{time_tag, kp_index, estimated_kp, kp}]}): payload {@code kp} ({@code estimated_kp}) and
     * {@code kpText} ({@code 3Z}, {@code 2P}, {@code 3M}); {@code observedAt = eventTime = time_tag}.
     *
     * @return the record, or null when the file has no usable row
     * @throws UpstreamFormatException when the body is not a JSON array
     */
    public static NormalizedRecord kpEstimated(byte[] body) {
        JsonNode newest = newest(array(read(body, "planetary_k_index_1m"), "planetary_k_index_1m"), "estimated_kp");
        if (newest == null) {
            return null;
        }
        Instant at = SpaceSources.utc(JsonProps.text(newest, "time_tag"));
        Double kp = SpaceSources.round(JsonProps.number(newest, "estimated_kp"), 2);
        String text = JsonProps.text(newest, "kp");
        return NormalizedRecord.builder(KP_ESTIMATED_ID, KP_KIND).observedAt(at).eventTime(at)
                .put("kp", kp)
                .put("kpText", text != null ? text : kpText(kp))
                .build();
    }

    /**
     * {@value #KP_OFFICIAL_ID} from the newest row of {@code noaa-planetary-k-index.json}
     * ({@code [{time_tag, Kp, a_running, station_count}]}): payload {@code kp}, {@code kpText} (derived from the
     * thirds, {@code 2.33} → {@code 2P}, {@code 2.67} → {@code 3M}), {@code aRunning}, {@code stationCount};
     * {@code eventTime = time_tag} (the period start) and {@code observedAt} the period end
     * ({@code time_tag} + 3 h): the value is measured when the period closes, and an observation time at the
     * period start would leave the record expired under its 3 h OBSERVED TTL as soon as it is published.
     *
     * @return the record, or null when the file has no usable row
     * @throws UpstreamFormatException when the body is not a JSON array
     */
    public static NormalizedRecord kpOfficial(byte[] body) {
        JsonNode newest = newest(array(read(body, "noaa-planetary-k-index"), "noaa-planetary-k-index"), "Kp");
        if (newest == null) {
            return null;
        }
        Instant start = SpaceSources.utc(JsonProps.text(newest, "time_tag"));
        Double kp = SpaceSources.round(JsonProps.number(newest, "Kp"), 2);
        return NormalizedRecord.builder(KP_OFFICIAL_ID, KP_KIND).observedAt(start.plus(KP_PERIOD)).eventTime(start)
                .put("kp", kp)
                .put("kpText", kpText(kp))
                .put("aRunning", JsonProps.integer(newest, "a_running"))
                .put("stationCount", JsonProps.integer(newest, "station_count"))
                .build();
    }

    /**
     * The SWPC thirds notation of a Kp value: {@code n.00} → {@code nZ}, {@code n.33} → {@code nP},
     * {@code n.67} → {@code (n+1)M} (as in {@code planetary_k_index_1m.json}); null for null.
     */
    static String kpText(Double kp) {
        if (kp == null) {
            return null;
        }
        long thirds = Math.round(kp * 3);
        long whole = thirds / 3;
        return switch ((int) (thirds % 3)) {
            case 0 -> whole + "Z";
            case 1 -> whole + "P";
            default -> (whole + 1) + "M";
        };
    }

    /**
     * The flare and current X-ray records of {@code xray-flares-latest.json}.
     *
     * <ul>
     *   <li>{@code solar-flare}: {@code externalId = begin_time + ":" + satellite}, payload {@code beginTime,
     *       maxTime, endTime, beginClass, maxClass, currentClass, satellite}, {@code eventTime = begin_time},
     *       {@code observedAt = time_tag};
     *   <li>{@code xray-current} ({@value #XRAY_CURRENT_ID}): {@code currentClass} and {@code currentRatio}
     *       (4 decimals) of the newest row.
     * </ul>
     *
     * @throws UpstreamFormatException when the body is not a JSON array
     */
    public static List<NormalizedRecord> xray(byte[] body) {
        JsonNode rows = array(read(body, "xray-flares-latest"), "xray-flares-latest");
        List<NormalizedRecord> out = new ArrayList<>();
        JsonNode newest = null;
        Instant newestAt = null;
        java.util.Set<String> ids = new java.util.HashSet<>();
        for (JsonNode row : rows) {
            Instant at = SpaceSources.utc(JsonProps.text(row, "time_tag"));
            Instant begin = SpaceSources.utc(JsonProps.text(row, "begin_time"));
            String satellite = JsonProps.text(row, "satellite");
            if (at == null) {
                continue;
            }
            if (newestAt == null || at.isAfter(newestAt)) {
                newest = row;
                newestAt = at;
            }
            if (begin == null || satellite == null || !ids.add(begin + ":" + satellite)) {
                continue;
            }
            out.add(NormalizedRecord.builder(begin + ":" + satellite, FLARE_KIND)
                    .observedAt(at)
                    .eventTime(begin)
                    .put("beginTime", begin.toString())
                    .put("maxTime", iso(JsonProps.text(row, "max_time")))
                    .put("endTime", iso(JsonProps.text(row, "end_time")))
                    .put("beginClass", JsonProps.text(row, "begin_class"))
                    .put("maxClass", JsonProps.text(row, "max_class"))
                    .put("currentClass", JsonProps.text(row, "current_class"))
                    .put("satellite", JsonProps.integer(row, "satellite"))
                    .build());
        }
        if (newest != null) {
            out.add(NormalizedRecord.builder(XRAY_CURRENT_ID, XRAY_CURRENT_KIND)
                    .observedAt(newestAt)
                    .put("currentClass", JsonProps.text(newest, "current_class"))
                    .put("currentRatio", SpaceSources.round(JsonProps.number(newest, "current_ratio"), 4))
                    .build());
        }
        return out;
    }

    /** An upstream time as ISO-8601 UTC text, or null. */
    static String iso(String text) {
        Instant at = SpaceSources.utc(text);
        return at == null ? null : at.toString();
    }

    /** The row with the latest {@code time_tag} that has {@code field}, or null. */
    static JsonNode newest(JsonNode rows, String field) {
        JsonNode best = null;
        Instant bestAt = null;
        for (JsonNode row : rows) {
            Instant at = SpaceSources.utc(JsonProps.text(row, "time_tag"));
            if (at != null && JsonProps.number(row, field) != null && (bestAt == null || at.isAfter(bestAt))) {
                best = row;
                bestAt = at;
            }
        }
        return best;
    }

    static JsonNode read(byte[] body, String name) {
        try {
            JsonNode root = JSON.readTree(body);
            if (root == null) {
                throw new UpstreamFormatException("SWPC " + name + " is empty", "");
            }
            return root;
        } catch (JacksonException e) {
            throw new UpstreamFormatException("SWPC " + name + " is not JSON: " + e.getOriginalMessage(), "", e);
        }
    }

    static JsonNode array(JsonNode root, String name) {
        if (!root.isArray()) {
            throw new UpstreamFormatException("SWPC " + name + " is not a JSON array", "");
        }
        return root;
    }

    private static Integer integer(String value) {
        if (value == null) {
            return null;
        }
        try {
            return Integer.valueOf(value.strip());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
