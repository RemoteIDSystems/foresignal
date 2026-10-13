package ai.foresignal.source.adapters.ioda;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import org.locationtech.jts.geom.Geometry;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Parses IODA {@code /v2/outages/alerts} responses and folds them into one {@code outage} state per entity and
 * datasource (prompt 068).
 *
 * <p>A pure function of its inputs (adapter rule 3): no network, no clock, no database. Geometry is resolved by
 * {@link IodaGeometry} and handed to {@link #toRecord}.
 *
 * <p><strong>Envelope</strong> (verified 2026-10-13 against the live API): a JSON object
 * <pre>{"type":"outages.alerts","metadata":{"requestTime":…,"responseTime":…},
 *  "requestParameters":{"from":"…","until":"…","datasource":null,"limit":500,"page":null,…},
 *  "error":null,"perf":null,"data":[…],
 *  "copyright":"This data is Copyright (c) 2021-2025 Georgia Tech Research Corporation. All Rights Reserved."}</pre>
 * Anything that is not an object with a {@code data} array, or that carries a non-null {@code error}, is an
 * {@link UpstreamFormatException}: an error with an empty {@code data} would otherwise read as a quiet window.
 *
 * <p><strong>Alerts</strong> (verified 2026-10-13): {@code datasource}, {@code entity{code, name, subnames, type,
 * attrs}}, {@code time} (epoch seconds), {@code level} ({@code critical} or {@code normal}; {@code warning} is
 * accepted and any other value is kept as sent), {@code condition} ({@code "< 0.99"}, or {@code "normal"} on a
 * normal alert), {@code value} and {@code historyValue} (integers in every captured alert) and {@code method}
 * ({@code median} or {@code sarima}). Entity types seen: {@code country} (ISO 3166-1 alpha-2 codes),
 * {@code region} (IODA/NetAcuity numeric codes; {@code attrs.country_code}, {@code country_name},
 * {@code ne_region_id}), {@code asn} and {@code geoasn}. Datasources seen: {@code bgp}, {@code ping-slash24},
 * {@code merit-nt}, {@code gtr}. Rows come in ascending {@code time}.
 *
 * <p><strong>Fold</strong> (Core-Data-Model rule 7, the record is the latest complete state): alerts are grouped by
 * {@code {entityType}:{entityCode}:{datasource}}, entity types and datasources outside the configured sets are
 * dropped, and per key the newest alert wins; of two with the same {@code time}, the later in the response does.
 */
public final class IodaAlertsParser {

    /** The record kind. */
    public static final String KIND = "outage";

    /** The entity types the source accepts. */
    public static final List<String> ENTITY_TYPES = List.of("country", "region", "asn");

    /** The datasources the source accepts (verified 2026-10 in the Swagger's {@code datasource} enum). */
    public static final List<String> DATASOURCES = List.of("bgp", "ping-slash24", "gtr", "merit-nt");

    /** The longest string kept, in characters. */
    static final int MAX_TEXT = 200;

    private static final JsonMapper JSON = JsonMapper.builder().build();
    private static final Pattern CONTROL = Pattern.compile("\\p{Cntrl}");
    private static final Pattern ISO2 = Pattern.compile("^[A-Z]{2}$");

    private IodaAlertsParser() {}

    /**
     * One alert as IODA sent it.
     *
     * @param datasource the datasource, lower case
     * @param entityType the entity type, lower case
     * @param entityCode the entity code as sent
     * @param entityName the entity name, sanitized, or null
     * @param attrs the entity's string attributes
     * @param time the alert time
     * @param level the level, lower case, as sent
     * @param condition the condition, or null
     * @param value the measured value, or null
     * @param historyValue the historical value, or null
     * @param method the detection method, or null
     * @param index the alert's position across every page of the run, for tie-breaking
     */
    public record Alert(String datasource, String entityType, String entityCode, String entityName,
            Map<String, String> attrs, Instant time, String level, String condition, Number value, Number historyValue,
            String method, int index) {

        /** Copies the attributes. */
        public Alert {
            attrs = attrs == null ? Map.of() : Map.copyOf(attrs);
        }

        /** {@code {entityType}:{entityCode}:{datasource}}, the record's {@code externalId}. */
        public String key() {
            return entityType + ":" + entityCode + ":" + datasource;
        }

        /** The same alert at another position (pagination renumbers each page). */
        Alert at(int newIndex) {
            return new Alert(datasource, entityType, entityCode, entityName, attrs, time, level, condition, value,
                    historyValue, method, newIndex);
        }
    }

    /**
     * One parsed page.
     *
     * @param alerts the readable alerts, in response order
     * @param rows the length of {@code data}, readable or not; a full page has {@code limit} rows
     * @param skipped rows that lacked a datasource, entity type, entity code, time or level
     */
    public record Page(List<Alert> alerts, int rows, int skipped) {

        /** Copies the alerts. */
        public Page {
            alerts = List.copyOf(alerts);
        }
    }

    /**
     * Parses one response.
     *
     * @param json the response body
     * @return the alerts and counts
     * @throws UpstreamFormatException when the body is empty, not JSON, not the IODA envelope, or carries an error
     */
    public static Page parse(byte[] json) {
        if (json == null || json.length == 0) {
            throw new UpstreamFormatException("IODA response is empty", "");
        }
        JsonNode root;
        try {
            root = JSON.readTree(json);
        } catch (JacksonException e) {
            throw new UpstreamFormatException("IODA response is not valid JSON: " + e.getOriginalMessage(),
                    snippet(json), e);
        }
        JsonNode data = root == null ? null : root.get("data");
        if (root == null || !root.isObject() || data == null || !data.isArray()) {
            throw new UpstreamFormatException("IODA response is not an object with a data array", snippet(json));
        }
        JsonNode error = root.get("error");
        if (error != null && !error.isNull() && !(error.isString() && error.asString().isBlank())) {
            String message = error.isString() ? error.asString() : error.toString();
            throw new UpstreamFormatException("IODA response carries an error: "
                    + CONTROL.matcher(message).replaceAll("").strip(), snippet(json));
        }
        List<Alert> alerts = new ArrayList<>();
        int skipped = 0;
        int index = 0;
        for (JsonNode row : data) {
            Alert alert = alert(row, index++);
            if (alert == null) {
                skipped++;
            } else {
                alerts.add(alert);
            }
        }
        return new Page(alerts, data.size(), skipped);
    }

    /**
     * Folds alerts into the newest per key, keeping only the configured entity types and datasources.
     *
     * @param alerts the alerts of every page, with run-wide indexes
     * @param entityTypes the entity types kept
     * @param datasources the datasources kept
     * @return one alert per key, in order of first appearance
     */
    public static List<Alert> fold(List<Alert> alerts, Collection<String> entityTypes, Collection<String> datasources) {
        Map<String, Alert> newest = new LinkedHashMap<>();
        for (Alert a : alerts) {
            if (!entityTypes.contains(a.entityType()) || !datasources.contains(a.datasource())) {
                continue;
            }
            Alert current = newest.get(a.key());
            if (current == null || newer(a, current)) {
                newest.put(a.key(), a);
            }
        }
        return List.copyOf(newest.values());
    }

    /** True when {@code a} replaces {@code b}: a later time, or the same time and later in the response. */
    static boolean newer(Alert a, Alert b) {
        int byTime = a.time().compareTo(b.time());
        return byTime > 0 || (byTime == 0 && a.index() > b.index());
    }

    /**
     * {@code value / historyValue} to three decimal places (half up), or null when either is missing or the
     * history is not positive.
     */
    public static Double ratio(Number value, Number historyValue) {
        if (value == null || historyValue == null || historyValue.doubleValue() <= 0) {
            return null;
        }
        return BigDecimal.valueOf(value.doubleValue())
                .divide(BigDecimal.valueOf(historyValue.doubleValue()), 3, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * The alert's country: the code of a country entity, {@code attrs.country_code} of a region, else null.
     */
    public static String country(Alert a) {
        String candidate = switch (a.entityType()) {
            case "country" -> a.entityCode();
            case "region" -> a.attrs().get("country_code");
            default -> null;
        };
        if (candidate == null) {
            return null;
        }
        String upper = candidate.strip().toUpperCase(Locale.ROOT);
        return ISO2.matcher(upper).matches() ? upper : null;
    }

    /**
     * The {@code outage} record of one folded alert.
     *
     * @param a the newest alert of its key
     * @param geometry the centroid from {@link IodaGeometry}, or null
     * @return the record: {@code observedAt} the alert time, no {@code eventTime} (a state)
     */
    public static NormalizedRecord toRecord(Alert a, Geometry geometry) {
        NormalizedRecord.Builder b = NormalizedRecord.builder(a.key(), KIND).observedAt(a.time()).geometry(geometry);
        String name = a.entityName() == null ? a.entityCode() : a.entityName();
        b.put("entityType", a.entityType());
        b.put("entityCode", a.entityCode());
        b.put("entityName", name);
        b.put("datasource", a.datasource());
        b.put("level", a.level());
        b.put("condition", a.condition());
        b.put("value", a.value());
        b.put("historyValue", a.historyValue());
        b.put("ratio", ratio(a.value(), a.historyValue()));
        b.put("method", a.method());
        b.put("summary", name + ": " + a.datasource() + " " + a.level());
        b.put("country", country(a));
        if (geometry != null) {
            b.put("geometryApproximate", true);
        }
        return b.build();
    }

    // ------------------------------------------------------------------------- reading

    private static Alert alert(JsonNode row, int index) {
        if (row == null || !row.isObject()) {
            return null;
        }
        JsonNode entity = row.get("entity");
        if (entity == null || !entity.isObject()) {
            return null;
        }
        String datasource = lower(text(row.get("datasource")));
        String type = lower(text(entity.get("type")));
        String code = text(entity.get("code"));
        String level = lower(text(row.get("level")));
        Instant time = epochSeconds(row.get("time"));
        if (datasource == null || type == null || code == null || level == null || time == null) {
            return null;
        }
        Map<String, String> attrs = new LinkedHashMap<>();
        JsonNode rawAttrs = entity.get("attrs");
        if (rawAttrs != null && rawAttrs.isObject()) {
            for (Map.Entry<String, JsonNode> e : rawAttrs.properties()) {
                String v = text(e.getValue());
                if (v != null) {
                    attrs.put(e.getKey(), v);
                }
            }
        }
        return new Alert(datasource, type, code, text(entity.get("name")), attrs, time, level,
                text(row.get("condition")), number(row.get("value")), number(row.get("historyValue")),
                text(row.get("method")), index);
    }

    private static Instant epochSeconds(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isNumber()) {
            return Instant.ofEpochSecond(node.asLong());
        }
        if (node.isString()) {
            try {
                return Instant.ofEpochSecond(Long.parseLong(node.asString().strip()));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /** A JSON number: integral values as {@link Long}, others as {@link Double}; finite only. */
    static Number number(JsonNode node) {
        if (node == null || !node.isNumber()) {
            return null;
        }
        if (node.isIntegralNumber() && node.canConvertToLong()) {
            return node.asLong();
        }
        double d = node.asDouble();
        return Double.isFinite(d) ? d : null;
    }

    private static String text(JsonNode node) {
        if (node == null || node.isNull() || node.isContainer()) {
            return null;
        }
        String raw = node.asString();
        String clean = CONTROL.matcher(raw).replaceAll("").strip();
        if (clean.isEmpty()) {
            return null;
        }
        return clean.length() > MAX_TEXT ? clean.substring(0, MAX_TEXT) : clean;
    }

    private static String lower(String s) {
        return s == null ? null : s.toLowerCase(Locale.ROOT);
    }

    private static String snippet(byte[] json) {
        int length = Math.min(json.length, UpstreamFormatException.MAX_SNIPPET_LENGTH * 4);
        return new String(json, 0, length, StandardCharsets.UTF_8);
    }
}
