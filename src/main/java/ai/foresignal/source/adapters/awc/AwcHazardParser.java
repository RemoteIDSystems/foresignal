package ai.foresignal.source.adapters.awc;

import ai.foresignal.source.spi.NormalizedRecord;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import tools.jackson.databind.JsonNode;

/**
 * Maps AWC GeoJSON hazard products to {@code aviation-hazard} records (prompt 087). Pure: the same features give the
 * same records.
 *
 * <p>Verified against live responses of 2026-10-14 (the fixtures):
 * <ul>
 *   <li><strong>{@code airsigmet}</strong> (US SIGMETs, convective SIGMETs and AIRMETs): {@code icaoId,
 *       airSigmetType, hazard, seriesId, validTimeFrom, validTimeTo} (ISO-8601), {@code severity} (a number),
 *       {@code altitudeLow1/2, altitudeHi1/2} (feet), {@code movementDir} (degrees), {@code movementSpd} (knots),
 *       {@code rawAirSigmet};
 *   <li><strong>{@code isigmet}</strong> (international SIGMETs): {@code icaoId, firId, firName, seriesId, hazard,
 *       qualifier, validTimeFrom, validTimeTo, base, top} (feet), {@code dir} (a compass point such as {@code W}),
 *       {@code spd} (knots, as text), {@code chng}, {@code rawSigmet};
 *   <li><strong>{@code gairmet}</strong> (G-AIRMET snapshots): {@code product} ({@code SIERRA, TANGO, ZULU}),
 *       {@code hazard, tag, issueTime, validTime, receiptTime, forecast} (hours), {@code severity, dueTo}, and
 *       {@code base, top} in hundreds of feet as text ({@code "090"}, or {@code "SFC"}, or {@code "FZL"} for "the
 *       freezing level"), {@code level} for the freezing-level contours; freezing-level contours are LineStrings;
 *   <li><strong>{@code cwa}</strong> (Center Weather Advisories): none were in force at capture, so the fields are
 *       those of AWC's OpenAPI {@code CwaGeoJSON}: {@code cwsu, name, validTimeFrom, validTimeTo} (epoch seconds),
 *       {@code hazard, seriesId, qualifier, base, top, cwaText}. Text times are read too.
 * </ul>
 *
 * <p>Payload {@code product, hazard, severity, qualifier, baseFt, topFt, movementDir, movementKt, raw}; null values
 * are left out. {@code raw} is upstream text: untrusted and stored as data only. A feature whose identity or time is
 * missing is skipped and counted; a repeated identity keeps its first feature.
 */
public final class AwcHazardParser {

    /** The record kind. */
    public static final String KIND = "aviation-hazard";

    /** The longest raw text kept, in characters. */
    static final int MAX_RAW = 8000;

    private static final String[] COMPASS = {"N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE", "S", "SSW", "SW",
        "WSW", "W", "WNW", "NW", "NNW"};

    private AwcHazardParser() {}

    /**
     * What one product response gave.
     *
     * @param records the records, at most one per identity
     * @param skipped features without an identity or a time
     * @param noGeometry records kept without a geometry
     */
    public record Parsed(List<NormalizedRecord> records, int skipped, int noGeometry) {

        /** Copies the list. */
        public Parsed {
            records = List.copyOf(records);
        }
    }

    /** {@code airsigmet} features: products {@code SIGMET}, {@code AIRMET} (and {@code OUTLOOK}) as published. */
    public static Parsed airSigmets(List<JsonNode> features) {
        Collector out = new Collector();
        for (JsonNode feature : features) {
            JsonNode p = feature.get("properties");
            if (p == null) {
                out.skipped++;
                continue;
            }
            Instant from = AwcSupport.instant(p.get("validTimeFrom"));
            String icao = AwcSupport.text(p.get("icaoId"));
            String series = AwcSupport.text(p.get("seriesId"));
            if (from == null || icao == null || series == null) {
                out.skipped++;
                continue;
            }
            String type = AwcSupport.text(p.get("airSigmetType"));
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("product", type == null ? "SIGMET" : type.toUpperCase(Locale.ROOT));
            payload.put("hazard", AwcSupport.text(p.get("hazard")));
            payload.put("severity", AwcSupport.text(p.get("severity")));
            payload.put("baseFt", min(AwcSupport.integer(p.get("altitudeLow1")), AwcSupport.integer(p.get("altitudeLow2"))));
            payload.put("topFt", max(AwcSupport.integer(p.get("altitudeHi1")), AwcSupport.integer(p.get("altitudeHi2"))));
            payload.put("movementDir", direction(p.get("movementDir")));
            payload.put("movementKt", AwcSupport.integer(p.get("movementSpd")));
            payload.put("raw", raw(AwcSupport.text(p.get("rawAirSigmet"))));
            out.add(feature, icao + ":" + series + ":" + from, from, from, AwcSupport.instant(p.get("validTimeTo")),
                    payload);
        }
        return out.parsed();
    }

    /** {@code isigmet} features: product {@code ISIGMET}, identity {@code firId:seriesId:validTimeFrom}. */
    public static Parsed isigmets(List<JsonNode> features) {
        Collector out = new Collector();
        for (JsonNode feature : features) {
            JsonNode p = feature.get("properties");
            if (p == null) {
                out.skipped++;
                continue;
            }
            Instant from = AwcSupport.instant(p.get("validTimeFrom"));
            String fir = AwcSupport.text(p.get("firId"));
            if (fir == null) {
                fir = AwcSupport.text(p.get("icaoId"));
            }
            String series = AwcSupport.text(p.get("seriesId"));
            if (from == null || fir == null || series == null) {
                out.skipped++;
                continue;
            }
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("product", "ISIGMET");
            payload.put("hazard", AwcSupport.text(p.get("hazard")));
            payload.put("qualifier", AwcSupport.text(p.get("qualifier")));
            payload.put("baseFt", AwcSupport.integer(p.get("base")));
            payload.put("topFt", AwcSupport.integer(p.get("top")));
            payload.put("movementDir", direction(p.get("dir")));
            payload.put("movementKt", AwcSupport.integer(p.get("spd")));
            payload.put("raw", raw(AwcSupport.text(p.get("rawSigmet"))));
            out.add(feature, fir + ":" + series + ":" + from, from, from, AwcSupport.instant(p.get("validTimeTo")),
                    payload);
        }
        return out.parsed();
    }

    /**
     * {@code gairmet} features: product {@code GAIRMET}, identity {@code product:tag:hazard:validTime:forecast}
     * ({@code SIERRA:9E:IFR:2026-10-14T06:00:00Z:3}), with {@code :level} appended for a freezing-level contour
     * ({@code ZULU:1C:FZLVL:…:3:160}): one tag carries every contour of a snapshot, and one contour can arrive in
     * several pieces, so pieces of one identity are merged into one record with a multi-part geometry.
     * {@code observedAt} = {@code issueTime}, {@code validFrom} =
     * {@code validTime} and no asserted end (the descriptor's 3-hour TTL ends it). {@code baseFt}/{@code topFt} are
     * the hundreds-of-feet levels times 100 ({@code SFC} is 0, {@code FZL} is left out); a freezing-level contour's
     * {@code level} is its {@code baseFt}. {@code qualifier} is {@code dueTo}.
     */
    public static Parsed gairmets(List<JsonNode> features) {
        Collector out = new Collector();
        for (JsonNode feature : features) {
            JsonNode p = feature.get("properties");
            if (p == null) {
                out.skipped++;
                continue;
            }
            Instant valid = AwcSupport.instant(p.get("validTime"));
            Instant issue = AwcSupport.instant(p.get("issueTime"));
            String product = AwcSupport.text(p.get("product"));
            String tag = AwcSupport.text(p.get("tag"));
            String hazard = AwcSupport.text(p.get("hazard"));
            Long forecast = AwcSupport.integer(p.get("forecast"));
            if (valid == null || product == null || tag == null || hazard == null) {
                out.skipped++;
                continue;
            }
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("product", "GAIRMET");
            payload.put("hazard", hazard);
            payload.put("severity", AwcSupport.text(p.get("severity")));
            payload.put("qualifier", AwcSupport.text(p.get("dueTo")));
            Long base = hundreds(AwcSupport.text(p.get("base")));
            if (base == null && hazard.toUpperCase(Locale.ROOT).endsWith("FZLVL")) {
                base = hundreds(AwcSupport.text(p.get("level")));
            }
            payload.put("baseFt", base);
            payload.put("topFt", hundreds(AwcSupport.text(p.get("top"))));
            String level = AwcSupport.text(p.get("level"));
            String id = product + ":" + tag + ":" + hazard + ":" + valid + ":" + (forecast == null ? 0 : forecast)
                    + (level == null ? "" : ":" + level);
            out.merge(feature, id, issue == null ? valid : issue, valid, payload);
        }
        return out.parsed();
    }

    /** {@code cwa} features: product {@code CWA}, identity {@code cwsu:seriesId:validTimeFrom}. */
    public static Parsed cwas(List<JsonNode> features) {
        Collector out = new Collector();
        for (JsonNode feature : features) {
            JsonNode p = feature.get("properties");
            if (p == null) {
                out.skipped++;
                continue;
            }
            Instant from = AwcSupport.instant(p.get("validTimeFrom"));
            String cwsu = AwcSupport.text(p.get("cwsu"));
            String series = AwcSupport.text(p.get("seriesId"));
            if (from == null || cwsu == null || series == null) {
                out.skipped++;
                continue;
            }
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("product", "CWA");
            payload.put("hazard", AwcSupport.text(p.get("hazard")));
            payload.put("qualifier", AwcSupport.text(p.get("qualifier")));
            payload.put("baseFt", AwcSupport.integer(p.get("base")));
            payload.put("topFt", AwcSupport.integer(p.get("top")));
            payload.put("raw", raw(AwcSupport.text(p.get("cwaText"))));
            out.add(feature, cwsu + ":" + series + ":" + from, from, from, AwcSupport.instant(p.get("validTimeTo")),
                    payload);
        }
        return out.parsed();
    }

    /**
     * A movement direction in degrees: a number as given, or a 16-point compass point ({@code W} is 270); null
     * otherwise.
     */
    static Integer direction(JsonNode node) {
        Long number = AwcSupport.integer(node);
        if (number != null) {
            return number.intValue();
        }
        String text = AwcSupport.text(node);
        if (text == null) {
            return null;
        }
        String upper = text.toUpperCase(Locale.ROOT);
        for (int i = 0; i < COMPASS.length; i++) {
            if (COMPASS[i].equals(upper)) {
                return (int) Math.round(i * 22.5) % 360;
            }
        }
        return null;
    }

    /** A G-AIRMET level in feet: hundreds of feet as text times 100, {@code SFC} as 0; null otherwise. */
    static Long hundreds(String text) {
        if (text == null) {
            return null;
        }
        if ("SFC".equalsIgnoreCase(text)) {
            return 0L;
        }
        if (!text.matches("^\\d{1,3}$")) {
            return null;
        }
        return Long.parseLong(text) * 100;
    }

    /** Upstream text capped at {@value #MAX_RAW} characters, trailing whitespace removed. */
    static String raw(String text) {
        if (text == null) {
            return null;
        }
        String value = text.stripTrailing();
        return value.length() > MAX_RAW ? value.substring(0, MAX_RAW) : value;
    }

    /** A geometry with nested collections flattened, so line pieces stay a MultiLineString. */
    static org.locationtech.jts.geom.Geometry flatten(org.locationtech.jts.geom.Geometry geometry) {
        List<org.locationtech.jts.geom.Geometry> parts = new ArrayList<>();
        collect(geometry, parts);
        return geometry.getFactory().buildGeometry(parts);
    }

    private static void collect(org.locationtech.jts.geom.Geometry geometry,
            List<org.locationtech.jts.geom.Geometry> out) {
        for (int i = 0; i < geometry.getNumGeometries(); i++) {
            org.locationtech.jts.geom.Geometry part = geometry.getGeometryN(i);
            if (part == geometry) {
                out.add(part);
            } else {
                collect(part, out);
            }
        }
    }

    private static Long min(Long a, Long b) {
        if (a == null) {
            return b;
        }
        return b == null ? a : Long.valueOf(Math.min(a, b));
    }

    private static Long max(Long a, Long b) {
        if (a == null) {
            return b;
        }
        return b == null ? a : Long.valueOf(Math.max(a, b));
    }

    /** Builds the records of one response, keeping the first feature of each identity. */
    private static final class Collector {
        private final Map<String, NormalizedRecord> records = new LinkedHashMap<>();
        private int skipped;
        private int noGeometry;

        void add(JsonNode feature, String id, Instant observedAt, Instant validFrom, Instant validUntil,
                Map<String, Object> payload) {
            if (records.containsKey(id)) {
                skipped++;
                return;
            }
            var geometry = AwcSupport.geometry(feature);
            if (geometry == null) {
                noGeometry++;
            }
            records.put(id, NormalizedRecord.builder(id, KIND)
                    .observedAt(observedAt)
                    .validFrom(validFrom)
                    .validUntil(validUntil != null && validFrom != null && validUntil.isBefore(validFrom) ? null
                            : validUntil)
                    .geometry(geometry)
                    .putAll(payload)
                    .build());
        }

        /**
         * Like {@link #add}, but a repeated identity adds its geometry to the record instead of being skipped (the
         * pieces of one G-AIRMET contour).
         */
        void merge(JsonNode feature, String id, Instant observedAt, Instant validFrom, Map<String, Object> payload) {
            NormalizedRecord existing = records.get(id);
            if (existing == null) {
                add(feature, id, observedAt, validFrom, null, payload);
                return;
            }
            var geometry = AwcSupport.geometry(feature);
            if (geometry == null) {
                noGeometry++;
                return;
            }
            var combined = existing.geometry() == null ? geometry
                    : geometry.getFactory().buildGeometry(List.of(existing.geometry(), geometry));
            records.put(id, NormalizedRecord.builder(id, KIND)
                    .observedAt(existing.observedAt())
                    .validFrom(existing.validFrom())
                    .geometry(flatten(combined))
                    .putAll(existing.payload())
                    .build());
        }

        Parsed parsed() {
            return new Parsed(new ArrayList<>(records.values()), skipped, noGeometry);
        }
    }
}
