package ai.foresignal.source.adapters.hydro.coops;

import ai.foresignal.source.adapters.env.EnvSources;
import ai.foresignal.source.adapters.hydro.HydroSources;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * NOAA CO-OPS water levels, tide predictions and flood thresholds as {@code tide-gauge} records (prompt 079). Pure.
 *
 * <p>Verified 2026-10-13 against The Battery (8518750) and San Francisco (9414290):
 * <ul>
 *   <li>{@code product=water_level&date=latest&datum=MLLW&units=metric&time_zone=gmt} answers
 *       {@code {metadata: {id, name, lat, lon}, data: [{t, v, s, f, q}]}}, every value text, {@code t} as
 *       {@code yyyy-MM-dd HH:mm} GMT, {@code q} {@code p} (preliminary) or {@code v} (verified); an unknown station or
 *       a station without MLLW (the Great Lakes) answers {@code {"error": {"message": …}}};
 *   <li>{@code product=predictions&interval=hilo} answers {@code {predictions: [{t, v, type: H|L}]}};
 *   <li>{@code mdapi …/floodlevels.json} gives {@code nos_minor, nos_moderate, nos_major, nws_minor, nws_moderate,
 *       nws_major, action} in feet, and {@code …/datums.json} gives the station's datums in its {@code units}. The
 *       flood levels are heights above the station datum ({@code STND}), like the datums table: The Battery's NOS
 *       minor 10.19 ft is 6.90 ft above its MLLW (3.29 ft), and its record 17.33 ft (2012-10-30) is the 14.04 ft MLLW
 *       crest of Sandy. They are converted to meters above MLLW: {@code (level − MLLW) × 0.3048}.
 * </ul>
 *
 * <p>{@code floodCategory} ({@code none|action|minor|moderate|major}) compares the water level with the NOS
 * thresholds, or the NWS ones when the station has no NOS minor level.
 */
public final class CoopsMapper {

    /** The record kind. */
    public static final String KIND = "tide-gauge";

    /** The datum of every height. */
    public static final String DATUM = "MLLW";

    /** Feet to meters. */
    static final double FOOT_M = 0.3048;

    private static final DateTimeFormatter GMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.ROOT);

    private CoopsMapper() {}

    /**
     * One {@code water_level&date=latest} answer.
     *
     * @param id the station id
     * @param name the station name
     * @param latitude degrees, or null
     * @param longitude degrees, or null
     * @param at the reading time, or null
     * @param levelM the water level above MLLW, meters, or null
     * @param sigmaM the standard deviation of the 1-second samples, meters, or null
     * @param quality {@code preliminary}, {@code verified} or the raw flag
     */
    public record WaterLevel(String id, String name, Double latitude, Double longitude, Instant at, Double levelM,
            Double sigmaM, String quality) {}

    /**
     * One predicted high or low water.
     *
     * @param at when
     * @param heightM the height above MLLW, meters
     * @param high true for a high water
     */
    public record Tide(Instant at, double heightM, boolean high) {}

    /**
     * A station's flood thresholds, meters above MLLW.
     *
     * @param actionM the action level, or null
     * @param minorM the minor flood level, or null
     * @param moderateM the moderate flood level, or null
     * @param majorM the major flood level, or null
     * @param authority {@code NOS} or {@code NWS}, whose minor/moderate/major were used
     */
    public record FloodLevels(Double actionM, Double minorM, Double moderateM, Double majorM, String authority) {

        /** No thresholds. */
        public static final FloodLevels NONE = new FloodLevels(null, null, null, null, null);
    }

    /** {@code yyyy-MM-dd HH:mm} in GMT, or null. */
    static Instant gmt(String text) {
        if (text == null) {
            return null;
        }
        try {
            return LocalDateTime.parse(text.strip(), GMT).toInstant(ZoneOffset.UTC);
        } catch (DateTimeException e) {
            return null;
        }
    }

    /**
     * Reads a {@code water_level} answer.
     *
     * @throws UpstreamFormatException on a CO-OPS error object, a body that is not JSON, or no {@code data}
     */
    public static WaterLevel waterLevel(byte[] body) {
        JsonNode root = HydroSources.json(body, "CO-OPS water_level");
        JsonNode metadata = root.path("metadata");
        JsonNode data = root.get("data");
        if (data == null || !data.isArray() || data.isEmpty()) {
            throw new UpstreamFormatException("CO-OPS water_level has no data", "");
        }
        JsonNode last = data.get(data.size() - 1);
        String q = HydroSources.text(last.get("q"));
        String quality = q == null ? null : switch (q.toLowerCase(Locale.ROOT)) {
            case "p" -> "preliminary";
            case "v" -> "verified";
            default -> q;
        };
        return new WaterLevel(HydroSources.text(metadata.get("id")), HydroSources.text(metadata.get("name")),
                HydroSources.number(metadata.get("lat")), HydroSources.number(metadata.get("lon")),
                gmt(HydroSources.text(last.get("t"))), HydroSources.number(last.get("v")),
                HydroSources.number(last.get("s")), quality);
    }

    /**
     * Reads a {@code predictions&interval=hilo} answer, sorted by time.
     *
     * @throws UpstreamFormatException on a CO-OPS error object, a body that is not JSON, or no {@code predictions}
     */
    public static List<Tide> predictions(byte[] body) {
        JsonNode root = HydroSources.json(body, "CO-OPS predictions");
        JsonNode list = root.get("predictions");
        if (list == null || !list.isArray()) {
            throw new UpstreamFormatException("CO-OPS predictions has no predictions array", "");
        }
        List<Tide> out = new ArrayList<>();
        for (JsonNode p : list) {
            Instant at = gmt(HydroSources.text(p.get("t")));
            Double v = HydroSources.number(p.get("v"));
            String type = HydroSources.text(p.get("type"));
            if (at != null && v != null && type != null) {
                out.add(new Tide(at, v, type.toUpperCase(Locale.ROOT).startsWith("H")));
            }
        }
        out.sort(Comparator.comparing(Tide::at));
        return out;
    }

    /** The first high ({@code high}) or low water strictly after {@code now}, or null. */
    public static Tide next(List<Tide> tides, Instant now, boolean high) {
        for (Tide t : tides) {
            if (t.high() == high && t.at().isAfter(now)) {
                return t;
            }
        }
        return null;
    }

    /**
     * The flood thresholds in meters above MLLW.
     *
     * @param floodLevels the {@code floodlevels.json} answer (feet above the station datum)
     * @param datums the {@code datums.json} answer
     * @return the thresholds; {@link FloodLevels#NONE} when the station has no MLLW or no thresholds
     */
    public static FloodLevels floodLevels(JsonNode floodLevels, JsonNode datums) {
        Double mllw = null;
        JsonNode list = datums == null ? null : datums.get("datums");
        if (list != null && list.isArray()) {
            for (JsonNode d : list) {
                if (DATUM.equalsIgnoreCase(HydroSources.text(d.get("name")))) {
                    mllw = HydroSources.number(d.get("value"));
                }
            }
        }
        if (mllw == null || floodLevels == null || !floodLevels.isObject()) {
            return FloodLevels.NONE;
        }
        String units = datums == null ? null : HydroSources.text(datums.get("units"));
        double factor = units != null && units.toLowerCase(Locale.ROOT).startsWith("met") ? 1.0 : FOOT_M;
        boolean nos = HydroSources.number(floodLevels.get("nos_minor")) != null;
        String prefix = nos ? "nos_" : "nws_";
        return new FloodLevels(convert(floodLevels.get("action"), mllw, factor),
                convert(floodLevels.get(prefix + "minor"), mllw, factor),
                convert(floodLevels.get(prefix + "moderate"), mllw, factor),
                convert(floodLevels.get(prefix + "major"), mllw, factor), nos ? "NOS" : "NWS");
    }

    private static Double convert(JsonNode level, double mllw, double factor) {
        Double value = HydroSources.number(level);
        return value == null ? null : EnvSources.round((value - mllw) * factor, 3);
    }

    /** {@code none|action|minor|moderate|major} for {@code levelM}, or null when there is no level or minor level. */
    public static String category(Double levelM, FloodLevels levels) {
        if (levelM == null || levels == null || levels.minorM() == null) {
            return null;
        }
        if (levels.majorM() != null && levelM >= levels.majorM()) {
            return "major";
        }
        if (levels.moderateM() != null && levelM >= levels.moderateM()) {
            return "moderate";
        }
        if (levelM >= levels.minorM()) {
            return "minor";
        }
        if (levels.actionM() != null && levelM >= levels.actionM()) {
            return "action";
        }
        return "none";
    }

    /**
     * The record of one station.
     *
     * @param stationId the station id asked for
     * @param level the water level answer
     * @param tides the cached predictions, possibly empty
     * @param levels the cached thresholds, or null
     * @param now the fetch time, for the next high and low
     * @return the record, or null when the reading has no time or no usable location
     */
    public static NormalizedRecord record(String stationId, WaterLevel level, List<Tide> tides, FloodLevels levels,
            Instant now) {
        if (level.at() == null || !EnvSources.validPoint(level.latitude(), level.longitude())) {
            return null;
        }
        FloodLevels f = levels == null ? FloodLevels.NONE : levels;
        NormalizedRecord.Builder b = NormalizedRecord.builder(stationId, KIND).observedAt(level.at())
                .eventTime(level.at()).point(level.longitude(), level.latitude())
                .put("name", level.name())
                .put("waterLevelM", level.levelM())
                .put("datum", DATUM)
                .put("sigmaM", level.sigmaM())
                .put("quality", level.quality());
        Tide high = next(tides, now, true);
        Tide low = next(tides, now, false);
        if (high != null) {
            b.put("nextHighTime", high.at().toString()).put("nextHighM", high.heightM());
        }
        if (low != null) {
            b.put("nextLowTime", low.at().toString()).put("nextLowM", low.heightM());
        }
        b.put("floodCategory", category(level.levelM(), f))
                .put("minorFloodM", f.minorM())
                .put("moderateFloodM", f.moderateM())
                .put("majorFloodM", f.majorM());
        return b.build();
    }

    /**
     * The ids of the stations a water-level request may name, from {@code stations.json?type=waterlevels}: every
     * station except the Great Lakes ones (they have no MLLW).
     *
     * @throws UpstreamFormatException when it has no {@code stations} array
     */
    public static Set<String> stationIds(JsonNode root) {
        JsonNode list = root == null ? null : root.get("stations");
        if (list == null || !list.isArray()) {
            throw new UpstreamFormatException("CO-OPS station list has no stations array", "");
        }
        Set<String> out = new LinkedHashSet<>();
        for (JsonNode s : list) {
            String id = HydroSources.text(s.get("id"));
            JsonNode lakes = s.get("greatlakes");
            if (id != null && !(lakes != null && lakes.isBoolean() && lakes.booleanValue())) {
                out.add(id);
            }
        }
        return out;
    }

    /** Predictions as compact JSON, for the adapter state. */
    public static String tidesToJson(List<Tide> tides) {
        ArrayNode root = HydroSources.JSON.createArrayNode();
        for (Tide t : tides) {
            ObjectNode n = root.addObject();
            n.put("t", t.at().getEpochSecond());
            n.put("v", t.heightM());
            n.put("h", t.high());
        }
        return root.toString();
    }

    /** Predictions written by {@link #tidesToJson}; empty for null or damaged text. */
    public static List<Tide> tidesFromJson(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            JsonNode root = HydroSources.JSON.readTree(json);
            List<Tide> out = new ArrayList<>();
            if (root != null && root.isArray()) {
                for (JsonNode n : root) {
                    if (n.get("t") != null && n.get("t").canConvertToLong() && n.get("v") != null
                            && n.get("v").isNumber() && n.get("h") != null && n.get("h").isBoolean()) {
                        out.add(new Tide(Instant.ofEpochSecond(n.get("t").longValue()), n.get("v").doubleValue(),
                                n.get("h").booleanValue()));
                    }
                }
            }
            return out;
        } catch (JacksonException e) {
            return List.of();
        }
    }

    /** Thresholds as compact JSON, for the adapter state. */
    public static String levelsToJson(FloodLevels f) {
        ObjectNode n = HydroSources.JSON.createObjectNode();
        putIf(n, "a", f.actionM());
        putIf(n, "mi", f.minorM());
        putIf(n, "mo", f.moderateM());
        putIf(n, "ma", f.majorM());
        if (f.authority() != null) {
            n.put("by", f.authority());
        }
        return n.toString();
    }

    /** Thresholds written by {@link #levelsToJson}; null for null or damaged text. */
    public static FloodLevels levelsFromJson(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            JsonNode n = HydroSources.JSON.readTree(json);
            if (n == null || !n.isObject()) {
                return null;
            }
            return new FloodLevels(HydroSources.number(n.get("a")), HydroSources.number(n.get("mi")),
                    HydroSources.number(n.get("mo")), HydroSources.number(n.get("ma")),
                    HydroSources.text(n.get("by")));
        } catch (JacksonException e) {
            return null;
        }
    }

    private static void putIf(ObjectNode n, String key, Double value) {
        if (value != null) {
            n.put(key, value);
        }
    }
}
