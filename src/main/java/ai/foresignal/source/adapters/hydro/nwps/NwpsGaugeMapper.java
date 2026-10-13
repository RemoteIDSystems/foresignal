package ai.foresignal.source.adapters.hydro.nwps;

import ai.foresignal.source.adapters.env.EnvSources;
import ai.foresignal.source.adapters.hydro.HydroSources;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.support.MappedRecord;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * NOAA National Water Prediction Service gauges as {@code river-gauge} records, joined with each gauge's flood
 * thresholds (prompt 079). Pure.
 *
 * <p>Verified 2026-10-13. The list ({@code /nwps/v1/gauges?bbox.…}) is {@code {"gauges": [...]}} with {@code lid, name,
 * rfc{abbreviation}, wfo{abbreviation}, state{abbreviation}, latitude, longitude} and {@code status.observed} and
 * {@code status.forecast}, each {@code {primary, primaryUnit, secondary, secondaryUnit, floodCategory, validTime}}.
 * The primary value is a stage in {@code ft} and the secondary a flow in {@code kcfs}; which is which is read from the
 * unit. {@code -999} and {@code 0001-01-01T00:00:00Z} mean "none", and {@code fcst_not_current} means no forecast. The
 * detail ({@code /nwps/v1/gauges/{lid}}) adds {@code flood.categories.{action,minor,moderate,major}.{stage,flow}}
 * ({@code -9999} undefined), {@code flood.stageUnits}, {@code usgsId} (often blank) and {@code inService.enabled}.
 *
 * <ul>
 *   <li>{@code externalId = lid}, a point from {@code latitude}/{@code longitude};
 *   <li>{@code observedAt = eventTime = status.observed.validTime}; a gauge whose observation time is the sentinel
 *       is skipped ({@value MappedRecord#MISSING_OBSERVED_AT});
 *   <li>payload {@code name, rfc, wfo, state, usgsId, inService, stage, stageUnit, flow, flowUnit, floodCategory}
 *       (as published: {@code no_flooding, action, minor, moderate, major, low_threshold, not_defined,
 *       obs_not_current, out_of_service}), {@code forecastCategory, forecastStage, forecastValidTime} only when the
 *       forecast is current, {@code actionStage, minorStage, moderateStage, majorStage} from the thresholds and
 *       {@code stageToMinor} (minor − stage, when both exist in the same unit).
 * </ul>
 */
public final class NwpsGaugeMapper {

    /** The record kind. */
    public static final String KIND = "river-gauge";

    /** The forecast category meaning no current forecast. */
    public static final String FORECAST_NOT_CURRENT = "fcst_not_current";

    /** Stage units. */
    static final Set<String> STAGE_UNITS = Set.of("ft", "feet", "m", "meters");

    /** Flow units. */
    static final Set<String> FLOW_UNITS = Set.of("cfs", "kcfs", "cms", "kcms");

    private NwpsGaugeMapper() {}

    /**
     * One gauge's flood thresholds and service details, from its detail document.
     *
     * @param actionStage the action stage, or null
     * @param minorStage the minor flood stage, or null
     * @param moderateStage the moderate flood stage, or null
     * @param majorStage the major flood stage, or null
     * @param stageUnits the unit of the stages ({@code ft}), or null
     * @param usgsId the USGS site number, or null when blank
     * @param inService {@code inService.enabled}, or null
     */
    public record Thresholds(Double actionStage, Double minorStage, Double moderateStage, Double majorStage,
            String stageUnits, String usgsId, Boolean inService) {

        /** A gauge whose detail could not be read: nothing known. */
        public static final Thresholds NONE = new Thresholds(null, null, null, null, null, null, null);
    }

    /**
     * The gauges of a list document.
     *
     * @throws UpstreamFormatException when it has no {@code gauges} array
     */
    public static List<JsonNode> gauges(JsonNode root) {
        JsonNode list = root == null ? null : root.get("gauges");
        if (list == null || !list.isArray()) {
            throw new UpstreamFormatException("NWPS gauge list has no gauges array", "");
        }
        List<JsonNode> out = new ArrayList<>();
        list.forEach(out::add);
        return out;
    }

    /** The thresholds of a detail document. */
    public static Thresholds thresholds(JsonNode detail) {
        if (detail == null || !detail.isObject()) {
            return Thresholds.NONE;
        }
        JsonNode flood = detail.path("flood");
        JsonNode categories = flood.path("categories");
        JsonNode inService = detail.get("inService");
        Boolean enabled = null;
        if (inService != null && inService.isObject() && inService.get("enabled") != null
                && inService.get("enabled").isBoolean()) {
            enabled = inService.get("enabled").booleanValue();
        } else if (inService != null && inService.isBoolean()) {
            enabled = inService.booleanValue();
        }
        return new Thresholds(HydroSources.number(categories.path("action").get("stage")),
                HydroSources.number(categories.path("minor").get("stage")),
                HydroSources.number(categories.path("moderate").get("stage")),
                HydroSources.number(categories.path("major").get("stage")),
                HydroSources.text(flood.get("stageUnits")), HydroSources.text(detail.get("usgsId")), enabled);
    }

    /**
     * Maps one list entry.
     *
     * @param gauge the list entry
     * @param thresholds its thresholds, or null when unknown
     */
    public static MappedRecord gauge(JsonNode gauge, Thresholds thresholds) {
        String lid = HydroSources.text(gauge == null ? null : gauge.get("lid"));
        if (lid == null) {
            return MappedRecord.skipped(MappedRecord.MISSING_ID);
        }
        JsonNode observed = gauge.path("status").path("observed");
        Instant at = HydroSources.instant(HydroSources.text(observed.get("validTime")));
        if (at == null) {
            return MappedRecord.skipped(MappedRecord.MISSING_OBSERVED_AT);
        }
        Double lat = HydroSources.number(gauge.get("latitude"));
        Double lon = HydroSources.number(gauge.get("longitude"));
        if (!EnvSources.validPoint(lat, lon)) {
            return MappedRecord.skipped(EnvSources.MISSING_LOCATION);
        }
        Thresholds t = thresholds == null ? Thresholds.NONE : thresholds;
        NormalizedRecord.Builder b = NormalizedRecord.builder(lid.toUpperCase(Locale.ROOT), KIND)
                .observedAt(at).eventTime(at).point(lon, lat)
                .put("name", HydroSources.text(gauge.get("name")))
                .put("rfc", HydroSources.text(gauge.path("rfc").get("abbreviation")))
                .put("wfo", HydroSources.text(gauge.path("wfo").get("abbreviation")))
                .put("state", HydroSources.text(gauge.path("state").get("abbreviation")))
                .put("usgsId", t.usgsId())
                .put("inService", t.inService());
        Reading now = reading(observed);
        b.put("stage", now.stage()).put("stageUnit", now.stage() == null ? null : now.stageUnit())
                .put("flow", now.flow()).put("flowUnit", now.flow() == null ? null : now.flowUnit())
                .put("floodCategory", HydroSources.text(observed.get("floodCategory")));
        JsonNode forecast = gauge.path("status").path("forecast");
        String forecastCategory = HydroSources.text(forecast.get("floodCategory"));
        Instant forecastAt = HydroSources.instant(HydroSources.text(forecast.get("validTime")));
        if (forecastCategory != null && !FORECAST_NOT_CURRENT.equals(forecastCategory) && forecastAt != null) {
            b.put("forecastCategory", forecastCategory).put("forecastStage", reading(forecast).stage())
                    .put("forecastValidTime", forecastAt.toString());
        }
        b.put("actionStage", t.actionStage()).put("minorStage", t.minorStage())
                .put("moderateStage", t.moderateStage()).put("majorStage", t.majorStage());
        if (now.stage() != null && t.minorStage() != null
                && (t.stageUnits() == null || t.stageUnits().equalsIgnoreCase(now.stageUnit()))) {
            b.put("stageToMinor", EnvSources.round(t.minorStage() - now.stage(), 2));
        }
        return MappedRecord.of(b.build());
    }

    /** The stage and flow of one status block, sorted by unit. */
    static Reading reading(JsonNode status) {
        Double stage = null;
        Double flow = null;
        String stageUnit = null;
        String flowUnit = null;
        for (String which : List.of("primary", "secondary")) {
            Double value = HydroSources.number(status.get(which));
            String unit = HydroSources.text(status.get(which + "Unit"));
            if (value == null || unit == null) {
                continue;
            }
            String u = unit.toLowerCase(Locale.ROOT);
            if (STAGE_UNITS.contains(u) && stage == null) {
                stage = value;
                stageUnit = unit;
            } else if (FLOW_UNITS.contains(u) && flow == null) {
                flow = value;
                flowUnit = unit;
            }
        }
        return new Reading(stage, stageUnit, flow, flowUnit);
    }

    /**
     * A status block's values.
     *
     * @param stage the stage, or null
     * @param stageUnit its unit
     * @param flow the flow, or null
     * @param flowUnit its unit
     */
    record Reading(Double stage, String stageUnit, Double flow, String flowUnit) {}

    /** Thresholds as compact JSON with their read time, for the adapter state. */
    public static String toJson(Thresholds t, Instant readAt) {
        ObjectNode n = HydroSources.JSON.createObjectNode();
        n.put("at", readAt.getEpochSecond());
        putIf(n, "a", t.actionStage());
        putIf(n, "mi", t.minorStage());
        putIf(n, "mo", t.moderateStage());
        putIf(n, "ma", t.majorStage());
        if (t.stageUnits() != null) {
            n.put("u", t.stageUnits());
        }
        if (t.usgsId() != null) {
            n.put("usgs", t.usgsId());
        }
        if (t.inService() != null) {
            n.put("in", t.inService());
        }
        return n.toString();
    }

    /**
     * A cached entry written by {@link #toJson}.
     *
     * @return the entry, or null for null or damaged text
     */
    public static Cached fromJson(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            JsonNode n = HydroSources.JSON.readTree(json);
            if (n == null || !n.isObject() || n.get("at") == null || !n.get("at").canConvertToLong()) {
                return null;
            }
            JsonNode in = n.get("in");
            return new Cached(new Thresholds(HydroSources.number(n.get("a")), HydroSources.number(n.get("mi")),
                    HydroSources.number(n.get("mo")), HydroSources.number(n.get("ma")), HydroSources.text(n.get("u")),
                    HydroSources.text(n.get("usgs")), in != null && in.isBoolean() ? in.booleanValue() : null),
                    Instant.ofEpochSecond(n.get("at").longValue()));
        } catch (JacksonException e) {
            return null;
        }
    }

    /**
     * A cached threshold entry.
     *
     * @param thresholds the thresholds
     * @param readAt when the detail was read
     */
    public record Cached(Thresholds thresholds, Instant readAt) {}

    private static void putIf(ObjectNode n, String key, Double value) {
        if (value != null) {
            n.put(key, value);
        }
    }

    /** The thresholds of every cached entry, by lid. */
    static Map<String, Thresholds> thresholdsOf(Map<String, Cached> cached) {
        Map<String, Thresholds> out = new java.util.HashMap<>();
        cached.forEach((lid, c) -> out.put(lid, c.thresholds()));
        return out;
    }
}
