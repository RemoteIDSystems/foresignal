package ai.foresignal.source.adapters.hydro.usgswater;

import ai.foresignal.source.adapters.env.EnvSources;
import ai.foresignal.source.adapters.hydro.HydroSources;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.support.MappedRecord;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * USGS Water Data OGC API {@code latest-continuous} features as {@code water-observation} records, and the
 * {@code monitoring-locations} name table (prompt 079). Pure.
 *
 * <p>Verified 2026-10-13: each feature is a Point with properties {@code time_series_id, monitoring_location_id}
 * ({@code USGS-06710385}), {@code parameter_code, statistic_id, time, value} (<em>text</em>, {@code "8.65"}, or null),
 * {@code unit_of_measure} ({@code ft^3/s}, {@code ft}), {@code approval_status} ({@code Provisional|Approved}),
 * {@code qualifier} (null or a list such as {@code ["SEASONAL"]}) and {@code last_modified}.
 *
 * <ul>
 *   <li>{@code externalId = monitoring_location_id + ":" + parameter_code}; a repeated id keeps its newest
 *       {@code time};
 *   <li>payload {@code siteName, siteType} (from the name table), {@code parameterCode, parameterName} (from
 *       {@link #PARAMETER_NAMES}), {@code value} (a number), {@code unit} ({@code ft3/s}, {@code ft}, {@code degC}),
 *       {@code approval, qualifier} (a list joined with commas); {@code time_series_id} and {@code last_modified}
 *       are left out;
 *   <li>a non-numeric value ({@code Ice}, {@code Eqp}) is left out and added to {@code qualifier}; a null value is
 *       left out; {@code -999} and {@code -9999} are scrubbed;
 *   <li>{@code observedAt = eventTime = time}.
 * </ul>
 */
public final class UsgsLatestContinuousParser {

    /** The record kind. */
    public static final String KIND = "water-observation";

    /** The parameter names this source knows; other codes carry no {@code parameterName}. */
    public static final Map<String, String> PARAMETER_NAMES = Map.of(
            "00060", "Discharge",
            "00065", "Gage height",
            "00010", "Water temperature",
            "72019", "Depth to water");

    private UsgsLatestContinuousParser() {}

    /**
     * A monitoring location's name entry.
     *
     * @param name {@code monitoring_location_name}
     * @param siteType {@code site_type_code} ({@code ST} stream, {@code LK} lake, {@code GW} well)
     */
    public record Site(String name, String siteType) {}

    /**
     * Maps the features of every page.
     *
     * @param features the {@code latest-continuous} features
     * @param sites the name table, possibly empty
     */
    public static EnvSources.Parsed parse(List<JsonNode> features, Map<String, Site> sites) {
        EnvSources.Collector collector = new EnvSources.Collector(null, null);
        for (JsonNode feature : features) {
            collector.add(feature(feature, sites == null ? Map.of() : sites));
        }
        return collector.result();
    }

    /** One feature as a record, or the skip reason. */
    static MappedRecord feature(JsonNode feature, Map<String, Site> sites) {
        JsonNode p = feature == null ? null : feature.get("properties");
        if (p == null || !p.isObject()) {
            return MappedRecord.skipped(MappedRecord.MISSING_ID);
        }
        String site = HydroSources.text(p.get("monitoring_location_id"));
        String parameter = HydroSources.text(p.get("parameter_code"));
        if (site == null || parameter == null) {
            return MappedRecord.skipped(MappedRecord.MISSING_ID);
        }
        Instant at = HydroSources.instant(HydroSources.text(p.get("time")));
        if (at == null) {
            return MappedRecord.skipped(MappedRecord.MISSING_OBSERVED_AT);
        }
        double[] point = point(feature.get("geometry"));
        if (point == null || !EnvSources.validPoint(point[1], point[0])) {
            return MappedRecord.skipped(EnvSources.MISSING_LOCATION);
        }
        NormalizedRecord.Builder b = NormalizedRecord.builder(site + ":" + parameter, KIND)
                .observedAt(at).eventTime(at).point(point[0], point[1]);
        Site entry = sites.get(site);
        if (entry != null) {
            b.put("siteName", entry.name()).put("siteType", entry.siteType());
        }
        b.put("parameterCode", parameter).put("parameterName", PARAMETER_NAMES.get(parameter));
        List<String> qualifiers = qualifiers(p.get("qualifier"));
        JsonNode raw = p.get("value");
        String text = HydroSources.text(raw);
        if (text != null) {
            Double value = HydroSources.number(raw);
            if (value != null) {
                b.put("value", value);
            } else if (!isNumeric(text) && !qualifiers.contains(text)) {
                // "Ice", "Eqp", "Ssn": the value is a status, not a number.
                qualifiers.add(text);
            }
        }
        b.put("unit", unit(HydroSources.text(p.get("unit_of_measure"))))
                .put("approval", HydroSources.text(p.get("approval_status")))
                .put("qualifier", qualifiers.isEmpty() ? null : String.join(",", qualifiers));
        return MappedRecord.of(b.build());
    }

    /**
     * The unit as this source stores it: {@code ft^3/s} becomes {@code ft3/s}, {@code deg C} becomes {@code degC};
     * anything else is kept, trimmed.
     */
    static String unit(String unit) {
        if (unit == null) {
            return null;
        }
        String compact = unit.replace(" ", "");
        return switch (compact) {
            case "ft^3/s", "ft3/s", "cfs" -> "ft3/s";
            case "degC", "deg_C", "°C" -> "degC";
            default -> unit.strip();
        };
    }

    private static boolean isNumeric(String text) {
        try {
            new BigDecimal(text);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static List<String> qualifiers(JsonNode node) {
        List<String> out = new ArrayList<>();
        if (node == null || node.isNull()) {
            return out;
        }
        if (node.isArray()) {
            for (JsonNode q : node) {
                String text = HydroSources.text(q);
                if (text != null && !out.contains(text)) {
                    out.add(text);
                }
            }
        } else {
            String text = HydroSources.text(node);
            if (text != null) {
                out.add(text);
            }
        }
        return out;
    }

    private static double[] point(JsonNode geometry) {
        if (geometry == null || !geometry.isObject() || !"Point".equals(HydroSources.text(geometry.get("type")))) {
            return null;
        }
        JsonNode c = geometry.get("coordinates");
        if (c == null || !c.isArray() || c.size() < 2) {
            return null;
        }
        Double lon = HydroSources.number(c.get(0));
        Double lat = HydroSources.number(c.get(1));
        return lon == null || lat == null ? null : new double[] {lon, lat};
    }

    /**
     * The name table from {@code monitoring-locations} features: feature {@code id} (or {@code properties.id}) to
     * name and site type.
     */
    public static Map<String, Site> sites(List<JsonNode> features) {
        Map<String, Site> out = new TreeMap<>();
        for (JsonNode f : features) {
            JsonNode p = f.get("properties");
            String id = HydroSources.text(f.get("id"));
            if (id == null && p != null) {
                id = HydroSources.text(p.get("id"));
            }
            if (id == null || p == null || !p.isObject()) {
                continue;
            }
            String name = HydroSources.text(p.get("monitoring_location_name"));
            String type = HydroSources.text(p.get("site_type_code"));
            if (name != null || type != null) {
                out.put(id, new Site(name, type));
            }
        }
        return Collections.unmodifiableMap(out);
    }

    /** The name table as compact JSON, for the adapter state. */
    public static String sitesToJson(Map<String, Site> sites) {
        ObjectNode root = HydroSources.JSON.createObjectNode();
        sites.forEach((id, s) -> {
            ObjectNode n = root.putObject(id);
            if (s.name() != null) {
                n.put("n", s.name());
            }
            if (s.siteType() != null) {
                n.put("t", s.siteType());
            }
        });
        return root.toString();
    }

    /** The name table written by {@link #sitesToJson}; empty for null or damaged text. */
    public static Map<String, Site> sitesFromJson(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        try {
            JsonNode root = HydroSources.JSON.readTree(json);
            Map<String, Site> out = new LinkedHashMap<>();
            for (Map.Entry<String, JsonNode> e : root.properties()) {
                out.put(e.getKey(), new Site(HydroSources.text(e.getValue().get("n")),
                        HydroSources.text(e.getValue().get("t"))));
            }
            return out;
        } catch (JacksonException | UpstreamFormatException e) {
            return Map.of();
        }
    }
}
