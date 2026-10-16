package ai.foresignal.source.adapters.spaceports;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.support.ReferenceSupport;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

/**
 * The Jonathan McDowell GCAT TSV tables (prompt 094): {@code sites.tsv} (launch sites) and {@code lp.tsv}
 * (launch pads).
 *
 * <p>The header is the first line, prefixed with {@code #}. A field of {@code -} is blank. Coordinates may be
 * missing; the record is still emitted with null geometry. Rows are read line by line and handed to a sink, so
 * a table is never held as one list of records.
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

    /** Streams {@code sites.tsv} into {@code sink}. */
    public static ReferenceSupport.ParseCounts parseSites(byte[] body, Consumer<NormalizedRecord> sink) {
        return parse(body, SITE_KIND, "site:", "Site", sink);
    }

    /** Parses {@code lp.tsv}. */
    public static List<NormalizedRecord> parsePads(byte[] body) {
        return parse(body, PAD_KIND, "pad:", "Pad");
    }

    /** Streams {@code lp.tsv} into {@code sink}. */
    public static ReferenceSupport.ParseCounts parsePads(byte[] body, Consumer<NormalizedRecord> sink) {
        return parse(body, PAD_KIND, "pad:", "Pad", sink);
    }

    /**
     * Parses a GCAT TSV document into a list.
     *
     * @param kind the record kind
     * @param idPrefix the externalId prefix
     * @param idColumn the header naming the identity column; falls back to {@code Site}
     */
    public static List<NormalizedRecord> parse(byte[] body, String kind, String idPrefix, String idColumn) {
        List<NormalizedRecord> out = new ArrayList<>();
        parse(body, kind, idPrefix, idColumn, out::add);
        return out;
    }

    /**
     * Streams a GCAT TSV document into {@code sink}, line by line.
     *
     * @param kind the record kind
     * @param idPrefix the externalId prefix
     * @param idColumn the header naming the identity column; falls back to {@code Site}
     * @throws UpstreamFormatException when the first non-blank line is not a {@code #}-prefixed header
     */
    public static ReferenceSupport.ParseCounts parse(byte[] body, String kind, String idPrefix, String idColumn,
            Consumer<NormalizedRecord> sink) {
        int records = 0;
        int skipped = 0;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new ByteArrayInputStream(body), StandardCharsets.UTF_8))) {
            Map<String, Integer> index = null;
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                if (index == null) {
                    if (!line.startsWith("#")) {
                        throw new UpstreamFormatException("GCAT TSV has no #-prefixed header", "");
                    }
                    String[] header = split(line.substring(1));
                    index = new HashMap<>();
                    for (int i = 0; i < header.length; i++) {
                        index.putIfAbsent(header[i].strip().toLowerCase(Locale.ROOT), i);
                    }
                    continue;
                }
                NormalizedRecord record = map(split(line), index, kind, idPrefix, idColumn);
                if (record == null) {
                    skipped++;
                } else {
                    sink.accept(record);
                    records++;
                }
            }
            if (index == null) {
                throw new UpstreamFormatException("GCAT TSV has no #-prefixed header", "");
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return new ReferenceSupport.ParseCounts(records, skipped, 0);
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
