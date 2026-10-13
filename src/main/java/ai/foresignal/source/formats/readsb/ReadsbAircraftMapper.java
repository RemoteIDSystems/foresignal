/*
 * The readsb field mapping is ported in part from God's Eye View, src/data/adsbLolFallback.js
 * (normalizeAdsbLolAircraftState), https://github.com/bilawalsidhu/gods-eye-view,
 * commit 591f299d11f38a612629a274463196d57ae3862e.
 * Copyright (c) 2026 Bilawal Sidhu. MIT License; the full notice is in THIRD_PARTY_NOTICES.md.
 * Changes: Java; a Foresignal record instead of an OpenSky state vector; observedAt is always
 * now − seen (Core-Data-Model §2); lastPosition is used when lat/lon are absent instead of dropping the
 * entry; an allowlisted payload in meters with posSource, navDegraded and the dbFlags bits.
 */
package ai.foresignal.source.formats.readsb;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.PositionSource;
import ai.foresignal.source.spi.Quality;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import tools.jackson.databind.JsonNode;

/**
 * Maps one readsb aircraft entry to one record (prompt 062; shared by adsb.lol, local receivers in 069 and
 * the feeder sources of 080).
 *
 * <p>A pure function (adapter rule 3): the snapshot time comes from the document, never from a clock.
 *
 * <ul>
 *   <li><strong>Identity:</strong> {@code externalId} is {@code hex} lower-cased, keeping a leading
 *       {@code ~} (a non-ICAO address). An entry without {@code hex} is skipped.
 *   <li><strong>Time base (Core-Data-Model §2, binding):</strong> {@code observedAt = now − seen}, always;
 *       {@code eventTime} is null. {@code seen_pos} (the position's age) goes to
 *       {@code quality.extra["adsb.seen_pos"]}.
 *   <li><strong>Geometry:</strong> a 2D point from {@code lat}/{@code lon}; else from
 *       {@code lastPosition} with {@code quality.extra["adsb.last_position"] = true}; else null, and the
 *       TRACK carry-forward keeps the previous position (Core-Data-Model §3). Altitude never goes in Z.
 *   <li><strong>Payload</strong> (rule 8, absent keys omitted, no nulls): {@code hex, flight, r, t, desc,
 *       squawk, emergency} (omitted when {@code none}), {@code category, onGround, alt_baro} and
 *       {@code alt_geom} in meters ({@code alt_baro} omitted on the ground), {@code gs} (knots),
 *       {@code track} (degrees), {@code baro_rate} (m/s), {@code posSource, nac_p, nic, navDegraded} and
 *       {@code mil, pia, ladd} from {@code dbFlags} (only when true). Raw counters and query-relative values
 *       ({@code messages, rssi, seen, seen_pos, rc, version, sil, gva, sda, nav_*}) are left out.
 *   <li><strong>Quality:</strong> {@code positionSource} REPORTED for ADS-B and ADS-R, CALCULATED for MLAT
 *       and TIS-B, UNKNOWN otherwise.
 * </ul>
 */
public final class ReadsbAircraftMapper {

    /** {@code quality.extra} key of the position's age in seconds. */
    public static final String SEEN_POS_EXTRA = "adsb.seen_pos";

    /** {@code quality.extra} key set when the geometry came from {@code lastPosition}. */
    public static final String LAST_POSITION_EXTRA = "adsb.last_position";

    /** {@code dbFlags} bit: military. */
    public static final int DB_FLAG_MILITARY = 1;
    /** {@code dbFlags} bit: privacy ICAO address. */
    public static final int DB_FLAG_PIA = 4;
    /** {@code dbFlags} bit: FAA LADD. */
    public static final int DB_FLAG_LADD = 8;

    /** {@code posSource} values with a reported position. */
    static final Set<String> REPORTED = Set.of("adsb", "adsr");

    /** {@code posSource} values with a calculated position. */
    static final Set<String> CALCULATED = Set.of("mlat", "tisb");

    private ReadsbAircraftMapper() {}

    /** The records of a document and how many entries were skipped (no {@code hex}). */
    public record MapResult(List<NormalizedRecord> records, int skipped) {

        /** Copies the list. */
        public MapResult {
            records = List.copyOf(records);
        }
    }

    /** Maps every entry of {@code document}. */
    public static MapResult mapAll(ReadsbDocument document, MapperOptions opts) {
        List<NormalizedRecord> records = new ArrayList<>(document.aircraft().size());
        int skipped = 0;
        for (JsonNode entry : document.aircraft()) {
            Optional<NormalizedRecord> record = map(entry, document.now(), opts);
            if (record.isPresent()) {
                records.add(record.get());
            } else {
                skipped++;
            }
        }
        return new MapResult(records, skipped);
    }

    /**
     * Maps one entry.
     *
     * @param ac one element of {@code ac} or {@code aircraft}
     * @param upstreamNow the document's {@code now}
     * @param opts the source's options; null means {@link MapperOptions#DEFAULTS}
     * @return the record, or empty when the entry has no {@code hex}
     */
    public static Optional<NormalizedRecord> map(JsonNode ac, Instant upstreamNow, MapperOptions opts) {
        MapperOptions options = opts == null ? MapperOptions.DEFAULTS : opts;
        if (ac == null || !ac.isObject() || upstreamNow == null) {
            return Optional.empty();
        }
        String hex = text(ac.get("hex"));
        if (hex == null) {
            return Optional.empty();
        }
        hex = hex.toLowerCase(Locale.ROOT);

        NormalizedRecord.Builder builder = NormalizedRecord.builder(hex, options.kind())
                .observedAt(observedAt(upstreamNow, number(ac.get("seen"))));

        Map<String, Object> extra = new LinkedHashMap<>();
        Double lat = number(ac.get("lat"));
        Double lon = number(ac.get("lon"));
        JsonNode last = ac.get("lastPosition");
        if (validPosition(lat, lon)) {
            builder.point(lon, lat);
        } else if (options.lastPositionFallback() && last != null && last.isObject()
                && validPosition(number(last.get("lat")), number(last.get("lon")))) {
            builder.point(number(last.get("lon")), number(last.get("lat")));
            extra.put(LAST_POSITION_EXTRA, true);
        }
        Double seenPos = number(ac.get("seen_pos"));
        if (seenPos == null && last != null && last.isObject() && extra.containsKey(LAST_POSITION_EXTRA)) {
            seenPos = number(last.get("seen_pos"));
        }
        if (seenPos != null) {
            extra.put(SEEN_POS_EXTRA, seenPos);
        }

        Map<String, Object> payload = payload(ac, hex);
        builder.putAll(payload);
        String posSource = (String) payload.get(AircraftFields.POS_SOURCE);
        builder.quality(new Quality(null, null, positionSource(posSource), extra));
        return Optional.of(builder.build());
    }

    /**
     * True when both coordinates are present and in range (adapter rule 11: a sentinel or out-of-range
     * value is scrubbed to "no position" rather than failing the whole fetch).
     */
    public static boolean validPosition(Double lat, Double lon) {
        return lat != null && lon != null && lat >= -90.0 && lat <= 90.0 && lon >= -180.0 && lon <= 180.0;
    }

    /** {@code now − seen}; {@code seen} absent or negative counts as 0. */
    static Instant observedAt(Instant now, Double seen) {
        if (seen == null || !(seen > 0)) {
            return now;
        }
        return now.minus(Duration.ofMillis(Math.round(seen * 1000.0)));
    }

    /** The allowlisted payload of one entry, in emission order. */
    static Map<String, Object> payload(JsonNode ac, String hex) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put(AircraftFields.HEX, hex);
        putText(out, AircraftFields.FLIGHT, ac.get("flight"));
        putText(out, AircraftFields.REGISTRATION, ac.get("r"));
        putText(out, AircraftFields.TYPE, ac.get("t"));
        putText(out, AircraftFields.DESCRIPTION, ac.get("desc"));
        putText(out, AircraftFields.SQUAWK, ac.get("squawk"));
        String emergency = text(ac.get("emergency"));
        if (emergency != null && !"none".equalsIgnoreCase(emergency)) {
            out.put(AircraftFields.EMERGENCY, emergency);
        }
        putText(out, AircraftFields.CATEGORY, ac.get("category"));

        JsonNode altBaro = ac.get("alt_baro");
        if (altBaro != null && altBaro.isString() && "ground".equalsIgnoreCase(altBaro.stringValue().trim())) {
            out.put(AircraftFields.ON_GROUND, true);
        } else if (altBaro != null && altBaro.isNumber()) {
            out.put(AircraftFields.ON_GROUND, false);
            out.put(AircraftFields.ALT_BARO, AircraftFields.feetToMeters(altBaro.doubleValue()));
        }
        Double altGeom = number(ac.get("alt_geom"));
        if (altGeom != null) {
            out.put(AircraftFields.ALT_GEOM, AircraftFields.feetToMeters(altGeom));
        }
        Double gs = number(ac.get("gs"));
        if (gs != null) {
            out.put(AircraftFields.GROUND_SPEED, AircraftFields.tenths(gs));
        }
        Double track = number(ac.get("track"));
        if (track != null) {
            out.put(AircraftFields.TRACK, AircraftFields.tenths(track));
        }
        Double baroRate = number(ac.get("baro_rate"));
        if (baroRate != null) {
            out.put(AircraftFields.BARO_RATE, AircraftFields.fpmToMps(baroRate));
        }
        String posSource = posSource(ac);
        if (posSource != null) {
            out.put(AircraftFields.POS_SOURCE, posSource);
        }
        Integer nacP = integer(ac.get("nac_p"));
        Integer nic = integer(ac.get("nic"));
        if (nacP != null) {
            out.put(AircraftFields.NAC_P, nacP);
        }
        if (nic != null) {
            out.put(AircraftFields.NIC, nic);
        }
        Boolean degraded = AircraftFields.navDegraded(nacP, nic);
        if (degraded != null) {
            out.put(AircraftFields.NAV_DEGRADED, degraded);
        }
        Integer flags = integer(ac.get("dbFlags"));
        if (flags != null) {
            if ((flags & DB_FLAG_MILITARY) != 0) {
                out.put(AircraftFields.MILITARY, true);
            }
            if ((flags & DB_FLAG_PIA) != 0) {
                out.put(AircraftFields.PIA, true);
            }
            if ((flags & DB_FLAG_LADD) != 0) {
                out.put(AircraftFields.LADD, true);
            }
        }
        return out;
    }

    /**
     * {@code posSource}: the {@code mlat} or {@code tisb} field list naming {@code lat} wins (the position
     * itself came from there); otherwise the readsb {@code type}. Null when the entry says neither.
     */
    static String posSource(JsonNode ac) {
        if (listsPosition(ac.get("mlat"))) {
            return "mlat";
        }
        if (listsPosition(ac.get("tisb"))) {
            return "tisb";
        }
        String type = text(ac.get("type"));
        if (type == null) {
            return null;
        }
        String t = type.toLowerCase(Locale.ROOT);
        if (t.startsWith("adsb")) {
            return "adsb";
        }
        if (t.startsWith("adsr")) {
            return "adsr";
        }
        if (t.startsWith("mlat")) {
            return "mlat";
        }
        if (t.startsWith("tisb")) {
            return "tisb";
        }
        if (t.equals("mode_s")) {
            return "modes";
        }
        return "other";
    }

    /** {@link Quality#positionSource()} for a {@code posSource} value. */
    public static PositionSource positionSource(String posSource) {
        if (posSource == null) {
            return PositionSource.UNKNOWN;
        }
        if (REPORTED.contains(posSource)) {
            return PositionSource.REPORTED;
        }
        return CALCULATED.contains(posSource) ? PositionSource.CALCULATED : PositionSource.UNKNOWN;
    }

    private static boolean listsPosition(JsonNode list) {
        if (list == null || !list.isArray()) {
            return false;
        }
        for (JsonNode field : list) {
            if (field.isString() && "lat".equals(field.stringValue())) {
                return true;
            }
        }
        return false;
    }

    private static void putText(Map<String, Object> out, String key, JsonNode node) {
        String value = text(node);
        if (value != null) {
            out.put(key, value);
        }
    }

    /** A trimmed, non-blank string value, or null. */
    static String text(JsonNode node) {
        if (node == null || !node.isString()) {
            return null;
        }
        String value = node.stringValue().trim();
        return value.isEmpty() ? null : value;
    }

    /** A finite number, or null. */
    static Double number(JsonNode node) {
        if (node == null || !node.isNumber()) {
            return null;
        }
        double value = node.doubleValue();
        return Double.isFinite(value) ? value : null;
    }

    /** An integral number that fits an int, or null. */
    static Integer integer(JsonNode node) {
        if (node == null || !node.isNumber()) {
            return null;
        }
        double value = node.doubleValue();
        if (!Double.isFinite(value) || value != Math.rint(value) || Math.abs(value) > Integer.MAX_VALUE) {
            return null;
        }
        return (int) value;
    }
}
