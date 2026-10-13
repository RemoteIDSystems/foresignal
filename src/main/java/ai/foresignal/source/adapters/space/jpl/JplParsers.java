package ai.foresignal.source.adapters.space.jpl;

import ai.foresignal.source.adapters.space.SpaceSources;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.support.MappedRecord;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The JPL SSD APIs of prompt 077 as records. Pure. Both answer {@code {signature: {version}, count, fields: [...],
 * data: [[...], ...]}} with values as strings (or null); columns are found by name.
 *
 * <ul>
 *   <li><strong>Fireballs</strong> ({@code fireball.api}, signature 1.2, fields {@code date, energy, impact-e, lat,
 *       lat-dir, lon, lon-dir, alt, vel}): kind {@value #FIREBALL_KIND}, {@code externalId = date}, a point from
 *       {@code lat}/{@code lon} signed by {@code lat-dir} (S negative) and {@code lon-dir} (W negative), payload
 *       {@code radiatedEnergyJ} ({@code energy × 1e10}), {@code impactEnergyKt, altitudeKm, velocityKms};
 *       {@code eventTime = date} (UTC).
 *   <li><strong>Close approaches</strong> ({@code cad.api}, signature 1.5, fields {@code des, orbit_id, jd, cd,
 *       dist, dist_min, dist_max, v_rel, v_inf, t_sigma_f, h} and {@code fullname}): kind
 *       {@value #CLOSE_APPROACH_KIND}, no geometry, {@code externalId = des + "@" + cd}, payload {@code designation,
 *       fullName, distAu, distLunar} ({@code dist × 389.17}, 2 decimals), {@code distMinAu, distMaxAu, vRelKms,
 *       absMagnitude, orbitId, timeSigma}; {@code eventTime = cd}, a TDB time read as UTC (TDB runs about 69 s
 *       ahead of UTC).
 * </ul>
 * Both sources are {@code observedAtMode=RECEIVED} (rows are revised without a revision stamp), so records carry
 * no {@code observedAt}.
 */
public final class JplParsers {

    /** The fireball kind. */
    public static final String FIREBALL_KIND = "fireball";

    /** The close-approach kind. */
    public static final String CLOSE_APPROACH_KIND = "close-approach";

    /** Lunar distances per astronomical unit. */
    public static final double LUNAR_DISTANCES_PER_AU = 389.17;

    /** The fireball {@code energy} unit, joules. */
    public static final double ENERGY_UNIT_J = 1e10;

    /** {@code 2026-Oct-13 04:29}. */
    static final DateTimeFormatter CAD_TIME = new DateTimeFormatterBuilder().parseCaseInsensitive()
            .appendPattern("uuuu-MMM-dd HH:mm").toFormatter(Locale.US);

    private static final JsonMapper JSON = JsonMapper.builder().build();

    /**
     * One parsed response.
     *
     * @param version the signature version
     * @param records one record per row
     * @param skipped skip reason to count
     */
    public record Parsed(String version, List<NormalizedRecord> records, Map<String, Integer> skipped) {}

    private JplParsers() {}

    /**
     * Parses a {@code fireball.api} response.
     *
     * @throws UpstreamFormatException when the body is not the API's table or lacks a required column
     */
    public static Parsed fireballs(byte[] body) {
        Table t = table(body, "fireball", List.of("date", "energy", "lat", "lat-dir", "lon", "lon-dir"));
        List<NormalizedRecord> out = new ArrayList<>();
        Map<String, Integer> skipped = new TreeMap<>();
        Set<String> ids = new HashSet<>();
        for (JsonNode row : t.data()) {
            MappedRecord m = fireball(t, row);
            accept(m, out, skipped, ids);
        }
        return new Parsed(t.version(), out, skipped);
    }

    /**
     * Parses a {@code cad.api} response.
     *
     * @throws UpstreamFormatException when the body is not the API's table or lacks a required column
     */
    public static Parsed closeApproaches(byte[] body) {
        Table t = table(body, "cad", List.of("des", "cd", "dist"));
        List<NormalizedRecord> out = new ArrayList<>();
        Map<String, Integer> skipped = new TreeMap<>();
        Set<String> ids = new HashSet<>();
        for (JsonNode row : t.data()) {
            accept(closeApproach(t, row), out, skipped, ids);
        }
        return new Parsed(t.version(), out, skipped);
    }

    static MappedRecord fireball(Table t, JsonNode row) {
        String date = t.text(row, "date");
        Instant at = SpaceSources.utc(date);
        if (date == null || at == null) {
            return MappedRecord.skipped(MappedRecord.MISSING_ID);
        }
        NormalizedRecord.Builder b = NormalizedRecord.builder(date, FIREBALL_KIND).eventTime(at);
        Double lat = signed(t.number(row, "lat"), t.text(row, "lat-dir"), "S", "N");
        Double lon = signed(t.number(row, "lon"), t.text(row, "lon-dir"), "W", "E");
        if (lat != null && lon != null && Math.abs(lat) <= 90 && Math.abs(lon) <= 180) {
            b.point(lon, lat);
        }
        Double energy = t.number(row, "energy");
        return MappedRecord.of(b.put("radiatedEnergyJ", energy == null ? null : energy * ENERGY_UNIT_J)
                .put("impactEnergyKt", t.number(row, "impact-e"))
                .put("altitudeKm", t.number(row, "alt"))
                .put("velocityKms", t.number(row, "vel"))
                .build());
    }

    static MappedRecord closeApproach(Table t, JsonNode row) {
        String des = t.text(row, "des");
        String cd = t.text(row, "cd");
        if (des == null || cd == null) {
            return MappedRecord.skipped(MappedRecord.MISSING_ID);
        }
        Double dist = t.number(row, "dist");
        String fullName = t.text(row, "fullname");
        return MappedRecord.of(NormalizedRecord.builder(des + "@" + cd, CLOSE_APPROACH_KIND)
                .eventTime(cadTime(cd))
                .put("designation", des)
                .put("fullName", fullName == null ? null : fullName.strip())
                .put("distAu", dist)
                .put("distLunar", dist == null ? null : SpaceSources.round(dist * LUNAR_DISTANCES_PER_AU, 2))
                .put("distMinAu", t.number(row, "dist_min"))
                .put("distMaxAu", t.number(row, "dist_max"))
                .put("vRelKms", t.number(row, "v_rel"))
                .put("absMagnitude", t.number(row, "h"))
                .put("orbitId", t.text(row, "orbit_id"))
                .put("timeSigma", t.text(row, "t_sigma_f"))
                .build());
    }

    /** {@code 2026-Oct-13 04:29} (TDB) as an instant read as UTC, or null. */
    static Instant cadTime(String cd) {
        try {
            return LocalDateTime.parse(cd.strip(), CAD_TIME).toInstant(ZoneOffset.UTC);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /** {@code value} negated when {@code dir} is {@code negative}; null when {@code dir} is neither letter. */
    static Double signed(Double value, String dir, String negative, String positive) {
        if (value == null || dir == null) {
            return null;
        }
        if (dir.equalsIgnoreCase(negative)) {
            return -value;
        }
        return dir.equalsIgnoreCase(positive) ? value : null;
    }

    private static void accept(MappedRecord m, List<NormalizedRecord> out, Map<String, Integer> skipped,
            Set<String> ids) {
        if (!m.accepted()) {
            skipped.merge(m.reason(), 1, Integer::sum);
        } else if (ids.add(m.record().externalId())) {
            out.add(m.record());
        } else {
            skipped.merge(MappedRecord.DUPLICATE_ID, 1, Integer::sum);
        }
    }

    /**
     * The table of a response.
     *
     * @param version the signature version
     * @param columns field name to column index
     * @param data the rows
     */
    record Table(String version, Map<String, Integer> columns, JsonNode data) {

        /** The cell as trimmed text, or null. */
        String text(JsonNode row, String field) {
            Integer i = columns.get(field);
            if (i == null || !row.isArray() || i >= row.size()) {
                return null;
            }
            JsonNode v = row.get(i);
            if (v == null || v.isNull() || !(v.isString() || v.isNumber())) {
                return null;
            }
            String s = v.asString().strip();
            return s.isEmpty() ? null : s;
        }

        /** The cell as a finite number (the APIs send numbers as strings), or null. */
        Double number(JsonNode row, String field) {
            String s = text(row, field);
            if (s == null) {
                return null;
            }
            try {
                double d = Double.parseDouble(s);
                return Double.isFinite(d) ? d : null;
            } catch (NumberFormatException e) {
                return null;
            }
        }
    }

    static Table table(byte[] body, String api, List<String> required) {
        JsonNode root;
        try {
            root = JSON.readTree(body);
        } catch (JacksonException e) {
            throw new UpstreamFormatException("JPL " + api + " API answer is not JSON: " + e.getOriginalMessage(), "",
                    e);
        }
        if (root == null || !root.isObject()) {
            throw new UpstreamFormatException("JPL " + api + " API answer is not a JSON object", "");
        }
        JsonNode fields = root.get("fields");
        JsonNode data = root.get("data");
        String version = root.path("signature").path("version").asString(null);
        if (fields == null && data == null && root.path("count").asInt(-1) == 0) {
            // An empty window: the API leaves out fields and data when count is 0.
            return new Table(version, Map.of(), JSON.createArrayNode());
        }
        if (fields == null || !fields.isArray()) {
            throw new UpstreamFormatException("JPL " + api + " API answer has no fields", "");
        }
        Map<String, Integer> columns = new HashMap<>();
        for (int i = 0; i < fields.size(); i++) {
            columns.put(fields.get(i).asString(), i);
        }
        for (String r : required) {
            if (!columns.containsKey(r)) {
                throw new UpstreamFormatException("JPL " + api + " API answer has no '" + r + "' field", "");
            }
        }
        return new Table(version, columns, data != null && data.isArray() ? data : JSON.createArrayNode());
    }
}
