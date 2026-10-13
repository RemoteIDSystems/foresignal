package ai.foresignal.source.adapters.opensky;

import ai.foresignal.source.formats.readsb.AircraftFields;
import ai.foresignal.source.formats.readsb.ReadsbAircraftMapper;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.PositionSource;
import ai.foresignal.source.spi.Quality;
import ai.foresignal.source.spi.UpstreamFormatException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Parses an OpenSky {@code /states/all?extended=1} response (prompt 062). A pure function (adapter rule 3).
 *
 * <p>The document is {@code {time, states: [[…], …] | null}}; {@code states: null} means no aircraft and
 * is valid. Each state is a positional array: {@code 0 icao24, 1 callsign, 2 origin_country,
 * 3 time_position, 4 last_contact, 5 lon, 6 lat, 7 baro_altitude (m), 8 on_ground, 9 velocity (m/s),
 * 10 true_track, 11 vertical_rate (m/s), 12 sensors, 13 geo_altitude (m), 14 squawk, 15 spi,
 * 16 position_source (0 ADS-B, 1 ASTERIX, 2 MLAT, 3 FLARM), 17 category}.
 *
 * <ul>
 *   <li><strong>Time base (Core-Data-Model §2):</strong> {@code observedAt = last_contact}, always;
 *       {@code time_position} goes to {@code quality.extra["opensky.time_position"]}.
 *   <li><strong>Payload:</strong> the readsb mapper's keys ({@link AircraftFields}): {@code hex, flight,
 *       originCountry, onGround, alt_baro, alt_geom} (meters, {@code alt_baro} omitted on the ground),
 *       {@code gs} (knots from m/s), {@code track, baro_rate, squawk, posSource}
 *       ({@code adsb|asterix|mlat|flarm}) and {@code category} (the integer as an ADS-B code; 0, 1 and 13
 *       omitted).
 *   <li><strong>Geometry:</strong> a point when lat and lon are present, else null (TRACK carry-forward).
 * </ul>
 *
 * <p>A state without {@code icao24} or {@code last_contact} is skipped and counted. Anything other than the
 * documented object throws {@link UpstreamFormatException}.
 */
public final class OpenSkyStatesParser {

    /** {@code quality.extra} key of the position's own timestamp. */
    public static final String TIME_POSITION_EXTRA = "opensky.time_position";

    /** Position source names by index. */
    static final List<String> POSITION_SOURCES = List.of("adsb", "asterix", "mlat", "flarm");

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private OpenSkyStatesParser() {}

    /** The records, the response time and how many states were skipped. */
    public record ParseResult(List<NormalizedRecord> records, Instant time, int states, int skipped) {

        /** Copies the list. */
        public ParseResult {
            records = List.copyOf(records);
        }
    }

    /** Parses a body and returns only the records. */
    public static List<NormalizedRecord> parse(byte[] body) {
        return parseWithDiagnostics(body).records();
    }

    /**
     * Parses a body.
     *
     * @throws UpstreamFormatException when it is not {@code {time, states: […] | null}}
     */
    public static ParseResult parseWithDiagnostics(byte[] body) {
        if (body == null || body.length == 0) {
            throw new UpstreamFormatException("OpenSky response is empty", "");
        }
        JsonNode root;
        try {
            root = JSON.readTree(body);
        } catch (JacksonException e) {
            throw new UpstreamFormatException("OpenSky response is not JSON: " + e.getOriginalMessage(),
                    snippet(body), e);
        }
        if (root == null || !root.isObject() || !root.has("time") || !root.get("time").isNumber()
                || !root.has("states")) {
            throw new UpstreamFormatException("OpenSky response is not {time, states}", snippet(body));
        }
        Instant time = Instant.ofEpochSecond(root.get("time").longValue());
        JsonNode states = root.get("states");
        if (states.isNull()) {
            return new ParseResult(List.of(), time, 0, 0);
        }
        if (!states.isArray()) {
            throw new UpstreamFormatException("OpenSky states is neither an array nor null", snippet(body));
        }
        List<NormalizedRecord> records = new ArrayList<>(states.size());
        int skipped = 0;
        for (JsonNode state : states) {
            if (!state.isArray()) {
                throw new UpstreamFormatException("OpenSky state is not an array", snippet(body));
            }
            NormalizedRecord record = map(state);
            if (record == null) {
                skipped++;
            } else {
                records.add(record);
            }
        }
        return new ParseResult(records, time, states.size(), skipped);
    }

    /** One state, or null when it has no {@code icao24} or {@code last_contact}. */
    static NormalizedRecord map(JsonNode s) {
        String icao = text(s.get(0));
        Double lastContact = number(s.get(4));
        if (icao == null || lastContact == null) {
            return null;
        }
        String hex = icao.toLowerCase(Locale.ROOT);
        NormalizedRecord.Builder builder = NormalizedRecord.builder(hex, AircraftFields.KIND)
                .observedAt(Instant.ofEpochSecond(lastContact.longValue()));
        Double lon = number(s.get(5));
        Double lat = number(s.get(6));
        if (ReadsbAircraftMapper.validPosition(lat, lon)) {
            builder.point(lon, lat);
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put(AircraftFields.HEX, hex);
        putText(payload, AircraftFields.FLIGHT, s.get(1));
        putText(payload, AircraftFields.ORIGIN_COUNTRY, s.get(2));
        JsonNode onGround = s.get(8);
        boolean ground = onGround != null && onGround.isBoolean() && onGround.booleanValue();
        if (onGround != null && onGround.isBoolean()) {
            payload.put(AircraftFields.ON_GROUND, ground);
        }
        Double baro = number(s.get(7));
        if (baro != null && !ground) {
            payload.put(AircraftFields.ALT_BARO, AircraftFields.wholeMeters(baro));
        }
        Double geo = number(s.get(13));
        if (geo != null) {
            payload.put(AircraftFields.ALT_GEOM, AircraftFields.wholeMeters(geo));
        }
        Double velocity = number(s.get(9));
        if (velocity != null) {
            payload.put(AircraftFields.GROUND_SPEED, AircraftFields.mpsToKnots(velocity));
        }
        Double track = number(s.get(10));
        if (track != null) {
            payload.put(AircraftFields.TRACK, AircraftFields.tenths(track));
        }
        Double rate = number(s.get(11));
        if (rate != null) {
            payload.put(AircraftFields.BARO_RATE, AircraftFields.tenths(rate));
        }
        putText(payload, AircraftFields.SQUAWK, s.get(14));
        String posSource = null;
        JsonNode source = s.get(16);
        if (source != null && source.isIntegralNumber()
                && source.intValue() >= 0 && source.intValue() < POSITION_SOURCES.size()) {
            posSource = POSITION_SOURCES.get(source.intValue());
            payload.put(AircraftFields.POS_SOURCE, posSource);
        }
        JsonNode category = s.get(17);
        if (category != null && category.isIntegralNumber()) {
            String code = AircraftFields.openSkyCategory(category.intValue());
            if (code != null) {
                payload.put(AircraftFields.CATEGORY, code);
            }
        }
        builder.putAll(payload);

        Map<String, Object> extra = new LinkedHashMap<>();
        Double timePosition = number(s.get(3));
        if (timePosition != null) {
            extra.put(TIME_POSITION_EXTRA, Instant.ofEpochSecond(timePosition.longValue()).toString());
        }
        builder.quality(new Quality(null, null, positionSource(posSource), extra));
        return builder.build();
    }

    /**
     * REPORTED for ADS-B and FLARM (the aircraft's own GNSS position), CALCULATED for MLAT, UNKNOWN for
     * ASTERIX (a ground surveillance feed of either kind) and none.
     */
    static PositionSource positionSource(String posSource) {
        if ("flarm".equals(posSource)) {
            return PositionSource.REPORTED;
        }
        if ("asterix".equals(posSource)) {
            return PositionSource.UNKNOWN;
        }
        return ReadsbAircraftMapper.positionSource(posSource);
    }

    private static void putText(Map<String, Object> payload, String key, JsonNode node) {
        String value = text(node);
        if (value != null) {
            payload.put(key, value);
        }
    }

    private static String text(JsonNode node) {
        if (node == null || !node.isString()) {
            return null;
        }
        String value = node.stringValue().trim();
        return value.isEmpty() ? null : value;
    }

    private static Double number(JsonNode node) {
        if (node == null || !node.isNumber()) {
            return null;
        }
        double value = node.doubleValue();
        return Double.isFinite(value) ? value : null;
    }

    private static String snippet(byte[] body) {
        return new String(body, 0, Math.min(body.length, 1000), StandardCharsets.UTF_8);
    }
}
