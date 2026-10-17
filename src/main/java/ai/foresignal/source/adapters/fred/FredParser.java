package ai.foresignal.source.adapters.fred;

import ai.foresignal.source.series.SeriesMeta;
import ai.foresignal.source.series.SeriesPeriods;
import ai.foresignal.source.series.SeriesPoint;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.support.JsonProps;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * Reads the FRED API's JSON (prompt 117): series metadata ({@code /fred/series}), observations
 * ({@code /fred/series/observations}), a series' release ({@code /fred/series/release}) and a release's sources
 * ({@code /fred/release/sources}). Pure: no network, no state.
 *
 * <p>A series is <em>copyrighted</em> when its {@code notes} contain "copyright" or "reprinted with permission"
 * (case-insensitive): FRED's terms of use say "Copyrighted series contain the word 'Copyright' in their notes".
 * An observation value {@code "."} means missing and becomes no record.
 */
public final class FredParser {

    /** The notice FRED's terms of use require on every application. */
    public static final String NOTICE =
            "This product uses the FRED® API but is not endorsed or certified by the Federal Reserve Bank of"
                    + " St. Louis.";

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private FredParser() {}

    /**
     * One series' metadata.
     *
     * @param id the FRED series id
     * @param title the title
     * @param units the units text ({@code Percent}, {@code Dollars per Barrel})
     * @param frequencyShort the short frequency ({@code D}, {@code W}, {@code M}, {@code Q}, {@code A})
     * @param notes the notes, possibly empty
     * @param lastUpdated FRED's {@code last_updated} text
     */
    public record SeriesInfo(String id, String title, String units, String frequencyShort, String notes,
            String lastUpdated) {

        /** True when the notes mark the series as copyrighted. */
        public boolean copyrighted() {
            return isCopyrighted(notes);
        }

        /** The stored form (a compact JSON object), with the series' source line and the read time. */
        public String encode(String sourceLine, Instant checkedAt) {
            ObjectNode node = JSON.createObjectNode();
            node.put("id", id);
            node.put("title", title);
            node.put("units", units);
            node.put("frequencyShort", frequencyShort);
            node.put("copyrighted", copyrighted());
            node.put("lastUpdated", lastUpdated);
            node.put("sourceLine", sourceLine);
            node.put("checkedAt", checkedAt == null ? null : checkedAt.toString());
            return JSON.writeValueAsString(node);
        }
    }

    /**
     * Metadata as stored in adapter state (the notes themselves are not stored, only whether they mark the series
     * copyrighted).
     *
     * @param info the metadata, with empty notes
     * @param copyrighted whether the notes marked the series copyrighted
     * @param sourceLine the series' source line, or null
     * @param checkedAt when the metadata was read, or null
     */
    public record Stored(SeriesInfo info, boolean copyrighted, String sourceLine, Instant checkedAt) {}

    /** Metadata stored by {@link SeriesInfo#encode}, or null when absent or unreadable. */
    public static Stored decode(String stored) {
        if (stored == null || stored.isBlank()) {
            return null;
        }
        try {
            JsonNode node = JSON.readTree(stored);
            if (node == null || !node.isObject()) {
                return null;
            }
            SeriesInfo info = new SeriesInfo(JsonProps.text(node, "id"), JsonProps.text(node, "title"),
                    JsonProps.text(node, "units"), JsonProps.text(node, "frequencyShort"), "",
                    JsonProps.text(node, "lastUpdated"));
            String checked = JsonProps.text(node, "checkedAt");
            Instant checkedAt = null;
            if (checked != null) {
                try {
                    checkedAt = Instant.parse(checked);
                } catch (java.time.format.DateTimeParseException e) {
                    checkedAt = null;
                }
            }
            JsonNode copyrighted = node.get("copyrighted");
            return new Stored(info, copyrighted != null && copyrighted.asBoolean(false),
                    JsonProps.text(node, "sourceLine"), checkedAt);
        } catch (JacksonException e) {
            return null;
        }
    }

    /** True when {@code notes} mark a series as copyrighted. */
    public static boolean isCopyrighted(String notes) {
        if (notes == null) {
            return false;
        }
        String lower = notes.toLowerCase(Locale.ROOT);
        return lower.contains("copyright") || lower.contains("reprinted with permission");
    }

    /**
     * The first series of a {@code /fred/series} body.
     *
     * @throws UpstreamFormatException when the body has no series
     */
    public static SeriesInfo series(byte[] body) {
        JsonNode root = tree(body);
        JsonNode list = root.get("seriess");
        if (list == null || !list.isArray() || list.isEmpty()) {
            throw new UpstreamFormatException("fred-series: /fred/series returned no series", null);
        }
        JsonNode s = list.get(0);
        return new SeriesInfo(JsonProps.text(s, "id"), JsonProps.text(s, "title"), JsonProps.text(s, "units"),
                JsonProps.text(s, "frequency_short"), s.has("notes") ? s.get("notes").asString("") : "",
                JsonProps.text(s, "last_updated"));
    }

    /** The first release id of a {@code /fred/series/release} body, or null. */
    public static Long releaseId(byte[] body) {
        JsonNode list = tree(body).get("releases");
        return list == null || !list.isArray() || list.isEmpty() ? null : JsonProps.integer(list.get(0), "id");
    }

    /** The release name of a {@code /fred/series/release} body, or null. */
    public static String releaseName(byte[] body) {
        JsonNode list = tree(body).get("releases");
        return list == null || !list.isArray() || list.isEmpty() ? null : JsonProps.text(list.get(0), "name");
    }

    /** The source names of a {@code /fred/release/sources} body, joined with "; ", or null when none. */
    public static String sourceNames(byte[] body) {
        JsonNode list = tree(body).get("sources");
        if (list == null || !list.isArray() || list.isEmpty()) {
            return null;
        }
        List<String> names = new ArrayList<>();
        for (JsonNode source : list) {
            String name = JsonProps.text(source, "name");
            if (name != null) {
                names.add(name);
            }
        }
        return names.isEmpty() ? null : String.join("; ", names);
    }

    /**
     * The series' own source line: {@code Source: {sources} ({release}), via FRED, Federal Reserve Bank of St.
     * Louis}, with the parts that are known.
     */
    public static String sourceLine(String sources, String release) {
        StringBuilder out = new StringBuilder("Source: ");
        if (sources != null) {
            out.append(sources);
            if (release != null) {
                out.append(" (").append(release).append(')');
            }
            out.append(", via ");
        } else if (release != null) {
            out.append(release).append(", via ");
        }
        return out.append("FRED, Federal Reserve Bank of St. Louis").toString();
    }

    /** The attribution a FRED series carries: the required notice and its source line. */
    public static String attribution(String sourceLine) {
        return sourceLine == null ? NOTICE : NOTICE + " " + sourceLine;
    }

    /** The Foresignal frequency code of a FRED short frequency. */
    public static String frequency(String frequencyShort) {
        if (frequencyShort == null) {
            return "P1D";
        }
        String f = frequencyShort.strip().toUpperCase(Locale.ROOT);
        if (f.startsWith("W") || f.equals("BW")) {
            return "P1W";
        }
        if (f.startsWith("M")) {
            return "P1M";
        }
        if (f.startsWith("Q")) {
            return "P3M";
        }
        if (f.startsWith("A") || f.startsWith("SA")) {
            return "P1Y";
        }
        return "P1D";
    }

    /** The family of a FRED units text: percent → rate, dollars per barrel or MMBtu → price, otherwise index. */
    public static String family(String units) {
        String u = units == null ? "" : units.toLowerCase(Locale.ROOT);
        if (u.contains("percent")) {
            return SeriesMeta.RATE;
        }
        if (u.contains("dollars per barrel") || u.contains("per million btu") || u.contains("per mmbtu")
                || u.startsWith("dollars per")) {
            return SeriesMeta.PRICE;
        }
        return SeriesMeta.INDEX;
    }

    /** The series metadata of a FRED series: its family, the transform for that family, a 1-day release lag. */
    public static SeriesMeta meta(String units) {
        String family = family(units);
        String transform = SeriesMeta.RATE.equals(family) ? SeriesMeta.DIFFERENCE : SeriesMeta.LOG_RETURN;
        return SeriesMeta.of(family, transform, 1);
    }

    /** A UCUM code for a FRED units text, or the text without spaces when no mapping is known. */
    public static String ucum(String units) {
        if (units == null) {
            return "1";
        }
        String u = units.toLowerCase(Locale.ROOT);
        if (u.startsWith("percent")) {
            return "%";
        }
        if (u.contains("dollars per barrel")) {
            return "[USD]/[bbl_us]";
        }
        if (u.contains("per million btu")) {
            return "[USD]/10*6.[Btu_IT]";
        }
        if (u.startsWith("index")) {
            return "1";
        }
        return units.strip().replaceAll("\\s+", "_");
    }

    /**
     * The points of a {@code /fred/series/observations} body; a {@code "."} value is skipped.
     *
     * @param info the series' metadata
     * @param body the observations body
     * @param observedAt the fetch time
     */
    public static List<NormalizedRecord> observations(SeriesInfo info, byte[] body, Instant observedAt) {
        JsonNode list = tree(body).get("observations");
        List<NormalizedRecord> out = new ArrayList<>();
        if (list == null || !list.isArray()) {
            throw new UpstreamFormatException("fred-series: the observations body has no observations array", null);
        }
        String frequency = frequency(info.frequencyShort());
        String seriesId = "fred." + info.id();
        String unit = ucum(info.units());
        for (JsonNode row : list) {
            String date = JsonProps.text(row, "date");
            String raw = JsonProps.text(row, "value");
            if (date == null || raw == null || ".".equals(raw)) {
                continue;
            }
            Double value = SeriesPoint.number(raw);
            SeriesPeriods.Period period = SeriesPeriods.parse(frequency, date);
            if (value == null || period == null) {
                continue;
            }
            out.add(SeriesPoint.point(seriesId, info.title() == null ? info.id() : info.title(), "fred/" + info.id(),
                    frequency, date, period.start(), period.end(), value, unit, "US", null, "final", observedAt));
        }
        return out;
    }

    private static JsonNode tree(byte[] body) {
        if (body == null || body.length == 0) {
            throw new UpstreamFormatException("fred-series: empty response", null);
        }
        try {
            JsonNode root = JSON.readTree(body);
            if (root == null || !root.isObject()) {
                throw new UpstreamFormatException("fred-series: the response is not a JSON object", null);
            }
            return root;
        } catch (JacksonException e) {
            throw new UpstreamFormatException("fred-series: the response is not JSON: " + e.getOriginalMessage(),
                    null, e);
        }
    }
}
