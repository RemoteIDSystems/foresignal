package ai.foresignal.source.adapters.spaceports;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The Jonathan McDowell GCAT TSV tables (prompt 094): {@code sites.tsv} (launch sites) and {@code lp.tsv}
 * (launch pads).
 *
 * <p>The header is the first line, prefixed with {@code #}. A field of {@code -} is blank. Coordinates may be
 * missing; the record is still emitted with null geometry.
 */
public final class GcatParser {

    /** The launch-site kind. */
    public static final String SITE_KIND = "launch-site";

    /** The launch-pad kind. */
    public static final String PAD_KIND = "launch-pad";

    private GcatParser() {}

    /** Parses {@code sites.tsv}. */
    public static List<NormalizedRecord> parseSites(byte[] body) {
        return parse(body, SITE_KIND, "site:", "Site");
    }

    /** Parses {@code lp.tsv}. */
    public static List<NormalizedRecord> parsePads(byte[] body) {
        return parse(body, PAD_KIND, "pad:", "Pad");
    }

    /**
     * Parses a GCAT TSV document.
     *
     * @param kind the record kind
     * @param idPrefix the externalId prefix
     * @param idColumn the header naming the identity column; falls back to {@code Site}
     */
    public static List<NormalizedRecord> parse(byte[] body, String kind, String idPrefix, String idColumn) {
        List<String> lines = new ArrayList<>();
        for (String line : new String(body, StandardCharsets.UTF_8).split("\r\n|\n|\r")) {
            if (!line.isBlank()) {
                lines.add(line);
            }
        }
        if (lines.isEmpty() || !lines.get(0).startsWith("#")) {
            throw new UpstreamFormatException("GCAT TSV has no #-prefixed header", "");
        }
        String[] header = split(lines.get(0).substring(1));
        Map<String, Integer> index = new HashMap<>();
        for (int i = 0; i < header.length; i++) {
            index.putIfAbsent(header[i].strip().toLowerCase(Locale.ROOT), i);
        }
        List<NormalizedRecord> out = new ArrayList<>(Math.max(0, lines.size() - 1));
        for (int i = 1; i < lines.size(); i++) {
            String[] fields = split(lines.get(i));
            NormalizedRecord record = map(fields, index, kind, idPrefix, idColumn);
            if (record != null) {
                out.add(record);
            }
        }
        return out;
    }

    private static NormalizedRecord map(String[] fields, Map<String, Integer> index, String kind, String idPrefix,
            String idColumn) {
        String id = value(fields, index, idColumn);
        if (id == null) {
            id = value(fields, index, "Site");
        }
        if (id == null) {
            return null;
        }
        // A pad names its launch site in the Site column; a site's parent (rarely set) is in Parent.
        String parent = value(fields, index, "Parent");
        if (parent == null && !"Site".equalsIgnoreCase(idColumn)) {
            parent = value(fields, index, "Site");
        }
        NormalizedRecord.Builder b = NormalizedRecord.builder(idPrefix + id, kind)
                .put("code", value(fields, index, "Code"))
                .put("type", value(fields, index, "Type"))
                .put("stateCode", value(fields, index, "StateCode"))
                .put("name", value(fields, index, "Name"))
                .put("tStart", value(fields, index, "TStart"))
                .put("tStop", value(fields, index, "TStop"))
                .put("parent", parent)
                .put("errorKm", number(value(fields, index, "Error")));
        Double lat = number(value(fields, index, "Latitude"));
        Double lon = number(value(fields, index, "Longitude"));
        if (lat != null && lon != null && Math.abs(lat) <= 90 && Math.abs(lon) <= 180) {
            b.point(lon, lat);
        }
        return b.build();
    }

    private static String value(String[] fields, Map<String, Integer> index, String column) {
        Integer i = index.get(column.toLowerCase(Locale.ROOT));
        if (i == null || i >= fields.length) {
            return null;
        }
        String value = fields[i].strip();
        return value.isEmpty() || value.equals("-") ? null : value;
    }

    private static Double number(String value) {
        if (value == null) {
            return null;
        }
        try {
            double parsed = Double.parseDouble(value.replace(",", "").strip());
            return Double.isFinite(parsed) ? parsed : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String[] split(String line) {
        return line.split("\t", -1);
    }
}
